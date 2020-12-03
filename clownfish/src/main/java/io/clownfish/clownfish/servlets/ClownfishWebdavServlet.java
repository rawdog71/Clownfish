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
package io.clownfish.clownfish.servlets;

import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetkeyword;
import io.clownfish.clownfish.dbentities.CfKeyword;
import io.clownfish.clownfish.lucene.AssetIndexer;
import io.clownfish.clownfish.lucene.IndexService;
import io.clownfish.clownfish.serviceinterface.CfAssetKeywordService;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.utils.FolderUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import io.clownfish.clownfish.webdav.WebdavStatus;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.Getter;
import lombok.Setter;
import org.apache.catalina.WebResource;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.util.XMLWriter;
import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.http.FastHttpDateFormat;
import org.imgscalr.AsyncScalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author SulzbachR
 */
@Component
public class ClownfishWebdavServlet extends DefaultServlet {
    @Autowired CfAssetService cfassetService;
    @Autowired CfKeywordService cfkeywordService;
    @Autowired CfAssetKeywordService cfassetkeywordService;
    @Autowired PropertyUtil propertyUtil;
    @Autowired FolderUtil folderUtil;
    @Autowired IndexService indexService;
    @Autowired AssetIndexer assetIndexer;
    
    private @Getter @Setter List<CfAsset> assetlist;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(ClownfishWebdavServlet.class);
    
