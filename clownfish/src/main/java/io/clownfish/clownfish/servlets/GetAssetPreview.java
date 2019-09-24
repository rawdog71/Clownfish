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

import io.clownfish.clownfish.beans.PropertyList;
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;
import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPOutputStream;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import org.apache.commons.io.IOUtils;
import org.imgscalr.AsyncScalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 *
 * @author sulzbachr
 */
@WebServlet(name = "GetAssetPreview", urlPatterns = {"/GetAssetPreview"}, asyncSupported = true)
@Component
public class GetAssetPreview extends HttpServlet {
    @Autowired transient CfAssetService cfassetService;
    @Autowired transient PropertyList propertylist;
    
    private static int width = 0;
    private static int height = 0;
    
    private static Map<String, String> propertymap = null;
    
    final transient Logger logger = LoggerFactory.getLogger(GetAssetPreview.class);
    
    public GetAssetPreview() {
    }

    @PostConstruct
    @Override
    public void init() {
        if (propertymap == null) {
            // read all System Properties of the property table
            propertymap = propertylist.fillPropertyMap();
        }
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
        response.setHeader("Content-Encoding", "gzip");
        final AsyncContext acontext = request.startAsync();
        
        acontext.start(() -> {
            try {
                width = 0;
                height = 0;
                CfAsset asset = null;
                String mediapath = propertymap.get("folder_media");
                String cachepath = propertymap.get("folder_cache");
                String imagefilename = acontext.getRequest().getParameter("file");
                if (imagefilename != null) {
                    asset = cfassetService.findByName(imagefilename);
                    imagefilename = asset.getName();
                }
                String mediaid = acontext.getRequest().getParameter("mediaid");
                if (mediaid != null) {
                    asset = cfassetService.findById(Long.parseLong(mediaid));
                    imagefilename = asset.getName();
                }
                if (null != asset) {
                    if (asset.getMimetype().contains("image")) {
                        if (asset.getMimetype().contains("svg")) {
                            acontext.getResponse().setContentType(asset.getMimetype());
                            InputStream in;
                            File f = new File(mediapath + File.separator + imagefilename);
                            try (OutputStream out = new GZIPOutputStream(acontext.getResponse().getOutputStream())) {
                                in = new FileInputStream(f);
                                IOUtils.copy(in, out);
                            } catch (IOException ex) {
                                logger.error(ex.getMessage());
                                acontext.complete();
                            }
                        } else {
                            String paramwidth = acontext.getRequest().getParameter("width");
                            if (paramwidth != null) {
                                width = Integer.parseInt(paramwidth);
                            }
                            String paramheight = acontext.getRequest().getParameter("height");
                            if (paramheight != null) {
                                height = Integer.parseInt(paramheight);
                            }
                            String cacheKey = "cache" + imagefilename + "W" + String.valueOf(width) + "H" + String.valueOf(height);
                            if (new File(cachepath + File.separator + cacheKey).exists()) {
                                File f = new File(cachepath + File.separator + cacheKey);
                                InputStream in;
                                try (OutputStream out = new GZIPOutputStream(acontext.getResponse().getOutputStream())) {
                                    in = new FileInputStream(f);
                                    IOUtils.copy(in, out);
                                } catch (IOException ex) {
                                    logger.error(ex.getMessage());
                                    acontext.complete();
                                }
                            } else {
                                acontext.getResponse().setContentType(asset.getMimetype());
                                InputStream in;
                                File f = new File(mediapath + File.separator + imagefilename);

                                if ((width > 0) || (height > 0)) {
                                    BufferedImage result = AsyncScalr.resize(ImageIO.read(f), width).get();
                                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                                    ImageIO.write(result, asset.getFileextension(), os);
                                    ImageIO.write(result, asset.getFileextension(), new File(cachepath + File.separator + cacheKey));

                                    try (OutputStream out = new GZIPOutputStream(acontext.getResponse().getOutputStream())) {
                                        in = new ByteArrayInputStream(os.toByteArray());
                                        IOUtils.copy(in, out);
                                    } catch (IOException ex) {
                                        logger.error(ex.getMessage());
                                        acontext.complete();
                                    }
                                } else {
                                    try (OutputStream out = new GZIPOutputStream(acontext.getResponse().getOutputStream())) {
                                        in = new FileInputStream(f);
                                        IOUtils.copy(in, out);
                                    } catch (IOException ex) {
                                        logger.error(ex.getMessage());
                                        acontext.complete();
                                    }
                                }
                            }
                        }
                    } else {
                        acontext.getResponse().setContentType("image/svg+xml");
                        InputStream in;
                        File f;
                        String iconfilename;

                        String mimetype = asset.getMimetype();
                        switch (mimetype) {
                            case "application/pdf":
                                iconfilename = "pdf.svg";
                                break;
                            default:
                                iconfilename = "document.svg";
                                break;
                        }

                        String iconpath = propertymap.get("folder_icon");
                        if (null != iconpath) {
                            f = new File(iconpath + File.separator + iconfilename);
                        } else {
                            ServletContext servletContext = getServletContext();
                            String path = servletContext.getRealPath("/WEB-INF/images/" + iconfilename);
                            f = new File(path);
                        }
                        try (OutputStream out = new GZIPOutputStream(acontext.getResponse().getOutputStream())) {
                            in = new FileInputStream(f);
                            IOUtils.copy(in, out);
                        } catch (IOException ex) {
                            logger.error(ex.getMessage());
                            acontext.complete();
                        }
                    }
                    acontext.complete();
                }
                
            } catch (javax.persistence.NoResultException | java.lang.IllegalArgumentException ex) {
                acontext.getResponse().setContentType("text/html;charset=UTF-8");
                try (PrintWriter out = acontext.getResponse().getWriter()) {
                    out.print("No image");
                    acontext.complete();
                } catch (IOException ex1) {
                    logger.error(ex1.getMessage());
                    acontext.complete();
                }
            } catch (IOException | InterruptedException | ExecutionException ex) {
                logger.error(ex.getMessage());
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
