/*
 * Copyright 2020 SulzbachR.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.clownfish.clownfish.webdav;

/**
 *
 * @author SulzbachR
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Stack;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.catalina.WebResource;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.util.ConcurrentDateFormat;
import org.apache.catalina.util.DOMWriter;
import org.apache.catalina.util.XMLWriter;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.http.FastHttpDateFormat;
import org.apache.tomcat.util.http.RequestUtil;
import org.apache.tomcat.util.security.ConcurrentMessageDigest;
import org.apache.tomcat.util.security.MD5Encoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class WebdavServlet extends DefaultServlet {

    /**
     * Default namespace.
     */
    protected static final String DEFAULT_NAMESPACE = "DAV:";
    /**
     * Simple date format for the creation date ISO representation (partial).
     */
    protected static final ConcurrentDateFormat creationDateFormat
            = new ConcurrentDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US,
                    TimeZone.getTimeZone("GMT"));
    private static final long serialVersionUID = 1L;

    // -------------------------------------------------------------- Constants
    private static final String METHOD_PROPFIND = "PROPFIND";
    private static final String METHOD_PROPPATCH = "PROPPATCH";
    private static final String METHOD_MKCOL = "MKCOL";
    private static final String METHOD_COPY = "COPY";
    private static final String METHOD_MOVE = "MOVE";
    private static final String METHOD_LOCK = "LOCK";
    private static final String METHOD_UNLOCK = "UNLOCK";
    private static final String METHOD_OPTIONS = "OPTIONS";
    /**
     * PROPFIND - Specify a property mask.
     */
    private static final int FIND_BY_PROPERTY = 0;
    /**
     * PROPFIND - Display all properties.
     */
    private static final int FIND_ALL_PROP = 1;
    /**
     * PROPFIND - Return property names.
     */
    private static final int FIND_PROPERTY_NAMES = 2;
    /**
     * Create a new lock.
     */
    private static final int LOCK_CREATION = 0;
    /**
     * Refresh lock.
     */
    private static final int LOCK_REFRESH = 1;
    /**
     * Default lock timeout value.
     */
    private static final int DEFAULT_TIMEOUT = 3600;
    /**
     * Maximum lock timeout.
     */
    private static final int MAX_TIMEOUT = 604800;
    /**
     * Repository of the locks put on single resources.
     * <p>
     * Key : path <br>
     * Value : LockInfo
     */
    private final Hashtable<String, LockInfo> resourceLocks = new Hashtable<>();
    /**
     * Repository of the lock-null resources.
     * <p>
     * Key : path of the collection containing the lock-null resource<br>
     * Value : Vector of lock-null resource which are members of the collection.
     * Each element of the Vector is the path associated with the lock-null
     * resource.
     */
    private final Hashtable<String, Vector<String>> lockNullResources
            = new Hashtable<>();

    // ----------------------------------------------------- Instance Variables
    /**
     * Vector of the heritable locks.
     * <p>
     * Key : path <br>
     * Value : LockInfo
     */
    private final Vector<LockInfo> collectionLocks = new Vector<>();
    boolean listings = true;
    boolean readOnly = false;
    /**
     * Secret information used to generate reasonably secure lock ids.
     */
    private String secret = "catalina";

    /**
     * Default depth in spec is infinite. Limit depth to 3 by default as
     * infinite depth makes operations very expensive.
     */
    private int maxDepth = 3;

    /**
     * Is access allowed via WebDAV to the special paths (/WEB-INF and
     * /META-INF)?
     */
    private boolean allowSpecialPaths = false;

    // --------------------------------------------------------- Public Methods
    /**
     * Initialize this servlet.
     */
    @Override
    public void init()
            throws ServletException {

        super.init();

        if (getServletConfig().getInitParameter("secret") != null) {
            this.secret = getServletConfig().getInitParameter("secret");
        }

        if (getServletConfig().getInitParameter("maxDepth") != null) {
            this.maxDepth = Integer.parseInt(
                    getServletConfig().getInitParameter("maxDepth"));
        }

        if (getServletConfig().getInitParameter("allowSpecialPaths") != null) {
            this.allowSpecialPaths = Boolean.parseBoolean(
                    getServletConfig().getInitParameter("allowSpecialPaths"));
        }
    }

    // ------------------------------------------------------ Protected Methods
    /**
     * Return JAXP document builder instance.
     *
     * @return the document builder
     * @throws ServletException document builder creation failed (wrapped
     * <code>ParserConfigurationException</code> exception)
     */
    protected DocumentBuilder getDocumentBuilder()
            throws ServletException {
        final DocumentBuilder documentBuilder;
        final DocumentBuilderFactory documentBuilderFactory;
        try {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setExpandEntityReferences(false);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setEntityResolver(
                    new WebdavResolver(this.getServletContext()));
        } catch (final ParserConfigurationException e) {
            throw new ServletException(sm.getString("webdavservlet.jaxpfailed"));
        }
        return documentBuilder;
    }

    /**
     * Handles the special WebDAV methods.
     */
    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {

        final String path = getRelativePath(req);

        // Block access to special subdirectories.
        // DefaultServlet assumes it services resources from the root of the web app
        // and doesn't add any special path protection
        // WebdavServlet remounts the webapp under a new path, so this check is
        // necessary on all methods (including GET).
        if (isSpecialPath(path)) {
            resp.sendError(WebdavStatus.SC_NOT_FOUND);
            return;
        }

        final String method = req.getMethod();

        if (this.debug > 0) {
            log("[" + method + "] " + path);
        }

        switch (method) {
            case METHOD_PROPFIND:
                doPropfind(req, resp);
                break;
            case METHOD_PROPPATCH:
                doProppatch(req, resp);
                break;
            case METHOD_MKCOL:
                doMkcol(req, resp);
                break;
            case METHOD_COPY:
                doCopy(req, resp);
                break;
            case METHOD_MOVE:
                doMove(req, resp);
                break;
            case METHOD_LOCK:
                doLock(req, resp);
                break;
            case METHOD_UNLOCK:
                doUnlock(req, resp);
                break;
            case METHOD_OPTIONS:
                doOptions(req, resp);
                break;    
            default:
                // DefaultServlet processing
                super.service(req, resp);
                break;
        }

    }

    /**
     * Checks whether a given path refers to a resource under
     * <code>WEB-INF</code> or <code>META-INF</code>.
     *
     * @param path the full path of the resource being accessed
     * @return <code>true</code> if the resource specified is under a special
     * path
     */
    private boolean isSpecialPath(final String path) {
        return !this.allowSpecialPaths && (path.toUpperCase(Locale.ENGLISH).startsWith("/WEB-INF")
                || path.toUpperCase(Locale.ENGLISH).startsWith("/META-INF"));
    }

    protected boolean checkIfHeaders(final HttpServletRequest request,
            final HttpServletResponse response,
            final WebResource resource)
            throws IOException {

        if (!super.checkIfHeaders(request, response, resource)) {
            return false;
        }

        // TODO : Checking the WebDAV If header
        return true;
    }

    /**
     * Override the DefaultServlet implementation and only use the PathInfo. If
     * the ServletPath is non-null, it will be because the WebDAV servlet has
     * been mapped to a url other than /* to configure editing at different url
     * than normal viewing.
     *
     * @param request The servlet request we are processing
     */
    @Override
    protected String getRelativePath(final HttpServletRequest request) {
        return getRelativePath(request, false);
    }

    @Override
    protected String getRelativePath(final HttpServletRequest request, final boolean allowEmptyPath) {
        final String pathInfo;

        if (request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI) != null) {
            // For includes, get the info from the attributes
            pathInfo = (String) request.getAttribute(RequestDispatcher.INCLUDE_PATH_INFO);
        } else {
            pathInfo = request.getPathInfo();
        }

        final StringBuilder result = new StringBuilder();
        if (pathInfo != null) {
            result.append(pathInfo);
        }
        if (result.length() == 0) {
            result.append('/');
        }

        return result.toString();
    }

    private void doDirectoryRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuilder location = new StringBuilder(request.getRequestURI());
        location.append('/');
        if (request.getQueryString() != null) {
            location.append('?');
            location.append(request.getQueryString());
        }

        response.sendRedirect(response.encodeRedirectURL(location.toString()));
    }

    protected void serveResource(HttpServletRequest request, HttpServletResponse response, boolean content, String encoding) throws IOException, ServletException {
        boolean serveContent = content;
        String path = this.getRelativePath(request, true);
        if (this.debug > 0) {
            if (content) {
                this.log("DefaultServlet.serveResource:  Serving resource \'" + path + "\' headers and data");
            } else {
                this.log("DefaultServlet.serveResource:  Serving resource \'" + path + "\' headers only");
            }
        }

        if (path.length() == 0) {
            this.doDirectoryRedirect(request, response);
        } else {
            WebResource resource = this.resources.getResource(path);
            String isError1;
            if (!resource.exists()) {
                isError1 = (String) request.getAttribute("javax.servlet.include.request_uri");
                if (isError1 == null) {
                    isError1 = request.getRequestURI();
                    response.sendError(404, isError1);
                } else {
                    throw new FileNotFoundException(sm.getString("defaultServlet.missingResource", isError1));
                }
            } else if (!resource.canRead()) {
                isError1 = (String) request.getAttribute("javax.servlet.include.request_uri");
                if (isError1 == null) {
                    isError1 = request.getRequestURI();
                    response.sendError(403, isError1);
                } else {
                    throw new FileNotFoundException(sm.getString("defaultServlet.missingResource", isError1));
                }
            } else if (resource.isFile() && (path.endsWith("/") || path.endsWith("\\"))) {
                isError1 = (String) request.getAttribute("javax.servlet.include.request_uri");
                if (isError1 == null) {
                    isError1 = request.getRequestURI();
                }

                response.sendError(404, isError1);
            } else {
                boolean isError = response.getStatus() >= 400;
                boolean included = false;
                if (resource.isFile()) {
                    included = request.getAttribute("javax.servlet.include.context_path") != null;
                    if (!included && !isError && !this.checkIfHeaders(request, response, resource)) {
                        return;
                    }
                }

                String contentType = resource.getMimeType();
                if (contentType == null) {
                    contentType = this.getServletContext().getMimeType(resource.getName());
                    resource.setMimeType(contentType);
                }

                String eTag = null;
                String lastModifiedHttp = null;
                if (resource.isFile() && !isError) {
                    eTag = resource.getETag();
                    lastModifiedHttp = resource.getLastModifiedHttp();
                }

                /*
                boolean usingGzippedVersion = false;
                if(this.gzip && !included && resource.isFile() && !path.endsWith(".gz")) {
                    WebResource ranges = this.resources.getResource(path + ".gz");
                    if(ranges.exists() && ranges.isFile()) {
                        Collection contentLength = response.getHeaders("Vary");
                        boolean addRequired = true;
                        Iterator ostream = contentLength.iterator();

                        label279: {
                            String writer;
                            do {
                                if(!ostream.hasNext()) {
                                    break label279;
                                }

                                writer = (String)ostream.next();
                            } while(!"*".equals(writer) && !"accept-encoding".equalsIgnoreCase(writer));

                            addRequired = false;
                        }

                        if(addRequired) {
                            response.addHeader("Vary", "accept-encoding");
                        }

                        if(this.checkIfGzip(request)) {
                            response.addHeader("Content-Encoding", "gzip");
                            resource = ranges;
                            usingGzippedVersion = true;
                        }
                    }
                }
                 */
                ArrayList ranges1 = null;
                long contentLength1 = -1L;
                if (resource.isDirectory()) {
                    if (!path.endsWith("/")) {
                        this.doDirectoryRedirect(request, response);
                        return;
                    }

                    if (!this.listings) {
                        response.sendError(404, request.getRequestURI());
                        return;
                    }

                    contentType = "text/html;charset=UTF-8";
                } else {
                    if (!isError) {
                        if (this.useAcceptRanges) {
                            response.setHeader("Accept-Ranges", "bytes");
                        }

                        ranges1 = this.parseRange(request, response, resource);
                        response.setHeader("ETag", eTag);
                        response.setHeader("Last-Modified", lastModifiedHttp);
                    }

                    contentLength1 = resource.getContentLength();
                    if (contentLength1 == 0L) {
                        serveContent = false;
                    }
                }

                ServletOutputStream ostream1 = null;
                PrintWriter writer1 = null;
                if (serveContent) {
                    try {
                        ostream1 = response.getOutputStream();
                    } catch (IllegalStateException var29) {
                        /*
                        if(usingGzippedVersion || contentType != null && !contentType.startsWith("text") && !contentType.endsWith("xml") && !contentType.contains("/javascript")) {
                            throw var29;
                        }
                         */

                        writer1 = response.getWriter();
                        ranges1 = FULL;
                    }
                }

                Object r = response;

                long contentWritten;
                for (contentWritten = 0L; r instanceof ServletResponseWrapper; r = ((ServletResponseWrapper) r).getResponse()) {
                }

                if (r instanceof ResponseFacade) {
                    contentWritten = ((ResponseFacade) r).getContentWritten();
                }

                if (contentWritten > 0L) {
                    ranges1 = FULL;
                }

                if (!resource.isDirectory() && !isError && (ranges1 != null && !ranges1.isEmpty() || request.getHeader("Range") != null) && ranges1 != FULL) {
                    if (ranges1 == null || ranges1.isEmpty()) {
                        return;
                    }

                    response.setStatus(206);
                    if (ranges1.size() == 1) {
                        DefaultServlet.Range range1 = (DefaultServlet.Range) ranges1.get(0);
                        response.addHeader("Content-Range", "bytes " + range1.start + "-" + range1.end + "/" + range1.length);
                        long length1 = range1.end - range1.start + 1L;
                        response.setContentLengthLong(length1);
                        if (contentType != null) {
                            if (this.debug > 0) {
                                this.log("DefaultServlet.serveFile:  contentType=\'" + contentType + "\'");
                            }

                            response.setContentType(contentType);
                        }

                        if (serveContent) {
                            try {
                                response.setBufferSize(this.output);
                            } catch (IllegalStateException var27) {
                            }

                            if (ostream1 == null) {
                                throw new IllegalStateException();
                            }

                            if (!this.checkSendfile(request, response, resource, range1.end - range1.start + 1L, range1)) {
                                this.copy(resource, ostream1, range1);
                            }
                        }
                    } else {
                        response.setContentType("multipart/byteranges; boundary=CATALINA_MIME_BOUNDARY");
                        if (serveContent) {
                            try {
                                response.setBufferSize(this.output);
                            } catch (IllegalStateException ignored) {
                            }

                            if (ostream1 == null) {
                                throw new IllegalStateException();
                            }

                            this.copy(resource, ostream1, ranges1.iterator(), contentType);
                        }
                    }
                } else {
                    if (contentType != null) {
                        if (this.debug > 0) {
                            this.log("DefaultServlet.serveFile:  contentType=\'" + contentType + "\'");
                        }

                        response.setContentType(contentType);
                    }

                    if (resource.isFile() && contentLength1 >= 0L && (!serveContent || ostream1 != null)) {
                        if (this.debug > 0) {
                            this.log("DefaultServlet.serveFile:  contentLength=" + contentLength1);
                        }

                        if (contentWritten == 0L) {
                            response.setContentLengthLong(contentLength1);
                        }
                    }

                    if (serveContent) {
                        try {
                            response.setBufferSize(this.output);
                        } catch (IllegalStateException ignored) {
                        }

                        InputStream range = null;
                        if (ostream1 == null) {
                            if (resource.isDirectory()) {
                                range = this.render(this.getPathPrefix(request), resource, encoding);
                            } else {
                                range = resource.getInputStream();
                            }

                            //this.copy(resource, range, writer1, encoding);
                        } else {
                            if (resource.isDirectory()) {
                                range = this.render(this.getPathPrefix(request), resource, encoding);
                            } else if (!this.checkSendfile(request, response, resource, contentLength1, null)) {
                                byte[] length = resource.getContent();
                                if (length == null) {
                                    range = resource.getInputStream();
                                } else {
                                    ostream1.write(length);
                                }
                            }

                            if (range != null) {
                                //this.copy(resource, range, ostream1);
                            }
                        }
                    }
                }

            }
        }
    }

    /**
     * Determines the prefix for standard directory GET listings.
     */
    @Override
    protected String getPathPrefix(final HttpServletRequest request) {
        // Repeat the servlet path (e.g. /webdav/) in the listing path
        String contextPath = request.getContextPath();
        if (request.getServletPath() != null) {
            contextPath = contextPath + request.getServletPath();
        }
        return contextPath;
    }

    @Override
    protected void doGet(final HttpServletRequest request,
            final HttpServletResponse response)
            throws IOException, ServletException {

        // Serve the requested resource, including the data content
        serveResource(request, response, true, this.fileEncoding);

    }

    @Override
    protected void doHead(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        // Serve the requested resource, without the data content unless we are
        // being included since in that case the content needs to be provided so
        // the correct content length is reported for the including resource
        final boolean serveContent = DispatcherType.INCLUDE.equals(request.getDispatcherType());
        serveResource(request, response, serveContent, this.fileEncoding);
    }

    /**
     * OPTIONS Method.
     *
     * @param req The Servlet request
     * @param resp The Servlet response
     * @throws ServletException If an error occurs
     * @throws IOException If an IO error occurs
     */
    @Override
    protected void doOptions(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {

        resp.addHeader("DAV", "1,2");

        final StringBuilder methodsAllowed = determineMethodsAllowed2(req);

        resp.addHeader("Allow", methodsAllowed.toString());
        resp.addHeader("MS-Author-Via", "DAV");
    }

    /**
     * PROPFIND Method.
     *
     * @param req The Servlet request
     * @param resp The Servlet response
     * @throws ServletException If an error occurs
     * @throws IOException If an IO error occurs
     */
    protected void doPropfind(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {

        if (!this.listings) {
            // Get allowed methods
            final StringBuilder methodsAllowed = determineMethodsAllowed2(req);

            resp.addHeader("Allow", methodsAllowed.toString());
            resp.sendError(WebdavStatus.SC_METHOD_NOT_ALLOWED);
            return;
        }

        String path = getRelativePath(req);
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // Properties which are to be displayed.
        Vector<String> properties = null;
        // Propfind depth
        int depth = this.maxDepth;
        // Propfind type
        int type = FIND_ALL_PROP;

        final String depthStr = req.getHeader("Depth");

        if (depthStr == null) {
            depth = this.maxDepth;
        } else {
            switch (depthStr) {
                case "0":
                    depth = 0;
                    break;
                case "1":
                    depth = 1;
                    break;
                case "infinity":
                    depth = this.maxDepth;
                    break;
            }
        }

        System.out.println(path + " -> " + depth);
        
        Node propNode = null;

        if (req.getContentLengthLong() > 0) {
            final DocumentBuilder documentBuilder = getDocumentBuilder();

            try {
                final Document document = documentBuilder.parse(new InputSource(req.getInputStream()));

                // Get the root element of the document
                final Element rootElement = document.getDocumentElement();
                final NodeList childList = rootElement.getChildNodes();

                for (int i = 0; i < childList.getLength(); i++) {
                    final Node currentNode = childList.item(i);
                    switch (currentNode.getNodeType()) {
                        case Node.TEXT_NODE:
                            break;
                        case Node.ELEMENT_NODE:
                            if (currentNode.getNodeName().endsWith("prop")) {
                                type = FIND_BY_PROPERTY;
                                propNode = currentNode;
                            }
                            if (currentNode.getNodeName().endsWith("propname")) {
                                type = FIND_PROPERTY_NAMES;
                            }
                            if (currentNode.getNodeName().endsWith("allprop")) {
                                type = FIND_ALL_PROP;
                            }
                            break;
                    }
                }
            } catch (final SAXException | IOException e) {
                // Something went wrong - bad request
                resp.sendError(WebdavStatus.SC_BAD_REQUEST);
                return;
            }
        }

        if (type == FIND_BY_PROPERTY) {
            properties = new Vector<>();
            // propNode must be non-null if type == FIND_BY_PROPERTY
            final NodeList childList = propNode.getChildNodes();

            for (int i = 0; i < childList.getLength(); i++) {
                final Node currentNode = childList.item(i);
                switch (currentNode.getNodeType()) {
                    case Node.TEXT_NODE:
                        break;
                    case Node.ELEMENT_NODE:
                        final String nodeName = currentNode.getNodeName();
                        final String propertyName;
                        if (nodeName.indexOf(':') != -1) {
                            propertyName = nodeName.substring(nodeName.indexOf(':') + 1);
                        } else {
                            propertyName = nodeName;
                        }
                        // href is a live property which is handled differently
                        properties.addElement(propertyName);
                        break;
                }
            }

        }

        WebResource resource = this.resources.getResource(path);

        if (!resource.exists()) {
            final int slash = path.lastIndexOf('/');
            if (slash != -1) {
                final String parentPath = path.substring(0, slash);
                final Vector<String> currentLockNullResources
                        = this.lockNullResources.get(parentPath);
                if (currentLockNullResources != null) {
                    final Enumeration<String> lockNullResourcesList
                            = currentLockNullResources.elements();
                    while (lockNullResourcesList.hasMoreElements()) {
                        final String lockNullPath
                                = lockNullResourcesList.nextElement();
                        if (lockNullPath.equals(path)) {
                            resp.setStatus(WebdavStatus.SC_MULTI_STATUS);
                            resp.setContentType("text/xml; charset=UTF-8");
                            // Create multistatus object
                            final XMLWriter generatedXML
                                    = new XMLWriter(resp.getWriter());
                            generatedXML.writeXMLHeader();
                            generatedXML.writeElement("D", DEFAULT_NAMESPACE,
                                    "multistatus", XMLWriter.OPENING);
                            parseLockNullProperties(req, generatedXML, lockNullPath, type,
                                    properties);
                            generatedXML.writeElement("D", "multistatus",
                                    XMLWriter.CLOSING);
                            generatedXML.sendData();
                            return;
                        }
                    }
                }
            }
        }

        if (!resource.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, path);
            return;
        }

        resp.setStatus(WebdavStatus.SC_MULTI_STATUS);

        resp.setContentType("text/xml; charset=UTF-8");

        // Create multistatus object
        final XMLWriter generatedXML = new XMLWriter(resp.getWriter());
        generatedXML.writeXMLHeader();

        generatedXML.writeElement("D", DEFAULT_NAMESPACE, "multistatus",
                XMLWriter.OPENING);

        if (depth == 0) {
            parseProperties(req, generatedXML, path, type,
                    properties);
        } else {
            // The stack always contains the object of the current level
            Stack<String> stack = new Stack<>();
            stack.push(path);

            // Stack of the objects one level below
            Stack<String> stackBelow = new Stack<>();

            while ((!stack.isEmpty()) && (depth >= 0)) {

                final String currentPath = stack.pop();
                parseProperties(req, generatedXML, currentPath,
                        type, properties);

                resource = this.resources.getResource(currentPath);

                if (resource.isDirectory() && (depth > 0)) {

                    final String[] entries = this.resources.list(currentPath);
                    for (final String entry : entries) {
                        String newPath = currentPath;
                        if (!(newPath.endsWith("/"))) {
                            newPath += "/";
                        }
                        newPath += entry;
                        stackBelow.push(newPath);
                    }

                    // Displaying the lock-null resources present in that
                    // collection
                    String lockPath = currentPath;
                    if (lockPath.endsWith("/")) {
                        lockPath
                                = lockPath.substring(0, lockPath.length() - 1);
                    }
                    final Vector<String> currentLockNullResources
                            = this.lockNullResources.get(lockPath);
                    if (currentLockNullResources != null) {
                        final Enumeration<String> lockNullResourcesList
                                = currentLockNullResources.elements();
                        while (lockNullResourcesList.hasMoreElements()) {
                            final String lockNullPath
                                    = lockNullResourcesList.nextElement();
                            parseLockNullProperties(req, generatedXML, lockNullPath, type,
                                    properties);
                        }
                    }

                }

                if (stack.isEmpty()) {
                    depth--;
                    stack = stackBelow;
                    stackBelow = new Stack<>();
                }

                System.out.println(generatedXML.toString());
                generatedXML.sendData();

            }
        }

        generatedXML.writeElement("D", "multistatus", XMLWriter.CLOSING);

        System.out.println(generatedXML.toString());
        generatedXML.sendData();

    }

    /**
     * PROPPATCH Method.
     *
     * @param req The Servlet request
     * @param resp The Servlet response
     * @throws IOException If an IO error occurs
     */
    protected void doProppatch(final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {

        if (this.readOnly) {
            resp.sendError(WebdavStatus.SC_FORBIDDEN);
            return;
        }

        if (isLocked(req)) {
            resp.sendError(WebdavStatus.SC_LOCKED);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);

    }

    /**
     * MKCOL Method.
     *
     * @param req The Servlet request
     * @param resp The Servlet response
     * @throws ServletException If an error occurs
     * @throws IOException If an IO error occurs
     */
    protected void doMkcol(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {

        if (this.readOnly) {
            resp.sendError(WebdavStatus.SC_FORBIDDEN);
            return;
        }

        if (isLocked(req)) {
            resp.sendError(WebdavStatus.SC_LOCKED);
            return;
        }

        final String path = getRelativePath(req);

        final WebResource resource = this.resources.getResource(path);

        // Can't create a collection if a resource already exists at the given
        // path
        if (resource.exists()) {
            // Get allowed methods
            final StringBuilder methodsAllowed = determineMethodsAllowed2(req);

            resp.addHeader("Allow", methodsAllowed.toString());

            resp.sendError(WebdavStatus.SC_METHOD_NOT_ALLOWED);
            return;
        }

        if (req.getContentLengthLong() > 0) {
            final DocumentBuilder documentBuilder = getDocumentBuilder();
            try {
                // Document document =
                documentBuilder.parse(new InputSource(req.getInputStream()));
                // TODO : Process this request body
                resp.sendError(WebdavStatus.SC_NOT_IMPLEMENTED);
                return;

            } catch (final SAXException saxe) {
                // Parse error - assume invalid content
                resp.sendError(WebdavStatus.SC_UNSUPPORTED_MEDIA_TYPE);
                return;
            }
        }

        if (this.resources.mkdir(path)) {
            resp.setStatus(WebdavStatus.SC_CREATED);
            // Removing any lock-null resource which would be present
            this.lockNullResources.remove(path);
        } else {
            resp.sendError(WebdavStatus.SC_CONFLICT,
                    WebdavStatus.getStatusText(WebdavStatus.SC_CONFLICT));
        }
    }

    /**
     * DELETE Method.
     *
     * @param req The Servlet request
     * @param resp The Servlet response
     * @throws ServletException If an error occurs
     * @throws IOException If an IO error occurs
     */
    @Override
    protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {

        if (this.readOnly) {
            resp.sendError(WebdavStatus.SC_FORBIDDEN);
            return;
        }

        if (isLocked(req)) {
            resp.sendError(WebdavStatus.SC_LOCKED);
            return;
        }

        deleteResource(req, resp);

    }

    /**
     * Process a PUT request for the specified resource.
     *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet-specified error occurs
     */
    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {

        if (isLocked(req)) {
            resp.sendError(WebdavStatus.SC_LOCKED);
            return;
        }

        final String path = this.getRelativePath(req);
        final WebResource resource = this.resources.getResource(path);
        final DefaultServlet.Range range = this.parseContentRange(req, resp);
        Object resourceInputStream = null;

        try {
            if (range != null) {
                final File contentFile = this.executePartialPut(req, range, path);
                resourceInputStream = new FileInputStream(contentFile);
            } else {
                resourceInputStream = req.getInputStream();
            }

            if (this.resources.write(path, (InputStream) resourceInputStream, true)) {
                if (resource.exists()) {
                    resp.setStatus(204);
                } else {
                    resp.setStatus(201);
                }
            } else {
                resp.sendError(409);
            }
        } finally {
            if (resourceInputStream != null) {
                try {
                    ((InputStream) resourceInputStream).close();
                } catch (final IOException ignored) {
                }
            }

        }

        // Removing any lock-null resource which would be present
        this.lockNullResources.remove(path);

    }

    /**
     * COPY Method.
     *
     * @param req The Servlet request
     * @param resp The Servlet response
     * @throws IOException If an IO error occurs
     */
    protected void doCopy(final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {

        if (this.readOnly) {
            resp.sendError(WebdavStatus.SC_FORBIDDEN);
            return;
        }

        copyResource(req, resp);

    }

    /**
     * MOVE Method.
     *
     * @param req The Servlet request
     * @param resp The Servlet response
     * @throws IOException If an IO error occurs
     */
    protected void doMove(final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {

        if (this.readOnly) {
            resp.sendError(WebdavStatus.SC_FORBIDDEN);
            return;
        }

        if (isLocked(req)) {
            resp.sendError(WebdavStatus.SC_LOCKED);
            return;
        }

        final String path = getRelativePath(req);

        if (copyResource(req, resp)) {
            deleteResource(path, req, resp, false);
        }

    }

    /**
     * LOCK Method.
     *
     * @param req The Servlet request
     * @param resp The Servlet response
     * @throws ServletException If an error occurs
     * @throws IOException If an IO error occurs
     */
    protected void doLock(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {

        if (this.readOnly) {
            resp.sendError(WebdavStatus.SC_FORBIDDEN);
            return;
        }

        if (isLocked(req)) {
            resp.sendError(WebdavStatus.SC_LOCKED);
            return;
        }

        LockInfo lock = new LockInfo();

        // Parsing lock request
        // Parsing depth header
        final String depthStr = req.getHeader("Depth");

        if (depthStr == null) {
            lock.depth = this.maxDepth;
        } else if (depthStr.equals("0")) {
            lock.depth = 0;
        } else {
            lock.depth = this.maxDepth;
        }

        // Parsing timeout header
        int lockDuration;
        String lockDurationStr = req.getHeader("Timeout");
        if (lockDurationStr == null) {
            lockDuration = DEFAULT_TIMEOUT;
        } else {
            final int commaPos = lockDurationStr.indexOf(',');
            // If multiple timeouts, just use the first
            if (commaPos != -1) {
                lockDurationStr = lockDurationStr.substring(0, commaPos);
            }
            if (lockDurationStr.startsWith("Second-")) {
                lockDuration = Integer.parseInt(lockDurationStr.substring(7));
            } else if (lockDurationStr.equalsIgnoreCase("infinity")) {
                lockDuration = MAX_TIMEOUT;
            } else {
                try {
                    lockDuration = Integer.parseInt(lockDurationStr);
                } catch (final NumberFormatException e) {
                    lockDuration = MAX_TIMEOUT;
                }
            }
            if (lockDuration == 0) {
                lockDuration = DEFAULT_TIMEOUT;
            }
            if (lockDuration > MAX_TIMEOUT) {
                lockDuration = MAX_TIMEOUT;
            }
        }
        lock.expiresAt = System.currentTimeMillis() + (lockDuration * 1000);

        int lockRequestType = LOCK_CREATION;

        Node lockInfoNode = null;

        final DocumentBuilder documentBuilder = getDocumentBuilder();

        try {
            final Document document = documentBuilder.parse(new InputSource(req.getInputStream()));

            // Get the root element of the document
            lockInfoNode = document.getDocumentElement();
        } catch (final IOException | SAXException e) {
            lockRequestType = LOCK_REFRESH;
        }

        if (lockInfoNode != null) {

            // Reading lock information
            NodeList childList = lockInfoNode.getChildNodes();
            StringWriter strWriter;
            DOMWriter domWriter;

            Node lockScopeNode = null;
            Node lockTypeNode = null;
            Node lockOwnerNode = null;

            for (int i = 0; i < childList.getLength(); i++) {
                final Node currentNode = childList.item(i);
                switch (currentNode.getNodeType()) {
                    case Node.TEXT_NODE:
                        break;
                    case Node.ELEMENT_NODE:
                        final String nodeName = currentNode.getNodeName();
                        if (nodeName.endsWith("lockscope")) {
                            lockScopeNode = currentNode;
                        }
                        if (nodeName.endsWith("locktype")) {
                            lockTypeNode = currentNode;
                        }
                        if (nodeName.endsWith("owner")) {
                            lockOwnerNode = currentNode;
                        }
                        break;
                }
            }

            if (lockScopeNode != null) {

                childList = lockScopeNode.getChildNodes();
                for (int i = 0; i < childList.getLength(); i++) {
                    final Node currentNode = childList.item(i);
                    switch (currentNode.getNodeType()) {
                        case Node.TEXT_NODE:
                            break;
                        case Node.ELEMENT_NODE:
                            final String tempScope = currentNode.getNodeName();
                            if (tempScope.indexOf(':') != -1) {
                                lock.scope = tempScope.substring(tempScope.indexOf(':') + 1);
                            } else {
                                lock.scope = tempScope;
                            }
                            break;
                    }
                }

                if (lock.scope == null) {
                    // Bad request
                    resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
                }

            } else {
                // Bad request
                resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
            }

            if (lockTypeNode != null) {

                childList = lockTypeNode.getChildNodes();
                for (int i = 0; i < childList.getLength(); i++) {
                    final Node currentNode = childList.item(i);
                    switch (currentNode.getNodeType()) {
                        case Node.TEXT_NODE:
                            break;
                        case Node.ELEMENT_NODE:
                            final String tempType = currentNode.getNodeName();
                            if (tempType.indexOf(':') != -1) {
                                lock.type
                                        = tempType.substring(tempType.indexOf(':') + 1);
                            } else {
                                lock.type = tempType;
                            }
                            break;
                    }
                }

                if (lock.type == null) {
                    // Bad request
                    resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
                }

            } else {
                // Bad request
                resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
            }

            if (lockOwnerNode != null) {

                childList = lockOwnerNode.getChildNodes();
                for (int i = 0; i < childList.getLength(); i++) {
                    final Node currentNode = childList.item(i);
                    switch (currentNode.getNodeType()) {
                        case Node.TEXT_NODE:
                            lock.owner += currentNode.getNodeValue();
                            break;
                        case Node.ELEMENT_NODE:
                            strWriter = new StringWriter();
                            //domWriter = new DOMWriter(strWriter, true);
                            //domWriter.print(currentNode);
                            lock.owner += strWriter.toString();
                            break;
                    }
                }

                if (lock.owner == null) {
                    // Bad request
                    resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
                }

            } else {
                lock.owner = "";
            }

        }

        final String path = getRelativePath(req);

        lock.path = path;

        final WebResource resource = this.resources.getResource(path);

        Enumeration<LockInfo> locksList;

        if (lockRequestType == LOCK_CREATION) {

            // Generating lock id
            final String lockTokenStr = req.getServletPath() + "-" + lock.type + "-"
                    + lock.scope + "-" + req.getUserPrincipal() + "-"
                    + lock.depth + "-" + lock.owner + "-" + lock.tokens + "-"
                    + lock.expiresAt + "-" + System.currentTimeMillis() + "-"
                    + this.secret;
            final String lockToken = MD5Encoder.encode(ConcurrentMessageDigest.digestMD5(
                    lockTokenStr.getBytes(StandardCharsets.ISO_8859_1)));

            if (resource.isDirectory() && lock.depth == this.maxDepth) {

                // Locking a collection (and all its member resources)
                // Checking if a child resource of this collection is
                // already locked
                final Vector<String> lockPaths = new Vector<>();
                locksList = this.collectionLocks.elements();
                while (locksList.hasMoreElements()) {
                    final LockInfo currentLock = locksList.nextElement();
                    if (currentLock.hasExpired()) {
                        this.resourceLocks.remove(currentLock.path);
                        continue;
                    }
                    if ((currentLock.path.startsWith(lock.path))
                            && ((currentLock.isExclusive())
                            || (lock.isExclusive()))) {
                        // A child collection of this collection is locked
                        lockPaths.addElement(currentLock.path);
                    }
                }
                locksList = this.resourceLocks.elements();
                while (locksList.hasMoreElements()) {
                    final LockInfo currentLock = locksList.nextElement();
                    if (currentLock.hasExpired()) {
                        this.resourceLocks.remove(currentLock.path);
                        continue;
                    }
                    if ((currentLock.path.startsWith(lock.path))
                            && ((currentLock.isExclusive())
                            || (lock.isExclusive()))) {
                        // A child resource of this collection is locked
                        lockPaths.addElement(currentLock.path);
                    }
                }

                if (!lockPaths.isEmpty()) {

                    // One of the child paths was locked
                    // We generate a multistatus error report
                    final Enumeration<String> lockPathsList = lockPaths.elements();

                    resp.setStatus(WebdavStatus.SC_CONFLICT);

                    final XMLWriter generatedXML = new XMLWriter();
                    generatedXML.writeXMLHeader();

                    generatedXML.writeElement("D", DEFAULT_NAMESPACE,
                            "multistatus", XMLWriter.OPENING);

                    while (lockPathsList.hasMoreElements()) {
                        generatedXML.writeElement("D", "response",
                                XMLWriter.OPENING);
                        generatedXML.writeElement("D", "href",
                                XMLWriter.OPENING);
                        generatedXML.writeText(lockPathsList.nextElement());
                        generatedXML.writeElement("D", "href",
                                XMLWriter.CLOSING);
                        generatedXML.writeElement("D", "status",
                                XMLWriter.OPENING);
                        generatedXML
                                .writeText("HTTP/1.1 " + WebdavStatus.SC_LOCKED
                                        + " " + WebdavStatus
                                        .getStatusText(WebdavStatus.SC_LOCKED));
                        generatedXML.writeElement("D", "status",
                                XMLWriter.CLOSING);

                        generatedXML.writeElement("D", "response",
                                XMLWriter.CLOSING);
                    }

                    generatedXML.writeElement("D", "multistatus",
                            XMLWriter.CLOSING);

                    final Writer writer = resp.getWriter();
                    writer.write(generatedXML.toString());
                    writer.close();

                    return;

                }

                boolean addLock = true;

                // Checking if there is already a shared lock on this path
                locksList = this.collectionLocks.elements();
                while (locksList.hasMoreElements()) {

                    final LockInfo currentLock = locksList.nextElement();
                    if (currentLock.path.equals(lock.path)) {

                        if (currentLock.isExclusive()) {
                            resp.sendError(WebdavStatus.SC_LOCKED);
                            return;
                        } else if (lock.isExclusive()) {
                            resp.sendError(WebdavStatus.SC_LOCKED);
                            return;
                        }

                        currentLock.tokens.addElement(lockToken);
                        lock = currentLock;
                        addLock = false;

                    }

                }

                if (addLock) {
                    lock.tokens.addElement(lockToken);
                    this.collectionLocks.addElement(lock);
                }

            } else {

                // Locking a single resource
                // Retrieving an already existing lock on that resource
                final LockInfo presentLock = this.resourceLocks.get(lock.path);
                if (presentLock != null) {

                    if ((presentLock.isExclusive()) || (lock.isExclusive())) {
                        // If either lock is exclusive, the lock can't be
                        // granted
                        resp.sendError(WebdavStatus.SC_PRECONDITION_FAILED);
                        return;
                    } else {
                        presentLock.tokens.addElement(lockToken);
                        lock = presentLock;
                    }

                } else {

                    lock.tokens.addElement(lockToken);
                    this.resourceLocks.put(lock.path, lock);

                    // Checking if a resource exists at this path
                    if (!resource.exists()) {

                        // "Creating" a lock-null resource
                        final int slash = lock.path.lastIndexOf('/');
                        final String parentPath = lock.path.substring(0, slash);

                        Vector<String> lockNulls
                                = this.lockNullResources.get(parentPath);
                        if (lockNulls == null) {
                            lockNulls = new Vector<>();
                            this.lockNullResources.put(parentPath, lockNulls);
                        }

                        lockNulls.addElement(lock.path);

                    }
                    // Add the Lock-Token header as by RFC 2518 8.10.1
                    // - only do this for newly created locks
                    resp.addHeader("Lock-Token", "<opaquelocktoken:"
                            + lockToken + ">");
                }

            }

        }

        if (lockRequestType == LOCK_REFRESH) {

            String ifHeader = req.getHeader("If");
            if (ifHeader == null) {
                ifHeader = "";
            }

            // Checking resource locks
            LockInfo toRenew = this.resourceLocks.get(path);
            Enumeration<String> tokenList;

            if (toRenew != null) {
                // At least one of the tokens of the locks must have been given
                tokenList = toRenew.tokens.elements();
                while (tokenList.hasMoreElements()) {
                    final String token = tokenList.nextElement();
                    if (ifHeader.contains(token)) {
                        toRenew.expiresAt = lock.expiresAt;
                        lock = toRenew;
                    }
                }
            }

            // Checking inheritable collection locks
            final Enumeration<LockInfo> collectionLocksList
                    = this.collectionLocks.elements();
            while (collectionLocksList.hasMoreElements()) {
                toRenew = collectionLocksList.nextElement();
                if (path.equals(toRenew.path)) {

                    tokenList = toRenew.tokens.elements();
                    while (tokenList.hasMoreElements()) {
                        final String token = tokenList.nextElement();
                        if (ifHeader.contains(token)) {
                            toRenew.expiresAt = lock.expiresAt;
                            lock = toRenew;
                        }
                    }

                }
            }

        }

        // Set the status, then generate the XML response containing
        // the lock information
        final XMLWriter generatedXML = new XMLWriter();
        generatedXML.writeXMLHeader();
        generatedXML.writeElement("D", DEFAULT_NAMESPACE, "prop",
                XMLWriter.OPENING);

        generatedXML.writeElement("D", "lockdiscovery", XMLWriter.OPENING);

        lock.toXML(generatedXML);

        generatedXML.writeElement("D", "lockdiscovery", XMLWriter.CLOSING);

        generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);

        resp.setStatus(WebdavStatus.SC_OK);
        resp.setContentType("text/xml; charset=UTF-8");
        final Writer writer = resp.getWriter();
        writer.write(generatedXML.toString());
        writer.close();

    }

    /**
     * UNLOCK Method.
     *
     * @param req The Servlet request
     * @param resp The Servlet response
     * @throws IOException If an IO error occurs
     */
    protected void doUnlock(final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {

        if (this.readOnly) {
            resp.sendError(WebdavStatus.SC_FORBIDDEN);
            return;
        }

        if (isLocked(req)) {
            resp.sendError(WebdavStatus.SC_LOCKED);
            return;
        }

        final String path = getRelativePath(req);

        String lockTokenHeader = req.getHeader("Lock-Token");
        if (lockTokenHeader == null) {
            lockTokenHeader = "";
        }

        // Checking resource locks
        LockInfo lock = this.resourceLocks.get(path);
        Enumeration<String> tokenList;
        if (lock != null) {

            // At least one of the tokens of the locks must have been given
            tokenList = lock.tokens.elements();
            while (tokenList.hasMoreElements()) {
                final String token = tokenList.nextElement();
                if (lockTokenHeader.contains(token)) {
                    lock.tokens.removeElement(token);
                }
            }

            if (lock.tokens.isEmpty()) {
                this.resourceLocks.remove(path);
                // Removing any lock-null resource which would be present
                this.lockNullResources.remove(path);
            }

        }

        // Checking inheritable collection locks
        final Enumeration<LockInfo> collectionLocksList = this.collectionLocks.elements();
        while (collectionLocksList.hasMoreElements()) {
            lock = collectionLocksList.nextElement();
            if (path.equals(lock.path)) {

                tokenList = lock.tokens.elements();
                while (tokenList.hasMoreElements()) {
                    final String token = tokenList.nextElement();
                    if (lockTokenHeader.contains(token)) {
                        lock.tokens.removeElement(token);
                        break;
                    }
                }

                if (lock.tokens.isEmpty()) {
                    this.collectionLocks.removeElement(lock);
                    // Removing any lock-null resource which would be present
                    this.lockNullResources.remove(path);
                }

            }
        }

        resp.setStatus(WebdavStatus.SC_NO_CONTENT);

    }

    // -------------------------------------------------------- Private Methods
    /**
     * Check to see if a resource is currently write locked. The method will
     * look at the "If" header to make sure the client has give the appropriate
     * lock tokens.
     *
     * @param req Servlet request
     * @return <code>true</code> if the resource is locked (and no appropriate
     * lock token has been found for at least one of the non-shared locks which
     * are present on the resource).
     */
    private boolean isLocked(final HttpServletRequest req) {

        final String path = getRelativePath(req);

        String ifHeader = req.getHeader("If");
        if (ifHeader == null) {
            ifHeader = "";
        }

        String lockTokenHeader = req.getHeader("Lock-Token");
        if (lockTokenHeader == null) {
            lockTokenHeader = "";
        }

        return isLocked(path, ifHeader + lockTokenHeader);

    }

    /**
     * Check to see if a resource is currently write locked.
     *
     * @param path Path of the resource
     * @param ifHeader "If" HTTP header which was included in the request
     * @return <code>true</code> if the resource is locked (and no appropriate
     * lock token has been found for at least one of the non-shared locks which
     * are present on the resource).
     */
    private boolean isLocked(final String path, final String ifHeader) {

        // Checking resource locks
        LockInfo lock = this.resourceLocks.get(path);
        Enumeration<String> tokenList;
        if ((lock != null) && (lock.hasExpired())) {
            this.resourceLocks.remove(path);
        } else if (lock != null) {

            // At least one of the tokens of the locks must have been given
            tokenList = lock.tokens.elements();
            boolean tokenMatch = false;
            while (tokenList.hasMoreElements()) {
                final String token = tokenList.nextElement();
                if (ifHeader.contains(token)) {
                    tokenMatch = true;
                    break;
                }
            }
            if (!tokenMatch) {
                return true;
            }

        }

        // Checking inheritable collection locks
        final Enumeration<LockInfo> collectionLocksList = this.collectionLocks.elements();
        while (collectionLocksList.hasMoreElements()) {
            lock = collectionLocksList.nextElement();
            if (lock.hasExpired()) {
                this.collectionLocks.removeElement(lock);
            } else if (path.startsWith(lock.path)) {

                tokenList = lock.tokens.elements();
                boolean tokenMatch = false;
                while (tokenList.hasMoreElements()) {
                    final String token = tokenList.nextElement();
                    if (ifHeader.contains(token)) {
                        tokenMatch = true;
                        break;
                    }
                }
                if (!tokenMatch) {
                    return true;
                }

            }
        }

        return false;

    }

    /**
     * Copy a resource.
     *
     * @param req Servlet request
     * @param resp Servlet response
     * @return boolean true if the copy is successful
     * @throws IOException If an IO error occurs
     */
    private boolean copyResource(final HttpServletRequest req,
            final HttpServletResponse resp)
            throws IOException {

        // Parsing destination header
        String destinationPath = req.getHeader("Destination");

        if (destinationPath == null) {
            resp.sendError(WebdavStatus.SC_BAD_REQUEST);
            return false;
        }

        // Remove url encoding from destination
        destinationPath = UDecoder.URLDecode(destinationPath, Charset.forName("UTF8"));

        final int protocolIndex = destinationPath.indexOf("://");
        if (protocolIndex >= 0) {
            // if the Destination URL contains the protocol, we can safely
            // trim everything upto the first "/" character after "://"
            final int firstSeparator
                    = destinationPath.indexOf('/', protocolIndex + 4);
            if (firstSeparator < 0) {
                destinationPath = "/";
            } else {
                destinationPath = destinationPath.substring(firstSeparator);
            }
        } else {
            final String hostName = req.getServerName();
            if ((hostName != null) && (destinationPath.startsWith(hostName))) {
                destinationPath = destinationPath.substring(hostName.length());
            }

            final int portIndex = destinationPath.indexOf(':');
            if (portIndex >= 0) {
                destinationPath = destinationPath.substring(portIndex);
            }

            if (destinationPath.startsWith(":")) {
                final int firstSeparator = destinationPath.indexOf('/');
                if (firstSeparator < 0) {
                    destinationPath = "/";
                } else {
                    destinationPath
                            = destinationPath.substring(firstSeparator);
                }
            }
        }

        // Normalise destination path (remove '.' and '..')
        destinationPath = RequestUtil.normalize(destinationPath);

        final String contextPath = req.getContextPath();
        if ((contextPath != null)
                && (destinationPath.startsWith(contextPath))) {
            destinationPath = destinationPath.substring(contextPath.length());
        }

        final String pathInfo = req.getPathInfo();
        if (pathInfo != null) {
            final String servletPath = req.getServletPath();
            if ((servletPath != null)
                    && (destinationPath.startsWith(servletPath))) {
                destinationPath = destinationPath
                        .substring(servletPath.length());
            }
        }

        if (this.debug > 0) {
            log("Dest path :" + destinationPath);
        }

        // Check destination path to protect special subdirectories
        if (isSpecialPath(destinationPath)) {
            resp.sendError(WebdavStatus.SC_FORBIDDEN);
            return false;
        }

        final String path = getRelativePath(req);

        if (destinationPath.equals(path)) {
            resp.sendError(WebdavStatus.SC_FORBIDDEN);
            return false;
        }

        // Parsing overwrite header
        boolean overwrite = true;
        final String overwriteHeader = req.getHeader("Overwrite");

        if (overwriteHeader != null) {
            overwrite = overwriteHeader.equalsIgnoreCase("T");
        }

        // Overwriting the destination
        final WebResource destination = this.resources.getResource(destinationPath);

        if (overwrite) {
            // Delete destination resource, if it exists
            if (destination.exists()) {
                if (!deleteResource(destinationPath, req, resp, true)) {
                    return false;
                }
            } else {
                resp.setStatus(WebdavStatus.SC_CREATED);
            }
        } else // If the destination exists, then it's a conflict
        if (destination.exists()) {
            resp.sendError(WebdavStatus.SC_PRECONDITION_FAILED);
            return false;
        }

        // Copying source to destination
        final Hashtable<String, Integer> errorList = new Hashtable<>();

        final boolean result = copyResource(errorList, path, destinationPath);

        if ((!result) || (!errorList.isEmpty())) {
            if (errorList.size() == 1) {
                resp.sendError(errorList.elements().nextElement());
            } else {
                sendReport(req, resp, errorList);
            }
            return false;
        }

        // Copy was successful
        if (destination.exists()) {
            resp.setStatus(WebdavStatus.SC_NO_CONTENT);
        } else {
            resp.setStatus(WebdavStatus.SC_CREATED);
        }

        // Removing any lock-null resource which would be present at
        // the destination path
        this.lockNullResources.remove(destinationPath);

        return true;
    }

    /**
     * Copy a collection.
     *
     * @param errorList Hashtable containing the list of errors which occurred
     * during the copy operation
     * @param source Path of the resource to be copied
     * @param dest Destination path
     * @return <code>true</code> if the copy was successful
     */
    private boolean copyResource(final Hashtable<String, Integer> errorList,
            final String source, final String dest) {

        if (this.debug > 1) {
            log("Copy: " + source + " To: " + dest);
        }

        final WebResource sourceResource = this.resources.getResource(source);

        if (sourceResource.isDirectory()) {
            if (!this.resources.mkdir(dest)) {
                final WebResource destResource = this.resources.getResource(dest);
                if (!destResource.isDirectory()) {
                    errorList.put(dest, WebdavStatus.SC_CONFLICT);
                    return false;
                }
            }

            final String[] entries = this.resources.list(source);
            for (final String entry : entries) {
                String childDest = dest;
                if (!childDest.equals("/")) {
                    childDest += "/";
                }
                childDest += entry;
                String childSrc = source;
                if (!childSrc.equals("/")) {
                    childSrc += "/";
                }
                childSrc += entry;
                copyResource(errorList, childSrc, childDest);
            }
        } else if (sourceResource.isFile()) {
            final WebResource destResource = this.resources.getResource(dest);
            if (!destResource.exists() && !destResource.getWebappPath().endsWith("/")) {
                final int lastSlash = destResource.getWebappPath().lastIndexOf('/');
                if (lastSlash > 0) {
                    final String parent = destResource.getWebappPath().substring(0, lastSlash);
                    final WebResource parentResource = this.resources.getResource(parent);
                    if (!parentResource.isDirectory()) {
                        errorList.put(source, WebdavStatus.SC_CONFLICT);
                        return false;
                    }
                }
            }
            if (!this.resources.write(dest, sourceResource.getInputStream(),
                    false)) {
                errorList.put(source,
                        WebdavStatus.SC_INTERNAL_SERVER_ERROR);
                return false;
            }
        } else {
            errorList.put(source,
                    WebdavStatus.SC_INTERNAL_SERVER_ERROR);
            return false;
        }
        return true;
    }

    /**
     * Delete a resource.
     *
     * @param req Servlet request
     * @param resp Servlet response
     * @return <code>true</code> if the delete is successful
     * @throws IOException If an IO error occurs
     */
    private boolean deleteResource(final HttpServletRequest req,
            final HttpServletResponse resp)
            throws IOException {

        final String path = getRelativePath(req);

        return deleteResource(path, req, resp, true);

    }

    /**
     * Delete a resource.
     *
     * @param path Path of the resource which is to be deleted
     * @param req Servlet request
     * @param resp Servlet response
     * @param setStatus Should the response status be set on successful
     * completion
     * @return <code>true</code> if the delete is successful
     * @throws IOException If an IO error occurs
     */
    private boolean deleteResource(final String path, final HttpServletRequest req,
            final HttpServletResponse resp, final boolean setStatus)
            throws IOException {

        String ifHeader = req.getHeader("If");
        if (ifHeader == null) {
            ifHeader = "";
        }

        String lockTokenHeader = req.getHeader("Lock-Token");
        if (lockTokenHeader == null) {
            lockTokenHeader = "";
        }

        if (isLocked(path, ifHeader + lockTokenHeader)) {
            resp.sendError(WebdavStatus.SC_LOCKED);
            return false;
        }

        final WebResource resource = this.resources.getResource(path);

        if (!resource.exists()) {
            resp.sendError(WebdavStatus.SC_NOT_FOUND);
            return false;
        }

        if (!resource.isDirectory()) {
            if (!resource.delete()) {
                resp.sendError(WebdavStatus.SC_INTERNAL_SERVER_ERROR);
                return false;
            }
        } else {

            final Hashtable<String, Integer> errorList = new Hashtable<>();

            deleteCollection(req, path, errorList);
            if (!resource.delete()) {
                errorList.put(path, WebdavStatus.SC_INTERNAL_SERVER_ERROR);
            }

            if (!errorList.isEmpty()) {
                sendReport(req, resp, errorList);
                return false;
            }
        }
        if (setStatus) {
            resp.setStatus(WebdavStatus.SC_NO_CONTENT);
        }
        return true;
    }

    /**
     * Deletes a collection.
     *
     * @param req The Servlet request
     * @param path Path to the collection to be deleted
     * @param errorList Contains the list of the errors which occurred
     */
    private void deleteCollection(final HttpServletRequest req,
            final String path,
            final Hashtable<String, Integer> errorList) {

        if (this.debug > 1) {
            log("Delete:" + path);
        }

        // Prevent deletion of special subdirectories
        if (isSpecialPath(path)) {
            errorList.put(path, WebdavStatus.SC_FORBIDDEN);
            return;
        }

        String ifHeader = req.getHeader("If");
        if (ifHeader == null) {
            ifHeader = "";
        }

        String lockTokenHeader = req.getHeader("Lock-Token");
        if (lockTokenHeader == null) {
            lockTokenHeader = "";
        }

        final String[] entries = this.resources.list(path);

        for (final String entry : entries) {
            String childName = path;
            if (!childName.equals("/")) {
                childName += "/";
            }
            childName += entry;

            if (isLocked(childName, ifHeader + lockTokenHeader)) {

                errorList.put(childName, WebdavStatus.SC_LOCKED);

            } else {
                final WebResource childResource = this.resources.getResource(childName);
                if (childResource.isDirectory()) {
                    deleteCollection(req, childName, errorList);
                }

                if (!childResource.delete()) {
                    if (!childResource.isDirectory()) {
                        // If it's not a collection, then it's an unknown
                        // error
                        errorList.put(childName, WebdavStatus.SC_INTERNAL_SERVER_ERROR);
                    }
                }
            }
        }
    }

    /**
     * Send a multistatus element containing a complete error report to the
     * client.
     *
     * @param req Servlet request
     * @param resp Servlet response
     * @param errorList List of error to be displayed
     * @throws IOException If an IO error occurs
     */
    private void sendReport(final HttpServletRequest req, final HttpServletResponse resp,
            final Hashtable<String, Integer> errorList)
            throws IOException {

        resp.setStatus(WebdavStatus.SC_MULTI_STATUS);

        final String absoluteUri = req.getRequestURI();
        final String relativePath = getRelativePath(req);

        final XMLWriter generatedXML = new XMLWriter();
        generatedXML.writeXMLHeader();

        generatedXML.writeElement("D", DEFAULT_NAMESPACE, "multistatus",
                XMLWriter.OPENING);

        final Enumeration<String> pathList = errorList.keys();
        while (pathList.hasMoreElements()) {

            final String errorPath = pathList.nextElement();
            final int errorCode = errorList.get(errorPath);

            generatedXML.writeElement("D", "response", XMLWriter.OPENING);

            generatedXML.writeElement("D", "href", XMLWriter.OPENING);
            String toAppend = errorPath.substring(relativePath.length());
            if (!toAppend.startsWith("/")) {
                toAppend = "/" + toAppend;
            }
            generatedXML.writeText(absoluteUri + toAppend);
            generatedXML.writeElement("D", "href", XMLWriter.CLOSING);
            generatedXML.writeElement("D", "status", XMLWriter.OPENING);
            generatedXML.writeText("HTTP/1.1 " + errorCode + " "
                    + WebdavStatus.getStatusText(errorCode));
            generatedXML.writeElement("D", "status", XMLWriter.CLOSING);

            generatedXML.writeElement("D", "response", XMLWriter.CLOSING);

        }

        generatedXML.writeElement("D", "multistatus", XMLWriter.CLOSING);

        final Writer writer = resp.getWriter();
        writer.write(generatedXML.toString());
        writer.close();

    }

    /**
     * Propfind helper method.
     *
     * @param req The servlet request
     * @param generatedXML XML response to the Propfind request
     * @param path Path of the current resource
     * @param type Propfind type
     * @param propertiesVector If the propfind type is find properties by name,
     * then this Vector contains those properties
     */
    private void parseProperties(final HttpServletRequest req,
            final XMLWriter generatedXML,
            final String path, final int type,
            final Vector<String> propertiesVector) {

        // Exclude any resource in the /WEB-INF and /META-INF subdirectories
        if (isSpecialPath(path)) {
            return;
        }

        final WebResource resource = this.resources.getResource(path);
        if (!resource.exists()) {
            // File is in directory listing but doesn't appear to exist
            // Broken symlink or odd permission settings?
            return;
        }

        String href = req.getContextPath() + req.getServletPath();
        if ((href.endsWith("/")) && (path.startsWith("/"))) {
            href += path.substring(1);
        } else {
            href += path;
        }
        if (resource.isDirectory() && (!href.endsWith("/"))) {
            href += "/";
        }

        final String rewrittenUrl = rewriteUrl(href);

        generatePropFindResponse(generatedXML, rewrittenUrl, path, type, propertiesVector,
                resource.isFile(), false, resource.getCreation(), resource.getLastModified(),
                resource.getContentLength(), getServletContext().getMimeType(resource.getName()),
                resource.getETag());
    }

    /**
     * Propfind helper method. Displays the properties of a lock-null resource.
     *
     * @param generatedXML XML response to the Propfind request
     * @param path Path of the current resource
     * @param type Propfind type
     * @param propertiesVector If the propfind type is find properties by name,
     * then this Vector contains those properties
     */
    private void parseLockNullProperties(final HttpServletRequest req,
            final XMLWriter generatedXML,
            final String path, final int type,
            final Vector<String> propertiesVector) {

        // Exclude any resource in the /WEB-INF and /META-INF subdirectories
        if (isSpecialPath(path)) {
            return;
        }

        // Retrieving the lock associated with the lock-null resource
        final LockInfo lock = this.resourceLocks.get(path);

        if (lock == null) {
            return;
        }

        final String absoluteUri = req.getRequestURI();
        final String relativePath = getRelativePath(req);
        String toAppend = path.substring(relativePath.length());
        if (!toAppend.startsWith("/")) {
            toAppend = "/" + toAppend;
        }

        final String rewrittenUrl = rewriteUrl(RequestUtil.normalize(
                absoluteUri + toAppend));

        generatePropFindResponse(generatedXML, rewrittenUrl, path, type, propertiesVector,
                true, true, lock.creationDate.getTime(), lock.creationDate.getTime(),
                0, "", "");
    }

    private void generatePropFindResponse(final XMLWriter generatedXML, final String rewrittenUrl,
            final String path, final int propFindType, final Vector<String> propertiesVector, final boolean isFile,
            final boolean isLockNull, final long created, final long lastModified, final long contentLength,
            final String contentType, final String eTag) {

        generatedXML.writeElement("D", "response", XMLWriter.OPENING);
        String status = "HTTP/1.1 " + WebdavStatus.SC_OK + " "
                + WebdavStatus.getStatusText(WebdavStatus.SC_OK);

        // Generating href element
        generatedXML.writeElement("D", "href", XMLWriter.OPENING);
        generatedXML.writeText(rewrittenUrl);
        generatedXML.writeElement("D", "href", XMLWriter.CLOSING);

        String resourceName = path;
        final int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1) {
            resourceName = resourceName.substring(lastSlash + 1);
        }

        switch (propFindType) {

            case FIND_ALL_PROP:

                generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
                generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

                generatedXML.writeProperty("D", "creationdate", getISOCreationDate(created));
                generatedXML.writeElement("D", "displayname", XMLWriter.OPENING);
                generatedXML.writeData(resourceName);
                generatedXML.writeElement("D", "displayname", XMLWriter.CLOSING);
                if (isFile) {
                    generatedXML.writeProperty("D", "getlastmodified",
                            FastHttpDateFormat.formatDate(lastModified, null));
                    generatedXML.writeProperty("D", "getcontentlength", Long.toString(contentLength));
                    if (contentType != null) {
                        generatedXML.writeProperty("D", "getcontenttype", contentType);
                    }
                    generatedXML.writeProperty("D", "getetag", eTag);
                    if (isLockNull) {
                        generatedXML.writeElement("D", "resourcetype", XMLWriter.OPENING);
                        generatedXML.writeElement("D", "lock-null", XMLWriter.NO_CONTENT);
                        generatedXML.writeElement("D", "resourcetype", XMLWriter.CLOSING);
                    } else {
                        generatedXML.writeElement("D", "resourcetype", XMLWriter.NO_CONTENT);
                    }
                } else {
                    generatedXML.writeElement("D", "resourcetype", XMLWriter.OPENING);
                    generatedXML.writeElement("D", "collection", XMLWriter.NO_CONTENT);
                    generatedXML.writeElement("D", "resourcetype", XMLWriter.CLOSING);
                }

                generatedXML.writeProperty("D", "source", "");

                String supportedLocks = "<D:lockentry>"
                        + "<D:lockscope><D:exclusive/></D:lockscope>"
                        + "<D:locktype><D:write/></D:locktype>"
                        + "</D:lockentry>" + "<D:lockentry>"
                        + "<D:lockscope><D:shared/></D:lockscope>"
                        + "<D:locktype><D:write/></D:locktype>"
                        + "</D:lockentry>";
                generatedXML.writeElement("D", "supportedlock", XMLWriter.OPENING);
                generatedXML.writeText(supportedLocks);
                generatedXML.writeElement("D", "supportedlock", XMLWriter.CLOSING);

                generateLockDiscovery(path, generatedXML);

                generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
                generatedXML.writeElement("D", "status", XMLWriter.OPENING);
                generatedXML.writeText(status);
                generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
                generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

                break;

            case FIND_PROPERTY_NAMES:

                generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
                generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

                generatedXML.writeElement("D", "creationdate", XMLWriter.NO_CONTENT);
                generatedXML.writeElement("D", "displayname", XMLWriter.NO_CONTENT);
                if (isFile) {
                    generatedXML.writeElement("D", "getcontentlanguage", XMLWriter.NO_CONTENT);
                    generatedXML.writeElement("D", "getcontentlength", XMLWriter.NO_CONTENT);
                    generatedXML.writeElement("D", "getcontenttype", XMLWriter.NO_CONTENT);
                    generatedXML.writeElement("D", "getetag", XMLWriter.NO_CONTENT);
                    generatedXML.writeElement("D", "getlastmodified", XMLWriter.NO_CONTENT);
                }
                generatedXML.writeElement("D", "resourcetype", XMLWriter.NO_CONTENT);
                generatedXML.writeElement("D", "source", XMLWriter.NO_CONTENT);
                generatedXML.writeElement("D", "lockdiscovery", XMLWriter.NO_CONTENT);

                generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
                generatedXML.writeElement("D", "status", XMLWriter.OPENING);
                generatedXML.writeText(status);
                generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
                generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

                break;

            case FIND_BY_PROPERTY:

                final Vector<String> propertiesNotFound = new Vector<>();

                // Parse the list of properties
                generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
                generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

                final Enumeration<String> properties = propertiesVector.elements();

                while (properties.hasMoreElements()) {

                    final String property = properties.nextElement();

                    switch (property) {
                        case "creationdate":
                            generatedXML.writeProperty("D", "creationdate", getISOCreationDate(created));
                            break;
                        case "displayname":
                            generatedXML.writeElement("D", "displayname", XMLWriter.OPENING);
                            generatedXML.writeData(resourceName);
                            generatedXML.writeElement("D", "displayname", XMLWriter.CLOSING);
                            break;
                        case "getcontentlanguage":
                            if (isFile) {
                                generatedXML.writeElement("D", "getcontentlanguage",
                                        XMLWriter.NO_CONTENT);
                            } else {
                                propertiesNotFound.addElement(property);
                            }
                            break;
                        case "getcontentlength":
                            if (isFile) {
                                generatedXML.writeProperty("D", "getcontentlength",
                                        Long.toString(contentLength));
                            } else {
                                propertiesNotFound.addElement(property);
                            }
                            break;
                        case "getcontenttype":
                            if (isFile) {
                                generatedXML.writeProperty("D", "getcontenttype", contentType);
                            } else {
                                propertiesNotFound.addElement(property);
                            }
                            break;
                        case "getetag":
                            if (isFile) {
                                generatedXML.writeProperty("D", "getetag", eTag);
                            } else {
                                propertiesNotFound.addElement(property);
                            }
                            break;
                        case "getlastmodified":
                            if (isFile) {
                                generatedXML.writeProperty("D", "getlastmodified",
                                        FastHttpDateFormat.formatDate(lastModified, null));
                            } else {
                                propertiesNotFound.addElement(property);
                            }
                            break;
                        case "resourcetype":
                            if (isFile) {
                                if (isLockNull) {
                                    generatedXML.writeElement("D", "resourcetype", XMLWriter.OPENING);
                                    generatedXML.writeElement("D", "lock-null", XMLWriter.NO_CONTENT);
                                    generatedXML.writeElement("D", "resourcetype", XMLWriter.CLOSING);
                                } else {
                                    generatedXML.writeElement("D", "resourcetype", XMLWriter.NO_CONTENT);
                                }
                            } else {
                                generatedXML.writeElement("D", "resourcetype", XMLWriter.OPENING);
                                generatedXML.writeElement("D", "collection", XMLWriter.NO_CONTENT);
                                generatedXML.writeElement("D", "resourcetype", XMLWriter.CLOSING);
                            }
                            break;
                        case "source":
                            generatedXML.writeProperty("D", "source", "");
                            break;
                        case "supportedlock":
                            supportedLocks = "<D:lockentry>"
                                    + "<D:lockscope><D:exclusive/></D:lockscope>"
                                    + "<D:locktype><D:write/></D:locktype>"
                                    + "</D:lockentry>" + "<D:lockentry>"
                                    + "<D:lockscope><D:shared/></D:lockscope>"
                                    + "<D:locktype><D:write/></D:locktype>"
                                    + "</D:lockentry>";
                            generatedXML.writeElement("D", "supportedlock", XMLWriter.OPENING);
                            generatedXML.writeText(supportedLocks);
                            generatedXML.writeElement("D", "supportedlock", XMLWriter.CLOSING);
                            break;
                        case "lockdiscovery":
                            if (!generateLockDiscovery(path, generatedXML)) {
                                propertiesNotFound.addElement(property);
                            }
                            break;
                        default:
                            propertiesNotFound.addElement(property);
                            break;
                    }

                }

                generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
                generatedXML.writeElement("D", "status", XMLWriter.OPENING);
                generatedXML.writeText(status);
                generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
                generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

                final Enumeration<String> propertiesNotFoundList = propertiesNotFound.elements();

                if (propertiesNotFoundList.hasMoreElements()) {

                    status = "HTTP/1.1 " + WebdavStatus.SC_NOT_FOUND + " "
                            + WebdavStatus.getStatusText(WebdavStatus.SC_NOT_FOUND);

                    generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
                    generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

                    while (propertiesNotFoundList.hasMoreElements()) {
                        generatedXML.writeElement("D", propertiesNotFoundList.nextElement(),
                                XMLWriter.NO_CONTENT);
                    }

                    generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
                    generatedXML.writeElement("D", "status", XMLWriter.OPENING);
                    generatedXML.writeText(status);
                    generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
                    generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

                }

                break;

        }

        generatedXML.writeElement("D", "response", XMLWriter.CLOSING);
    }

    /**
     * Print the lock discovery information associated with a path.
     *
     * @param path Path
     * @param generatedXML XML data to which the locks info will be appended
     * @return <code>true</code> if at least one lock was displayed
     */
    private boolean generateLockDiscovery(final String path, final XMLWriter generatedXML) {

        final LockInfo resourceLock = this.resourceLocks.get(path);
        final Enumeration<LockInfo> collectionLocksList = this.collectionLocks.elements();

        boolean wroteStart = false;

        if (resourceLock != null) {
            wroteStart = true;
            generatedXML.writeElement("D", "lockdiscovery", XMLWriter.OPENING);
            resourceLock.toXML(generatedXML);
        }

        while (collectionLocksList.hasMoreElements()) {
            final LockInfo currentLock = collectionLocksList.nextElement();
            if (path.startsWith(currentLock.path)) {
                if (!wroteStart) {
                    wroteStart = true;
                    generatedXML.writeElement("D", "lockdiscovery",
                            XMLWriter.OPENING);
                }
                currentLock.toXML(generatedXML);
            }
        }

        if (wroteStart) {
            generatedXML.writeElement("D", "lockdiscovery", XMLWriter.CLOSING);
        } else {
            return false;
        }

        return true;

    }

    /**
     * Get creation date in ISO format.
     *
     * @return the formatted creation date
     */
    private String getISOCreationDate(final long creationDate) {
        return creationDateFormat.format(new Date(creationDate));
    }

    /**
     * Determines the methods normally allowed for the resource.
     *
     * @param req The Servlet request
     * @return a string builder with the allowed HTTP methods
     */
    private StringBuilder determineMethodsAllowed2(final HttpServletRequest req) {

        final StringBuilder methodsAllowed = new StringBuilder();

        final WebResource resource = this.resources.getResource(getRelativePath(req));

        if (!resource.exists()) {
            methodsAllowed.append("OPTIONS, MKCOL, PUT, LOCK");
            return methodsAllowed;
        }

        methodsAllowed.append("OPTIONS, GET, HEAD, POST, DELETE, TRACE");
        methodsAllowed.append(", PROPPATCH, COPY, MOVE, LOCK, UNLOCK");

        if (this.listings) {
            methodsAllowed.append(", PROPFIND");
        }

        if (resource.isFile()) {
            methodsAllowed.append(", PUT");
        }

        return methodsAllowed;
    }

    // --------------------------------------------------  LockInfo Inner Class
    /**
     * Work around for XML parsers that don't fully respect
     * {@link DocumentBuilderFactory#setExpandEntityReferences(boolean)} when
     * called with <code>false</code>. External references are filtered out for
     * security reasons. See CVE-2007-5461.
     */
    private static class WebdavResolver implements EntityResolver {

        private final ServletContext context;

        public WebdavResolver(final ServletContext theContext) {
            this.context = theContext;
        }

        @Override
        public InputSource resolveEntity(final String publicId, final String systemId) {
            this.context.log(sm.getString("webdavservlet.enternalEntityIgnored",
                    publicId, systemId));
            return new InputSource(
                    new StringReader("Ignored external entity"));
        }
    }

    // --------------------------------------------- WebdavResolver Inner Class
    /**
     * Holds a lock information.
     */
    private class LockInfo {

        // ------------------------------------------------- Instance Variables
        String path = "/";
        String type = "write";
        String scope = "exclusive";
        int depth = 0;
        String owner = "";
        Vector<String> tokens = new Vector<>();
        long expiresAt = 0;
        Date creationDate = new Date();

        // ----------------------------------------------------- Public Methods
        /**
         * Get a String representation of this lock token.
         */
        @Override
        public String toString() {

            final StringBuilder result = new StringBuilder("Type:");
            result.append(this.type);
            result.append("\nScope:");
            result.append(this.scope);
            result.append("\nDepth:");
            result.append(this.depth);
            result.append("\nOwner:");
            result.append(this.owner);
            result.append("\nExpiration:");
            result.append(FastHttpDateFormat.formatDate(this.expiresAt, null));
            final Enumeration<String> tokensList = this.tokens.elements();
            while (tokensList.hasMoreElements()) {
                result.append("\nToken:");
                result.append(tokensList.nextElement());
            }
            result.append("\n");
            return result.toString();
        }

        /**
         * @return true if the lock has expired.
         */
        public boolean hasExpired() {
            return System.currentTimeMillis() > this.expiresAt;
        }

        /**
         * @return true if the lock is exclusive.
         */
        public boolean isExclusive() {
            return this.scope.equals("exclusive");
        }

        /**
         * Get an XML representation of this lock token.
         *
         * @param generatedXML The XML write to which the fragment will be
         * appended
         */
        public void toXML(final XMLWriter generatedXML) {

            generatedXML.writeElement("D", "activelock", XMLWriter.OPENING);

            generatedXML.writeElement("D", "locktype", XMLWriter.OPENING);
            generatedXML.writeElement("D", this.type, XMLWriter.NO_CONTENT);
            generatedXML.writeElement("D", "locktype", XMLWriter.CLOSING);

            generatedXML.writeElement("D", "lockscope", XMLWriter.OPENING);
            generatedXML.writeElement("D", this.scope, XMLWriter.NO_CONTENT);
            generatedXML.writeElement("D", "lockscope", XMLWriter.CLOSING);

            generatedXML.writeElement("D", "depth", XMLWriter.OPENING);
            if (this.depth == WebdavServlet.this.maxDepth) {
                generatedXML.writeText("Infinity");
            } else {
                generatedXML.writeText("0");
            }
            generatedXML.writeElement("D", "depth", XMLWriter.CLOSING);

            generatedXML.writeElement("D", "owner", XMLWriter.OPENING);
            generatedXML.writeText(this.owner);
            generatedXML.writeElement("D", "owner", XMLWriter.CLOSING);

            generatedXML.writeElement("D", "timeout", XMLWriter.OPENING);
            final long timeout = (this.expiresAt - System.currentTimeMillis()) / 1000;
            generatedXML.writeText("Second-" + timeout);
            generatedXML.writeElement("D", "timeout", XMLWriter.CLOSING);

            generatedXML.writeElement("D", "locktoken", XMLWriter.OPENING);
            final Enumeration<String> tokensList = this.tokens.elements();
            while (tokensList.hasMoreElements()) {
                generatedXML.writeElement("D", "href", XMLWriter.OPENING);
                generatedXML.writeText("opaquelocktoken:"
                        + tokensList.nextElement());
                generatedXML.writeElement("D", "href", XMLWriter.CLOSING);
            }
            generatedXML.writeElement("D", "locktoken", XMLWriter.CLOSING);

            generatedXML.writeElement("D", "activelock", XMLWriter.CLOSING);

        }

    }
}