    /**
     * Default namespace.
     */
    protected static final String DEFAULT_NAMESPACE = "DAV:";
    
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
    private final Hashtable<String, Vector<String>> lockNullResources = new Hashtable<>();

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
    public void init() throws ServletException {

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
        
        /*
        if (isSpecialPath(path)) {
            resp.sendError(WebdavStatus.SC_NOT_FOUND);
            return;
        }
        */

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

    private void doPropfind(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!this.listings) {
            try {
                // Get allowed methods
                final String methodsAllowed = determineMethodsAllowed(req);
                
                resp.addHeader("Allow", methodsAllowed);
                resp.sendError(WebdavStatus.SC_METHOD_NOT_ALLOWED);
                return;
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
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
        
        resp.setStatus(WebdavStatus.SC_MULTI_STATUS);
        resp.setContentType("text/xml; charset=UTF-8");
        
        if (0 == path.compareToIgnoreCase("/webdav")) {                         // ROOT
            if (0 == depth) {    
                // Create multistatus object
                final XMLWriter generatedXML = new XMLWriter(resp.getWriter());
                generatedXML.writeXMLHeader();
                generatedXML.writeElement("D", DEFAULT_NAMESPACE, "multistatus", XMLWriter.OPENING);
                setCollectionProps(generatedXML, "");
                generatedXML.writeElement("D", "multistatus", XMLWriter.CLOSING);
                System.out.println(generatedXML.toString());
                generatedXML.sendData();
            } else {
                List<CfKeyword> keywordlist = cfkeywordService.findAll();
                
                if (!keywordlist.isEmpty()) {
                    // Create multistatus object
                    final XMLWriter generatedXML = new XMLWriter(resp.getWriter());
                    generatedXML.writeXMLHeader();
                    generatedXML.writeElement("D", DEFAULT_NAMESPACE, "multistatus", XMLWriter.OPENING);
                    for (CfKeyword keyword : keywordlist) {
                        setCollectionProps(generatedXML, keyword.getName());
                    }
                    generatedXML.writeElement("D", "multistatus", XMLWriter.CLOSING);
                    System.out.println(generatedXML.toString());
                    generatedXML.sendData();
                }
            }            
        } else {  
            if (0 == depth) {
                String subpath = path.substring(8);
                System.out.println("SUB: " + subpath);
                
                if (subpath.contains("/")) {
                    String assetname = subpath.substring(subpath.indexOf("/") + 1);
                    System.out.println("ASSET: " + assetname);
                    
                    try {
                        CfAsset asset = cfassetService.findByName(assetname);

                        final XMLWriter generatedXML = new XMLWriter(resp.getWriter());
                        generatedXML.writeXMLHeader();
                        generatedXML.writeElement("D", DEFAULT_NAMESPACE, "multistatus", XMLWriter.OPENING);

                        generatedXML.writeElement("D", "response", XMLWriter.OPENING);
                        String status = "HTTP/1.1 " + WebdavStatus.SC_OK + " " + WebdavStatus.getStatusText(WebdavStatus.SC_OK);

                        // Generating href element
                        generatedXML.writeElement("D", "href", XMLWriter.OPENING);
                        generatedXML.writeText("/webdav/" + subpath);
                        generatedXML.writeElement("D", "href", XMLWriter.CLOSING);

                        // STATUS OK
                        generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
                        generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

                        generatedXML.writeElement("D", "displayname", XMLWriter.OPENING);
                        generatedXML.writeData(getName(asset));
                        generatedXML.writeElement("D", "displayname", XMLWriter.CLOSING);

                        generatedXML.writeElement("D", "resourcetype", XMLWriter.NO_CONTENT);

                        generatedXML.writeProperty("D", "getcontentlength", Long.toString(getContentLength(asset)));
                        generatedXML.writeProperty("D", "creationdate", getModifieyDate(asset).toString());
                        generatedXML.writeProperty("D", "getlastmodified", getModifieyDate(asset).toString());
                        generatedXML.writeProperty("D", "getcontenttype", getContentType(asset));

                        generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
                        generatedXML.writeElement("D", "status", XMLWriter.OPENING);
                        generatedXML.writeText(status);
                        generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
                        generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

                        generatedXML.writeElement("D", "response", XMLWriter.CLOSING);

                        generatedXML.writeElement("D", "multistatus", XMLWriter.CLOSING);

                        System.out.println(generatedXML.toString());
                        generatedXML.sendData();
                    } catch (Exception ex) {
                        LOGGER.error(ex.getMessage());
                    }
                } else {
                    // Create multistatus object
                    if (subpath.compareToIgnoreCase("desktop.ini") != 0) {
                        final XMLWriter generatedXML = new XMLWriter(resp.getWriter());
                        generatedXML.writeXMLHeader();
                        generatedXML.writeElement("D", DEFAULT_NAMESPACE, "multistatus", XMLWriter.OPENING);
                        setCollectionProps(generatedXML, subpath);
                        generatedXML.writeElement("D", "multistatus", XMLWriter.CLOSING);
                        System.out.println(generatedXML.toString());
                        generatedXML.sendData();
                    }
                }
            } else {
                String subpath = path.substring(8);
                System.out.println("SUB: " + subpath);
                
                if (subpath.contains("/")) {
                    String assetname = subpath.substring(subpath.indexOf("/") + 1);
                    System.out.println("ASSET: " + assetname);
                    
                    CfAsset asset = cfassetService.findByName(assetname);
                    
                    final XMLWriter generatedXML = new XMLWriter(resp.getWriter());
                    generatedXML.writeXMLHeader();
                    generatedXML.writeElement("D", DEFAULT_NAMESPACE, "multistatus", XMLWriter.OPENING);
                    
                    generatedXML.writeElement("D", "response", XMLWriter.OPENING);
                    String status = "HTTP/1.1 " + WebdavStatus.SC_OK + " " + WebdavStatus.getStatusText(WebdavStatus.SC_OK);

                    // Generating href element
                    generatedXML.writeElement("D", "href", XMLWriter.OPENING);
                    generatedXML.writeText("/webdav/" + subpath + "/" + getName(asset));
                    generatedXML.writeElement("D", "href", XMLWriter.CLOSING);

                    // STATUS OK
                    generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
                    generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

                    generatedXML.writeElement("D", "displayname", XMLWriter.OPENING);
                    generatedXML.writeData(getName(asset));
                    generatedXML.writeElement("D", "displayname", XMLWriter.CLOSING);

                    generatedXML.writeElement("D", "resourcetype", XMLWriter.NO_CONTENT);

                    generatedXML.writeProperty("D", "getcontentlength", Long.toString(getContentLength(asset)));
                    generatedXML.writeProperty("D", "creationdate", getModifieyDate(asset).toString());
                    generatedXML.writeProperty("D", "getlastmodified", getModifieyDate(asset).toString());
                    generatedXML.writeProperty("D", "getcontenttype", getContentType(asset));

                    generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
                    generatedXML.writeElement("D", "status", XMLWriter.OPENING);
                    generatedXML.writeText(status);
                    generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
                    generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

                    generatedXML.writeElement("D", "response", XMLWriter.CLOSING);
                    
                    generatedXML.writeElement("D", "multistatus", XMLWriter.CLOSING);

                    System.out.println(generatedXML.toString());
                    generatedXML.sendData();
                    
                } else {
                    CfKeyword keyword = cfkeywordService.findByName(subpath);

                    ArrayList<CfAsset> assetlist = new ArrayList<>();
                    List<CfAssetkeyword> assetkeywordlist = cfassetkeywordService.findByKeywordRef(keyword.getId());
                    for (CfAssetkeyword assetkeyword : assetkeywordlist) {
                        assetlist.add(cfassetService.findById(assetkeyword.getCfAssetkeywordPK().getAssetref()));
                    }

                    if (!assetlist.isEmpty()) {
                        // Create multistatus object
                        final XMLWriter generatedXML = new XMLWriter(resp.getWriter());
                        generatedXML.writeXMLHeader();
                        generatedXML.writeElement("D", DEFAULT_NAMESPACE, "multistatus", XMLWriter.OPENING);
                        for (CfAsset asset : assetlist) {
                            generatedXML.writeElement("D", "response", XMLWriter.OPENING);
                            String status = "HTTP/1.1 " + WebdavStatus.SC_OK + " " + WebdavStatus.getStatusText(WebdavStatus.SC_OK);

                            // Generating href element
                            generatedXML.writeElement("D", "href", XMLWriter.OPENING);
                            generatedXML.writeText("/webdav/" + keyword.getName() + "/" + getName(asset));
                            generatedXML.writeElement("D", "href", XMLWriter.CLOSING);

                            // STATUS OK
                            generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
                            generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

                            generatedXML.writeElement("D", "displayname", XMLWriter.OPENING);
                            generatedXML.writeData(getName(asset));
                            generatedXML.writeElement("D", "displayname", XMLWriter.CLOSING);

                            generatedXML.writeElement("D", "resourcetype", XMLWriter.NO_CONTENT);

                            generatedXML.writeElement("D", "creationdate", XMLWriter.OPENING);
                            generatedXML.writeData(getModifieyDate(asset).toString());
                            generatedXML.writeElement("D", "creationdate", XMLWriter.CLOSING);

                            generatedXML.writeElement("D", "getlastmodified", XMLWriter.OPENING);
                            generatedXML.writeData(getModifieyDate(asset).toString());
                            generatedXML.writeElement("D", "getlastmodified", XMLWriter.CLOSING);

                            generatedXML.writeElement("D", "getcontentlength", XMLWriter.OPENING);
                            generatedXML.writeData(getContentLength(asset).toString());
                            generatedXML.writeElement("D", "getcontentlength", XMLWriter.CLOSING);

                            generatedXML.writeElement("D", "getcontenttype", XMLWriter.OPENING);
                            generatedXML.writeData(getContentType(asset));
                            generatedXML.writeElement("D", "getcontenttype", XMLWriter.CLOSING);

                            generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
                            generatedXML.writeElement("D", "status", XMLWriter.OPENING);
                            generatedXML.writeText(status);
                            generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
                            generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

                            generatedXML.writeElement("D", "response", XMLWriter.CLOSING);
                        }
                        generatedXML.writeElement("D", "multistatus", XMLWriter.CLOSING);

                        System.out.println(generatedXML.toString());
                        generatedXML.sendData();
                    }
                }
            }
        }
    }

    private void doProppatch(HttpServletRequest req, HttpServletResponse resp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void doMkcol(HttpServletRequest req, HttpServletResponse resp) {
        try {
            final String path = getRelativePath(req);
            
            String subpath = path.substring(8);
            if (subpath.length() > 1 && subpath.endsWith("/")) {
                subpath = subpath.substring(0, subpath.length() - 1);
            }
            System.out.println("PATH: " + subpath);
            CfKeyword newkeyword = new CfKeyword();
            newkeyword.setName(subpath);
            newkeyword = cfkeywordService.create(newkeyword);
            //return newkeyword;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    private void doCopy(HttpServletRequest req, HttpServletResponse resp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void doMove(HttpServletRequest req, HttpServletResponse resp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void doLock(HttpServletRequest req, HttpServletResponse resp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void doUnlock(HttpServletRequest req, HttpServletResponse resp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.addHeader("DAV", "1,2");

        final String methodsAllowed = determineMethodsAllowed(req);

        resp.addHeader("Allow", methodsAllowed);
        resp.addHeader("MS-Author-Via", "DAV");
    }
    
    
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        // Serve the requested resource, including the data content
        serveResource(request, response, true, this.fileEncoding);
    }
    
    @Override
    protected void serveResource(HttpServletRequest request, HttpServletResponse response, boolean content, String encoding) throws IOException, ServletException {
        String path = this.getRelativePath(request, true);
        String subpath = path.substring(8);
        String assetname = subpath.substring(subpath.indexOf("/") + 1);
        System.out.println("ASSET: " + assetname);
        
        CfAsset asset = cfassetService.findByName(assetname);
        
        if (null != asset) {
            if (!asset.isScrapped()) {
                //if (1 == inst_download) {
                    response.setHeader("Content-Encoding", "gzip");
                    response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(assetname, StandardCharsets.UTF_8.toString()));
                //} else {
                //    response.setHeader("Content-disposition", "inline; filename=" + URLEncoder.encode(imagefilename, StandardCharsets.UTF_8.toString()));
                //}
                if (asset.getMimetype().contains("image")) {
                    if (asset.getMimetype().contains("svg")) {
                        response.setContentType(asset.getMimetype());
                        InputStream in;
                        File f = new File(propertyUtil.getPropertyValue("folder_media") + File.separator + assetname);
                        try (OutputStream out = new GZIPOutputStream(response.getOutputStream())) {
                            in = new FileInputStream(f);
                            IOUtils.copy(in, out);
                        } catch (IOException ex) {
                            LOGGER.error(ex.getMessage());
                            //acontext.complete();
                        }
                    } else {
                        response.setContentType(asset.getMimetype());
                        InputStream in;
                        File f = new File(propertyUtil.getPropertyValue("folder_media") + File.separator + assetname);
                        try (OutputStream out = new GZIPOutputStream(response.getOutputStream())) {
                            in = new FileInputStream(f);
                            IOUtils.copy(in, out);
                        } catch (IOException ex) {
                            LOGGER.error(ex.getMessage());
                            //acontext.complete();
                        }
                    }
                } else {
                    response.setContentType(asset.getMimetype());
                    InputStream in;
                    File f = new File(propertyUtil.getPropertyValue("folder_media") + File.separator + assetname);
                    try (OutputStream out = response.getOutputStream()) {
                        in = new FileInputStream(f);
                        IOUtils.copy(in, out);
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                        //acontext.complete();
                    }
                }
            }
            //acontext.complete();
        } else {
            OutputStream outputStream = response.getOutputStream();
            outputStream.close();
            //acontext.complete();
        }
        
        
        /*
        try {
            String imagefilename = asset.getName();
            return Files.readAllBytes(Paths.get(propUtil.getPropertyValue("folder_media") + File.separator + imagefilename));
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(WebDAVController.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        */
    }
    
    private String getName(CfAsset asset) {
        return asset.getName();
    }
    
    private String getDisplayName(CfAsset asset) {
        return asset.getName();
    }
    
    private String getContentType(CfAsset asset) {
        return asset.getMimetype();
    }
    
    private Long getContentLength(CfAsset asset) {
        try {
            String imagefilename = asset.getName();
            return Files.size(Paths.get(propertyUtil.getPropertyValue("folder_media") + File.separator + imagefilename));
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
            return 0L;
        }
    }
    
    private Date getModifieyDate(CfAsset asset) {
        try {
            String imagefilename = asset.getName();
            return new Date(Files.getLastModifiedTime(Paths.get(propertyUtil.getPropertyValue("folder_media") + File.separator + imagefilename)).toMillis());
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }
    
    private void setCollectionProps(XMLWriter generatedXML, String path) {
        generatedXML.writeElement("D", "response", XMLWriter.OPENING);
        String status = "HTTP/1.1 " + WebdavStatus.SC_OK + " " + WebdavStatus.getStatusText(WebdavStatus.SC_OK);

        // Generating href element
        generatedXML.writeElement("D", "href", XMLWriter.OPENING);
        if (path.isEmpty()) {
            generatedXML.writeText("/webdav/");
        } else {
            generatedXML.writeText("/webdav/" + path + "/");
        }
        generatedXML.writeElement("D", "href", XMLWriter.CLOSING);

        // STATUS OK
        generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
        generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

        generatedXML.writeElement("D", "displayname", XMLWriter.OPENING);
        generatedXML.writeData(path);
        generatedXML.writeElement("D", "displayname", XMLWriter.CLOSING);

        generatedXML.writeElement("D", "resourcetype", XMLWriter.OPENING);
        generatedXML.writeElement("D", "collection", XMLWriter.NO_CONTENT);
        generatedXML.writeElement("D", "resourcetype", XMLWriter.CLOSING);

        generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
        generatedXML.writeElement("D", "status", XMLWriter.OPENING);
        generatedXML.writeText(status);
        generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
        generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

        // STATUS NOT FOUND
        status = "HTTP/1.1 " + WebdavStatus.SC_NOT_FOUND + " " + WebdavStatus.getStatusText(WebdavStatus.SC_NOT_FOUND);
        generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
        generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

        generatedXML.writeElement("D", "creationdate", XMLWriter.NO_CONTENT);
        generatedXML.writeElement("D", "getcontentlength", XMLWriter.NO_CONTENT);
        generatedXML.writeElement("D", "getcontenttype", XMLWriter.NO_CONTENT);
        generatedXML.writeElement("D", "getetag", XMLWriter.NO_CONTENT);
        generatedXML.writeElement("D", "getlastmodified", XMLWriter.NO_CONTENT);

        generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
        generatedXML.writeElement("D", "status", XMLWriter.OPENING);
        generatedXML.writeText(status);
        generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
        generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);


        generatedXML.writeElement("D", "response", XMLWriter.CLOSING);
    }
    
    /**
     * Determines the methods normally allowed for the resource.
     *
     * @param req The Servlet request
     * @return a string builder with the allowed HTTP methods
     */
    @Override
    protected String determineMethodsAllowed(final HttpServletRequest req) {
        final StringBuilder methodsAllowed = new StringBuilder();
        final WebResource resource = this.resources.getResource(getRelativePath(req));

        if (!resource.exists()) {
            methodsAllowed.append("OPTIONS, MKCOL, PUT, LOCK");
            return methodsAllowed.toString();
        }

        methodsAllowed.append("OPTIONS, GET, HEAD, POST, DELETE, TRACE");
        methodsAllowed.append(", PROPPATCH, COPY, MOVE, LOCK, UNLOCK");

        if (this.listings) {
            methodsAllowed.append(", PROPFIND");
        }

        if (resource.isFile()) {
            methodsAllowed.append(", PUT");
        }

        return methodsAllowed.toString();
    }
    
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
            documentBuilder.setEntityResolver(new WebdavResolver(this.getServletContext()));
        } catch (final ParserConfigurationException e) {
            throw new ServletException(sm.getString("webdavservlet.jaxpfailed"));
        }
        return documentBuilder;
    }
    
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
            if (this.depth == ClownfishWebdavServlet.this.maxDepth) {
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
