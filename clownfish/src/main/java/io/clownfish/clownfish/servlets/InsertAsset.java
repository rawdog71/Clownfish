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

import com.google.gson.Gson;
import io.clownfish.clownfish.beans.ContentList;
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetkeyword;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.lucene.AssetIndexer;
import io.clownfish.clownfish.lucene.IndexService;
import io.clownfish.clownfish.serviceinterface.CfAssetKeywordService;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.ClownfishUtil;
import io.clownfish.clownfish.utils.FolderUtil;
import io.clownfish.clownfish.utils.SiteUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.PersistenceException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tomcat.util.http.fileupload.impl.InvalidContentTypeException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

/**
 *
 * @author SulzbachR
 */
@WebServlet(name = "InsertAsset", urlPatterns = {"/InsertAsset"})
@MultipartConfig
public class InsertAsset extends HttpServlet {
    @Autowired CfAssetService cfassetService;
    @Autowired CfSiteService cfsiteService;
    @Autowired AssetIndexer assetIndexer;
    @Autowired IndexService indexService;
    @Autowired ContentList classcontentlist;
    @Autowired CfKeywordService cfkeywordService;
    @Autowired CfAssetKeywordService cfassetkeywordService;
    @Autowired FolderUtil folderUtil;
    @Autowired transient SiteUtil siteUtil;
    @Autowired ApiKeyUtil apikeyutil;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(InsertAsset.class);
    
    /**
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String apikey = request.getParameter("apikey");
            if (apikeyutil.checkApiKey(apikey, "RestService")) {
                boolean publicuse = ClownfishUtil.getBoolean(request.getParameter("publicuse"), false);
                boolean overwrite = ClownfishUtil.getBoolean(request.getParameter("overwrite"), false);
            
                HashMap<String, String> metamap = new HashMap<>();
                List<Part> fileParts = request.getParts().stream().filter(part -> "file".equals(part.getName()) && part.getSize() > 0).collect(Collectors.toList()); // Retrieves <input type="file" name="file" multiple="true">

                String keywords = request.getParameter("keywords");
                String[] keywordlist = null;
                if (null != keywords) {
                    keywordlist = keywords.split(";");
                }
                
                String publishsites = request.getParameter("publishsites");
                String[] publishsiteslist = null;
                if (null != publishsites) {
                    publishsiteslist = publishsites.split(";");
                }
                
                for (Part filePart : fileParts) {
                    String filename = Paths.get(filePart.getSubmittedFileName()).getFileName().toString().toLowerCase(); // MSIE fix.
                    InputStream inputStream = filePart.getInputStream();

                    File result = new File(folderUtil.getMedia_folder() + File.separator + filename);
                    boolean fileexists = result.exists();
                    if ((!fileexists) || (overwrite)) {
                        try (FileOutputStream fileOutputStream = new FileOutputStream(result, false)) {
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

                    //detecting the file type using detect method
                    String fileextension = FilenameUtils.getExtension(folderUtil.getMedia_folder() + File.separator + filename);

                    Parser parser = new AutoDetectParser();
                    BodyContentHandler handler = new BodyContentHandler(-1);
                    Metadata metadata = new Metadata();
                    try (FileInputStream inputstream = new FileInputStream(result)) {
                        ParseContext context = new ParseContext();
                        parser.parse(inputstream, handler, metadata, context);
                    } catch (SAXException | TikaException ex) {
                        LOGGER.error(ex.getMessage());
                    }

                    //getting the list of all meta data elements 
                    String[] metadataNames = metadata.names();
                    for (String name : metadataNames) {
                        metamap.put(name, metadata.get(name));
                    }

                    CfAsset newasset = null;
                    if (!fileexists) {
                        newasset = new CfAsset();
                    } else {
                        if (overwrite) {
                            newasset = cfassetService.findByName(filename);
                        }
                    }
                    if (null != newasset) {
                        newasset.setName(filename);
                        newasset.setFileextension(fileextension.toLowerCase());
                        newasset.setMimetype(metamap.get("Content-Type"));
                        newasset.setImagewidth(metamap.get("Image Width"));
                        newasset.setImageheight(metamap.get("Image Height"));
                        newasset.setPublicuse(publicuse);
                        newasset.setDescription("");
                        newasset.setUploadtime(new DateTime().toDate());
                        newasset.setFilesize(result.length());
                        try {
                            if (!fileexists) {
                                newasset = cfassetService.create(newasset);
                            } else {
                                if (overwrite) {
                                    newasset = cfassetService.edit(newasset);
                                }
                            }
                            // Index the uploaded assets and merge the Index files
                            if ((null != folderUtil.getIndex_folder()) && (!folderUtil.getMedia_folder().isEmpty())) {
                                Thread assetindexer_thread = new Thread(assetIndexer);
                                assetindexer_thread.start();
                            }
                            // If Asset exists and is overwrite, delete all assetkeywords
                            if (fileexists && overwrite) {
                                for (CfAssetkeyword assetkeyword : cfassetkeywordService.findByAssetRef(newasset.getId())) {
                                    cfassetkeywordService.delete(assetkeyword);
                                }
                            }
                            if (null != keywordlist) {
                                for (String keyword : keywordlist) {
                                    if (!keyword.isBlank()) {
                                        CfAssetkeyword assetkeyword = new CfAssetkeyword(newasset.getId(), cfkeywordService.findByName(keyword).getId());
                                        cfassetkeywordService.create(assetkeyword);
                                    }
                                }
                            }
                        } catch (PersistenceException ex) {
                            newasset = cfassetService.findByName(filename);
                            LOGGER.info("DUPLICATE FOUND " + filename);
                            Gson gson = new Gson(); 
                            String json = gson.toJson("DUPLICATE FOUND " + filename);
                            response.setContentType("application/json;charset=UTF-8");
                            try (PrintWriter out = response.getWriter()) {
                                out.print(json);
                            } catch (IOException ex1) {
                                LOGGER.error(ex1.getMessage());
                            }
                        }
                    }

                    classcontentlist.initAssetlist();
                    Gson gson = new Gson(); 
                    String json = gson.toJson(newasset);
                    response.setContentType("application/json;charset=UTF-8");
                    try (PrintWriter out = response.getWriter()) {
                        out.print(json);
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                }
                if (null != publishsiteslist) {
                    for (String sitename : publishsiteslist) {
                        CfSite site = cfsiteService.findByName(sitename);
                        siteUtil.publishSite(site, false);
                    }
                }
                
            }
        } catch (IOException | PersistenceException e) {
            LOGGER.error(e.getMessage());
            Gson gson = new Gson(); 
            String json = gson.toJson("ERROR");
            response.setContentType("application/json;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print(json);
            } catch (IOException ex1) {
                LOGGER.error(ex1.getMessage());
            }
        }
    }
}
