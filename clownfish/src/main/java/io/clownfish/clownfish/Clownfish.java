/*
 * Copyright 2019 sulzbachr.
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
package io.clownfish.clownfish;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.clownfish.clownfish.beans.ServiceStatus;
import io.clownfish.clownfish.beans.SiteTreeBean;
import io.clownfish.clownfish.datamodels.ClientInformation;
import io.clownfish.clownfish.datamodels.ClownfishResponse;
import io.clownfish.clownfish.datamodels.JsonFormParameter;
import io.clownfish.clownfish.datamodels.RenderContext;
import io.clownfish.clownfish.datamodels.SearchContext;
import io.clownfish.clownfish.dbentities.*;
import io.clownfish.clownfish.exceptions.PageNotFoundException;
import io.clownfish.clownfish.lucene.SearchResult;
import io.clownfish.clownfish.lucene.Searcher;
import io.clownfish.clownfish.service.PageRenderService;
import io.clownfish.clownfish.serviceinterface.*;
import io.clownfish.clownfish.templatebeans.*;
import io.clownfish.clownfish.utils.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.fileupload.FileItem;
import org.apache.lucene.queryparser.classic.ParseException;
import org.primefaces.webapp.MultipartRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;


/**
 *
 * @author sulzbachr
 * Central class of the Clownfish server
 */
@RestController
@EnableAutoConfiguration(exclude = HibernateJpaAutoConfiguration.class)
@Component
@Configuration
@PropertySources({
    @PropertySource("file:application.properties")
})
public class Clownfish {
    final transient Logger LOGGER = LoggerFactory.getLogger(Clownfish.class);
    
    private final CfSiteService cfsiteService;
    private final PropertyUtil propertyUtil;
    private final Searcher searcher;
    private final SearchUtil searchUtil;
    private final FolderUtil folderUtil;
    private final ClownfishUtil clownfishUtil;
    private final ServiceStatus servicestatus;
    private final PageRenderService pagerenderservice;
    private final @Getter MarkdownUtil markdownUtil;
    
    UploadTemplateBean uploadbean;
    private String contenttype;
    private String characterencoding;
    private String locale;
    private HttpSession userSession;
    private @Getter @Setter int searchlimit;
    private @Getter @Setter Map<String, String> metainfomap;
    private @Getter @Setter boolean initmessage = false;
    private @Getter @Setter SiteTreeBean sitetree;
    
    @Value("${server.name:Clownfish Server Open Source}") 
    String servername;
    @Value("${server.x-powered:Clownfish Server Open Source by Rainer Sulzbach}") 
    String serverxpowered;
    
    @Autowired @Getter @Setter CfAssetService cfassetService;
    @Autowired @Getter @Setter CfAttributcontentService cfattributcontentService;
    
    // Constructor Injection
    public Clownfish(CfSiteService cfsiteService, 
                        PageRenderService pageRenderService, 
                        PropertyUtil propertyUtil, 
                        Searcher searcher, 
                        SearchUtil searchUtil, 
                        FolderUtil folderUtil, 
                        ServiceStatus serviceStatus, 
                        ClownfishUtil clownfishUtil,
                        ServiceStatus servicestatus,
                        PageRenderService pagerenderservice,
                        MarkdownUtil markdownUtil) {
        this.cfsiteService = cfsiteService;
        this.propertyUtil = propertyUtil;
        this.searcher = searcher;
        this.searchUtil = searchUtil;
        this.folderUtil = folderUtil;
        this.clownfishUtil = clownfishUtil;
        this.servicestatus = servicestatus;
        this.pagerenderservice = pagerenderservice;
        this.markdownUtil = markdownUtil;
    }
    
