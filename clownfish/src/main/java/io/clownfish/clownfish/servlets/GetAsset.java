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
package io.clownfish.clownfish.servlets;

import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;
import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;
import org.imgscalr.AsyncScalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 *
 * @author sulzbachr
 */
@WebServlet(name = "GetAsset", urlPatterns = {"/GetAsset"}, asyncSupported = true)
@Component
public class GetAsset extends HttpServlet {
    @Autowired transient CfAssetService cfassetService;
    @Autowired transient PropertyUtil propertyUtil;
    @Autowired ApiKeyUtil apikeyutil;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(GetAsset.class);
    
    public GetAsset() {
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        //response.setHeader("Content-Encoding", "gzip");
        final AsyncContext acontext = request.startAsync();
        
        acontext.start(() -> {
            int inst_width = 0;
            int inst_height = 0;
            int inst_download = 0;
            try {
                String apikey = acontext.getRequest().getParameter("apikey");
                if (apikeyutil.checkApiKey(apikey, "GetAsset")) {
                
                    String paramdownload = acontext.getRequest().getParameter("dl");
                    if (paramdownload != null) {
                        try {
                            inst_download = Integer.parseInt(paramdownload);
                        } catch (NumberFormatException nfe) {
                            inst_download = 0;
                        }
                    }
                    inst_width = 0;
                    inst_height = 0;
                    CfAsset asset = null;
                    String imagefilename = acontext.getRequest().getParameter("file");
                    if (imagefilename != null) {
                        asset = cfassetService.findByName(imagefilename);
                        if (null != asset) {
                            imagefilename = asset.getName();
                        }
                    }
                    String mediaid = acontext.getRequest().getParameter("mediaid");
                    if (mediaid != null) {
                        asset = cfassetService.findById(Long.parseLong(mediaid));
                        if (null != asset) {
                            imagefilename = asset.getName();
                        }
                    }
                    if (null != asset) {
                        if (!asset.isScrapped()) {
                            if (1 == inst_download) {
                                response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(imagefilename, StandardCharsets.UTF_8.toString()));
                            } else {
                                response.setHeader("Content-disposition", "inline; filename=" + URLEncoder.encode(imagefilename, StandardCharsets.UTF_8.toString()));
                            }
                            if (asset.getMimetype().contains("image")) {
                                if (asset.getMimetype().contains("svg")) {
                                    acontext.getResponse().setContentType(asset.getMimetype());
                                    InputStream in;
                                    File f = new File(propertyUtil.getPropertyValue("folder_media") + File.separator + imagefilename);
                                    try (OutputStream out = acontext.getResponse().getOutputStream()) {
                                        in = new FileInputStream(f);
                                        IOUtils.copy(in, out);
                                    } catch (IOException ex) {
                                        LOGGER.error(ex.getMessage());
                                        acontext.complete();
                                    }
                                } else {
                                    String paramwidth = acontext.getRequest().getParameter("width");
                                    if (paramwidth != null) {
                                        try {
                                            inst_width = Integer.parseInt(paramwidth);
                                        } catch (NumberFormatException nfe) {
                                            inst_width = 100;
                                        }
                                    }
                                    String paramheight = acontext.getRequest().getParameter("height");
                                    if (paramheight != null) {
                                        try {
                                            inst_height = Integer.parseInt(paramheight);
                                        } catch (NumberFormatException nfe) {
                                            inst_height = 100;
                                        }
                                    }
                                    String cacheKey = "cache" + imagefilename + "W" + String.valueOf(inst_width) + "H" + String.valueOf(inst_height);
                                    if (new File(propertyUtil.getPropertyValue("folder_cache") + File.separator + cacheKey).exists()) {
                                        File f = new File(propertyUtil.getPropertyValue("folder_cache") + File.separator + cacheKey);
                                        InputStream in;
                                        try (OutputStream out = acontext.getResponse().getOutputStream()) {
                                            in = new FileInputStream(f);
                                            IOUtils.copy(in, out);
                                        } catch (IOException ex) {
                                            LOGGER.error(ex.getMessage());
                                            acontext.complete();
                                        }
                                    } else {
                                        acontext.getResponse().setContentType(asset.getMimetype());
                                        InputStream in;
                                        File f = new File(propertyUtil.getPropertyValue("folder_media") + File.separator + imagefilename);

                                        if ((inst_width > 0) || (inst_height > 0)) {
                                            BufferedImage result = AsyncScalr.resize(ImageIO.read(f), inst_width).get();
                                            ByteArrayOutputStream os = new ByteArrayOutputStream();
                                            ImageIO.write(result, asset.getFileextension(), os);
                                            ImageIO.write(result, asset.getFileextension(), new File(propertyUtil.getPropertyValue("folder_cache") + File.separator + cacheKey));

                                            try (OutputStream out = acontext.getResponse().getOutputStream()) {
                                                in = new ByteArrayInputStream(os.toByteArray());
                                                IOUtils.copy(in, out);
                                            } catch (IOException ex) {
                                                LOGGER.error(ex.getMessage());
                                                acontext.complete();
                                            }
                                        } else {
                                            try (OutputStream out = acontext.getResponse().getOutputStream()) {
                                                in = new FileInputStream(f);
                                                IOUtils.copy(in, out);
                                            } catch (IOException ex) {
                                                LOGGER.error(ex.getMessage());
                                                acontext.complete();
                                            }
                                        }
                                    }
                                }
                            } else {
                                acontext.getResponse().setContentType(asset.getMimetype());
                                InputStream in;
                                File f = new File(propertyUtil.getPropertyValue("folder_media") + File.separator + imagefilename);
                                try (OutputStream out = acontext.getResponse().getOutputStream()) {
                                    in = new FileInputStream(f);
                                    IOUtils.copy(in, out);
                                } catch (IOException ex) {
                                    LOGGER.error(ex.getMessage());
                                    acontext.complete();
                                }
                            }
                        }
                        acontext.complete();
                    } else {
                        OutputStream outputStream = acontext.getResponse().getOutputStream();
                        outputStream.close();
                        acontext.complete();
                    }
                } else {
                    OutputStream outputStream = acontext.getResponse().getOutputStream();
                    outputStream.close();
                    acontext.complete();
                }
            } catch (javax.persistence.NoResultException | java.lang.IllegalArgumentException ex) {
                acontext.getResponse().setContentType("text/html;charset=UTF-8");
                try (PrintWriter out = acontext.getResponse().getWriter()) {
                    out.print("No image");
                    acontext.complete();
                } catch (IOException ex1) {
                    LOGGER.error(ex1.getMessage());
                    acontext.complete();
                }
            } catch (IOException | InterruptedException | ExecutionException ex) {
                LOGGER.error(ex.getMessage());
            }
        });
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