    /**
     * Call of the "root" site
     * Fetches the root site from the system property "site_root" and calls universalGet 
     * @param request
     * @param response
     */
    @RequestMapping("/")
    public void home(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        String root_site = propertyUtil.getPropertyValue("site_root");
        if (null == root_site) {
            root_site = "root";
        }
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, root_site);
        universalGet(root_site, request, response, null);
    }
    
    /**
     * GET
     * 
     * @param name
     * @param request
     * @param response
     * @param searchctx
     */
    @GetMapping(path = "/{name}/**")
    public void universalGet(@PathVariable("name") String name, @Context HttpServletRequest request, @Context HttpServletResponse response, SearchContext searchctx) {
        if (servicestatus.isOnline()) {
            Map searchcontentmap = null;
            Map searchassetmap = null;
            Map searchassetmetadatamap = null;
            Map searchclasscontentmap = null;
            Map searchmetadata = null;
            
            if (null == searchctx) {
                // Init Lucene Search Map
                if (null == searchcontentmap) {
                    searchcontentmap = new HashMap<>();
                }
                if (null == searchassetmap) {
                    searchassetmap = new HashMap<>();
                }
                if (null == searchassetmetadatamap) {
                    searchassetmetadatamap = new HashMap<>();
                }
                if (null == searchclasscontentmap) {
                    searchclasscontentmap = new HashMap<>();
                }
                if (null == searchmetadata) {
                    searchmetadata = new HashMap<>();
                }
            } else {
                searchcontentmap = searchctx.getSearchcontentmap();
                searchassetmap = searchctx.getSearchassetmap();
                searchassetmetadatamap = searchctx.getSearchassetmetadatamap();
                searchclasscontentmap = searchctx.getSearchclasscontentmap();
                searchmetadata = searchctx.getSearchmetadata();
            }
            
            Cookie[] cookies = request.getCookies();
            String referrer = getCookieVal(cookies, "cf_referrer");
            addHeader(response, clownfishUtil.getVersion());
            ClientInformation clientinfo = getClientinformation(request.getRemoteAddr());
            String token = getCookieVal(cookies, "cf_token");
            String login_token = getCookieVal(cookies, "cf_login_token");
            boolean alias = false;
            String aliasname = "";
            try {
                ArrayList urlParams = new ArrayList();
                // fetch site by name or aliasname
                CfSite cfsite = null;
                cfsite = cfsiteService.findByName(name);
                if (null == cfsite) {
                    aliasname = name;
                    cfsite = cfsiteService.findByAliaspath(name);
                    if (null != cfsite) {
                        name = cfsite.getName();
                        alias = true;
                    } else {
                        aliasname = name;
                        cfsite = cfsiteService.findByShorturl(name);
                        if (null != cfsite) {
                            name = cfsite.getName();
                            alias = true;
                        } else {
                            throw new PageNotFoundException("PageNotFound Exception: " + name);
                        }
                    }
                }
                
                response.setContentType(cfsite.getContenttype());
                response.setCharacterEncoding(cfsite.getCharacterencoding());
                
                if (!cfsite.isSearchresult()) {
                    String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
                    if (alias) {
                        path = path.replaceFirst(aliasname, name);
                    } else {
                        path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
                    }

                    if (path.contains("/")) {
                        String[] params = path.split("/");
                        for (int i = 1; i < params.length; i++) {
                            if (1 == i) {
                                path = params[i];
                            } else {
                                urlParams.add(params[i]);
                            }
                        }
                    }

                    if (name.compareToIgnoreCase(path) != 0) {
                        if (path.startsWith("/")) {
                            name = path.substring(1);
                        } else {
                            name = path;
                        }
                        if (name.lastIndexOf("/")+1 == name.length()) {
                            name = name.substring(0, name.length()-1);
                        }
                    }
                } else {
                    searchmetadata.clear();
                    String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
                    String query = "";
                    if (path.contains("/")) {
                        String[] params = path.split("/");
                        for (int i = 1; i < params.length; i++) {
                            if (1 == i) {
                                path = params[i];
                            } else {
                                query += params[i];
                            }
                        }
                    }
                    if (query.isEmpty()) {
                        Map<String, String[]> parammap = request.getParameterMap();
                        if (parammap.containsKey("query")) {
                            query = parammap.get("query")[0];
                        } else {
                            query = "";
                        }
                    }

                    String[] searchexpressions = query.split(" ");
                    searchUtil.updateSearchhistory(searchexpressions);

                    searcher.setIndexPath(folderUtil.getIndex_folder());
                    long startTime = System.currentTimeMillis();
                    SearchResult searchresult = searcher.search(query, searchlimit);
                    long endTime = System.currentTimeMillis();

                    LOGGER.info("Search Time :" + (endTime - startTime));
                    
                    searchcontentmap.clear();
                    if (searchresult != null && searchresult.getFoundSites() != null) {
                        // putAll statt loop
                        searchcontentmap.putAll(
                            searchresult.getFoundSites().stream()
                                .filter(Objects::nonNull) // Null-Check aus dem Stream
                                .collect(Collectors.toMap(
                                    site -> site.getName(), // Key
                                    site -> site,           // Value
                                    (existing, replacement) -> replacement // Falls Keys doppelt sind: überschreiben (wie bei put)
                                ))
                        );
                    }

                    searchassetmap.clear();
                    if (searchresult != null && searchresult.getFoundAssets() != null) {
                        searchassetmap.putAll(
                            searchresult.getFoundAssets().stream()
                                .filter(Objects::nonNull)
                                .collect(Collectors.toMap(CfAsset::getName, asset -> asset, (v1, v2) -> v2))
                        );
                    }

                    searchassetmetadatamap.clear();
                    if (searchresult != null && searchresult.getFoundAssetsMetadata() != null) {
                        searchassetmetadatamap.putAll(searchresult.getFoundAssetsMetadata());
                    }
                    
                    searchclasscontentmap.clear();
                    if (searchresult != null && searchresult.getFoundClasscontent() != null) {
                        searchclasscontentmap.putAll(searchresult.getFoundClasscontent());
                    }
                }

                userSession = request.getSession();
                Map<String, String[]> querymap = request.getParameterMap();

                ArrayList queryParams = new ArrayList();
                if ((null != token) && (!token.isEmpty())) {
                    JsonFormParameter jfp = new JsonFormParameter();
                    jfp.setName("cf_token");
                    jfp.setValue(token);
                    queryParams.add(jfp);
                }
                if ((null != login_token) && (!login_token.isEmpty())) {
                    JsonFormParameter jfp = new JsonFormParameter();
                    jfp.setName("cf_login_token");
                    jfp.setValue(login_token);
                    queryParams.add(jfp);
                }
                querymap.keySet().stream().map((key) -> {
                    JsonFormParameter jfp = new JsonFormParameter();
                    jfp.setName((String) key);
                    String[] values = querymap.get((String) key);
                    jfp.setValue(values[0]);
                    return jfp;
                }).forEach((jfp) -> {
                    queryParams.add(jfp);
                });

                //addHeader(response, clownfishutil.getVersion());
                //LOGGER.info("MAKERESPONSE: " + name);
                
                RenderContext rc = new RenderContext();
                rc.setName(name);
                rc.setPostmap(queryParams);
                rc.setUrlParams(urlParams);
                rc.setMakestatic(false);
                rc.setFileitems(null);
                rc.setClientinfo(clientinfo);
                rc.setReferrer(referrer);
                rc.setSearchassetmap(searchassetmap);
                rc.setSearchassetmetadatamap(searchassetmetadatamap);
                rc.setSearchclasscontentmap(searchclasscontentmap);
                rc.setSearchcontentmap(searchcontentmap);
                rc.setSearchmetadata(searchmetadata);
                
                ClownfishResponse cfResponse = pagerenderservice.renderPage(rc);
                
                Cookie refcookie = new Cookie("cf_referrer", "");
                response.addCookie(refcookie);
                if (0 != cfResponse.getErrorcode()) {
                    switch (cfResponse.getErrorcode()) {
                        case 1, 2, 4 -> {
                            response.setContentType("text/html");
                            response.setCharacterEncoding("UTF-8");
                        }
                        case 3, 5 -> {
                            refcookie = new Cookie("cf_referrer", name);
                            response.addCookie(refcookie);
                            response.sendRedirect("/" + cfResponse.getRelocation());
                        }
                    }
                }
                ServletOutputStream out = response.getOutputStream();
                out.write(cfResponse.getOutput().getBytes(response.getCharacterEncoding()));
            } catch (IOException | ParseException ex) {
                LOGGER.error(ex.getMessage());
            } catch (PageNotFoundException ex) {
                String error_site = propertyUtil.getPropertyValue("site_error");
                if (null == error_site) {
                    error_site = "error";
                }
                request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, error_site);
                universalGet(error_site, request, response, null);
            }
        } else {
            PrintWriter outwriter = null;
            try {
                outwriter = response.getWriter();
                outwriter.println(servicestatus.getMessage());
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            } finally {
                if (null != outwriter) {
                    outwriter.close();
                }
            }
        }
    }

    /**
     * POST
     * 
     * @param name
     * @param request
     * @param response
     * @throws io.clownfish.clownfish.exceptions.PageNotFoundException
     */
    @PostMapping("/upload/{name}")
    public void universalPostMultipart(@PathVariable("name") String name, @Context MultipartRequest request, @Context HttpServletResponse response) throws PageNotFoundException {
        Map searchcontentmap = null;
        Map searchassetmap = null;
        Map searchassetmetadatamap = null;
        Map searchclasscontentmap = null;
        Map searchmetadata = null;
        
        boolean alias = false;
        try {
            ClientInformation clientinfo = getClientinformation(request.getRemoteAddr());
            ArrayList urlParams = new ArrayList();
            String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            
            userSession = request.getSession();
            if (request.getContentType().startsWith("multipart/form-data")) {
                Map<String, String[]> parameterMap = request.getParameterMap();
                List<FileItem> fis = request.getFileItems("file");
                List<JsonFormParameter> map = new ArrayList<>();
                for (String key : parameterMap.keySet()) {
                    map.add(new JsonFormParameter(key, parameterMap.get(key)[0]));
                }
                
                addHeader(response, clownfishUtil.getVersion());
                
                RenderContext rc = new RenderContext();
                rc.setName(name);
                rc.setPostmap(map);
                rc.setUrlParams(urlParams);
                rc.setMakestatic(false);
                rc.setFileitems(null);
                rc.setClientinfo(clientinfo);
                rc.setReferrer("");
                rc.setSearchassetmap(searchassetmap);
                rc.setSearchassetmetadatamap(searchassetmetadatamap);
                rc.setSearchclasscontentmap(searchclasscontentmap);
                rc.setSearchcontentmap(searchcontentmap);
                rc.setSearchmetadata(searchmetadata);
                ClownfishResponse cfResponse = pagerenderservice.renderPage(rc);
                
                //ClownfishResponse cfResponse = makeResponse(name, map, urlParams, false, fis, clientinfo, "");
                if (cfResponse.getErrorcode() == 0) {
                    response.setContentType(this.contenttype);
                    response.setCharacterEncoding(this.characterencoding);
                    
                    if ((null != uploadbean) && (null != uploadbean.getUploadpath()) && (!uploadbean.getUploadpath().isEmpty())) {
                        for (FileItem fi : fis) {
                            if ((uploadbean.getFileitemmap().get(fi.getName())) || (uploadbean.getFileitemmap().isEmpty())) {
                                File result = new File(uploadbean.getUploadpath() + File.separator + fi.getName());
                                boolean fileexists = result.exists();
                                InputStream inputStream = fi.getInputStream();
                                if (!fileexists) {
                                    try (FileOutputStream fileOutputStream = new FileOutputStream(result)) {
                                        byte[] buffer = new byte[64535];
                                        int bulk;
                                        while (true) {
                                            bulk = inputStream.read(buffer);
                                            if (bulk < 0) {
                                                break;
                                            }
                                            fileOutputStream.write(buffer, 0, bulk);
                                            fileOutputStream.flush();
                                        }
                                        fileOutputStream.close();
                                    }
                                }
                                inputStream.close();
                            }
                        }
                    }
                    
                    ServletOutputStream out = response.getOutputStream();
                    out.write(cfResponse.getOutput().getBytes(this.characterencoding)); 
                } else {
                    response.setContentType("text/html");
                    response.setCharacterEncoding("UTF-8");
                    ServletOutputStream out = response.getOutputStream();
                    out.write(cfResponse.getOutput().getBytes(this.characterencoding)); 
                }
            } else {
                String content = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

                Gson gson = new Gson();
                List<JsonFormParameter> map;
                map = (List<JsonFormParameter>) gson.fromJson(content, new TypeToken<List<JsonFormParameter>>() {}.getType());
                
                // fetch site by name or aliasname
                CfSite cfsite = null;
                try {
                    cfsite = cfsiteService.findByName(name);
                } catch (Exception ex) {
                    try {
                        cfsite = cfsiteService.findByAliaspath(name);
                        name = cfsite.getName();
                        alias = true;
                    } catch (Exception e1) {
                        try {
                            cfsite = cfsiteService.findByShorturl(name);
                            name = cfsite.getName();
                            alias = true;
                        } catch (Exception ey) {
                            throw new PageNotFoundException("PageNotFound Exception: " + name);
                        }
                    }
                }
                if (cfsite.isSearchresult()) {
                    if (searchcontentmap.isEmpty()) {
                        String query = "";
                        for (JsonFormParameter jsp : map) {
                            if (0 == jsp.getName().compareToIgnoreCase("search")) {
                                query += jsp.getValue();
                            }
                        }
                        String[] searchexpressions = query.split(" ");
                        searchUtil.updateSearchhistory(searchexpressions);

                        searcher.setIndexPath(folderUtil.getIndex_folder());
                        long startTime = System.currentTimeMillis();
                        SearchResult searchresult = searcher.search(query, searchlimit);
                        long endTime = System.currentTimeMillis();

                        LOGGER.info("Search Time :" + (endTime - startTime));
                        searchmetadata.clear();
                        searchmetadata.put("cfSearchQuery", query);
                        searchmetadata.put("cfSearchTime", String.valueOf(endTime - startTime));
                        searchcontentmap.clear();
                        searchresult.getFoundSites().stream().forEach((site) -> {
                            searchcontentmap.put(site.getName(), site);
                        });
                        searchassetmap.clear();
                        searchresult.getFoundAssets().stream().forEach((asset) -> {
                            searchassetmap.put(asset.getName(), asset);
                        });
                        searchassetmetadatamap.clear();
                        searchresult.getFoundAssetsMetadata().keySet().stream().forEach((key) -> {
                            searchassetmetadatamap.put(key, searchresult.getFoundAssetsMetadata().get(key));
                        });
                        searchclasscontentmap.clear();
                        searchresult.getFoundClasscontent().keySet().stream().forEach((key) -> {
                            searchclasscontentmap.put(key, searchresult.getFoundClasscontent().get(key));
                        });
                    }
                }

                addHeader(response, clownfishUtil.getVersion());
                
                RenderContext rc = new RenderContext();
                rc.setName(name);
                rc.setPostmap(map);
                rc.setUrlParams(urlParams);
                rc.setMakestatic(false);
                rc.setFileitems(null);
                rc.setClientinfo(clientinfo);
                rc.setReferrer("");
                rc.setSearchassetmap(searchassetmap);
                rc.setSearchassetmetadatamap(searchassetmetadatamap);
                rc.setSearchclasscontentmap(searchclasscontentmap);
                rc.setSearchcontentmap(searchcontentmap);
                rc.setSearchmetadata(searchmetadata);
                ClownfishResponse cfResponse = pagerenderservice.renderPage(rc);
                
                if (cfResponse.getErrorcode() == 0) {
                    response.setContentType(this.contenttype);
                    response.setCharacterEncoding(this.characterencoding);
                    ServletOutputStream out = response.getOutputStream();
                    out.write(cfResponse.getOutput().getBytes(this.characterencoding)); 
                } else {
                    response.setContentType("text/html");
                    response.setCharacterEncoding("UTF-8");
                    ServletOutputStream out = response.getOutputStream();
                    out.write(cfResponse.getOutput().getBytes(this.characterencoding)); 
                }
            }
        } catch (IOException | PageNotFoundException | IllegalStateException | ParseException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    /**
     * POST
     * 
     * @param name
     * @param request
     * @param response
     * @throws io.clownfish.clownfish.exceptions.PageNotFoundException
     */
    @PostMapping("/{name}/**")
    public void universalPostHttp(@PathVariable("name") String name, @Context HttpServletRequest request, @Context HttpServletResponse response) throws PageNotFoundException {
        Map searchcontentmap = null;
        Map searchassetmap = null;
        Map searchassetmetadatamap = null;
        Map searchclasscontentmap = null;
        Map searchmetadata = null;
        
        boolean alias = false;
        try {
            ClientInformation clientinfo = getClientinformation(request.getRemoteAddr());
            ArrayList urlParams = new ArrayList();
            String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            if (path.contains("/")) {
                String[] params = path.split("/");
                for (int i = 1; i < params.length; i++) {
                    if (1 == i) {
                        path = params[i];
                    } else {
                        urlParams.add(params[i]);
                    }
                }
            }
            if (name.compareToIgnoreCase(path) != 0) {
                name = path.substring(1);
                if (name.lastIndexOf("/")+1 == name.length()) {
                    name = name.substring(0, name.length()-1);
                }
            }
            
            userSession = request.getSession();
            String content = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

            Gson gson = new Gson();
            List<JsonFormParameter> map;
            map = (List<JsonFormParameter>) gson.fromJson(content, new TypeToken<List<JsonFormParameter>>() {}.getType());

            // fetch site by name or aliasname
            CfSite cfsite = null;
            try {
                cfsite = cfsiteService.findByName(name);
            } catch (Exception ex) {
                try {
                    cfsite = cfsiteService.findByAliaspath(name);
                    name = cfsite.getName();
                    alias = true;
                } catch (Exception e1) {
                    try {
                        cfsite = cfsiteService.findByShorturl(name);
                        name = cfsite.getName();
                        alias = true;
                    } catch (Exception ey) {
                        throw new PageNotFoundException("PageNotFound Exception: " + name);
                    }
                }
            }
            if (cfsite.isSearchresult()) {
                if (searchcontentmap.isEmpty()) {
                    String query = "";
                    for (JsonFormParameter jsp : map) {
                        if (0 == jsp.getName().compareToIgnoreCase("search")) {
                            query += jsp.getValue();
                        }
                    }
                    String[] searchexpressions = query.split(" ");
                    searchUtil.updateSearchhistory(searchexpressions);

                    searcher.setIndexPath(folderUtil.getIndex_folder());
                    long startTime = System.currentTimeMillis();
                    SearchResult searchresult = searcher.search(query, searchlimit);
                    long endTime = System.currentTimeMillis();

                    LOGGER.info("Search Time :" + (endTime - startTime));
                    searchmetadata.clear();
                    searchmetadata.put("cfSearchQuery", query);
                    searchmetadata.put("cfSearchTime", String.valueOf(endTime - startTime));
                    searchcontentmap.clear();
                    searchresult.getFoundSites().stream().forEach((site) -> {
                        searchcontentmap.put(site.getName(), site);
                    });
                    searchassetmap.clear();
                    searchresult.getFoundAssets().stream().forEach((asset) -> {
                        searchassetmap.put(asset.getName(), asset);
                    });
                    searchassetmetadatamap.clear();
                    searchresult.getFoundAssetsMetadata().keySet().stream().forEach((key) -> {
                        searchassetmetadatamap.put(key, searchresult.getFoundAssetsMetadata().get(key));
                    });
                    searchclasscontentmap.clear();
                    searchresult.getFoundClasscontent().keySet().stream().forEach((key) -> {
                        searchclasscontentmap.put(key, searchresult.getFoundClasscontent().get(key));
                    });
                }
            }

            addHeader(response, clownfishUtil.getVersion());
            
            RenderContext rc = new RenderContext();
            rc.setName(name);
            rc.setPostmap(map);
            rc.setUrlParams(urlParams);
            rc.setMakestatic(false);
            rc.setFileitems(null);
            rc.setClientinfo(clientinfo);
            rc.setReferrer("");
            rc.setSearchassetmap(searchassetmap);
            rc.setSearchassetmetadatamap(searchassetmetadatamap);
            rc.setSearchclasscontentmap(searchclasscontentmap);
            rc.setSearchcontentmap(searchcontentmap);
            rc.setSearchmetadata(searchmetadata);
            ClownfishResponse cfResponse = pagerenderservice.renderPage(rc);
            
            if (cfResponse.getErrorcode() == 0) {
                response.setContentType(this.contenttype);
                response.setCharacterEncoding(this.characterencoding);
                ServletOutputStream out = response.getOutputStream();
                out.write(cfResponse.getOutput().getBytes(this.characterencoding)); 
            } else {
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                ServletOutputStream out = response.getOutputStream();
                out.write(cfResponse.getOutput().getBytes(this.characterencoding)); 
            }
        } catch (IOException | PageNotFoundException | IllegalStateException | ParseException ex) {
            LOGGER.error(ex.getMessage());
        }
    }


    /**
     * addHeader
     * 
     */
    private void addHeader(HttpServletResponse response, String version) {
        String serverString = servername.replaceAll("#version#", version);
        String serverxpowerdedString = serverxpowered.replaceAll("#version#", version);
        response.addHeader("Server", serverString);
        response.addHeader("X-Powered-By", serverxpowerdedString);
    }

    private ClientInformation getClientinformation(String ip) {
        ClientInformation ci = new ClientInformation();
        ci.setIpadress(ip);
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(ip);
            ci.setHostname(addr.getHostName());
        } catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(Clownfish.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ci;
    }
    
    private String getCookieVal(Cookie[] cookies, String key) {
        if (null != cookies) {
            if (cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    if (0 == cookie.getName().compareToIgnoreCase(key)) {
                        String value = cookie.getValue();
                        cookie.setMaxAge(0);
                        cookie.setValue("");
                        return value;
                    }
                }
                return "";
            } else {
                return "";
            }
        } else {
            return "";
        }
    }
}
