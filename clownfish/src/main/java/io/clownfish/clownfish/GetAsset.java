/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPOutputStream;
import javax.annotation.PostConstruct;
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
    @Autowired CfAssetService cfassetService;
    @Autowired PropertyList propertylist;
    
    private int width = 0;
    private int height = 0;
    
    private static Map<String, String> propertymap = null;
    
    public GetAsset() {
    }

    @PostConstruct
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
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Content-Encoding", "gzip");
        final AsyncContext acontext = request.startAsync();
        
        acontext.start(new Runnable() {
            public void run() {
                try {
                    width = 0;
                    height = 0;
                    CfAsset asset = null;
                    String mediapath = propertymap.get("media.folder");
                    String cachepath = propertymap.get("cache.folder");
                    String imagefilename = acontext.getRequest().getParameter("file");
                    if (imagefilename != null) {
                        //asset = (CfAsset) em.createNamedQuery("Knasset.findByName").setParameter("name", imagefilename).getSingleResult();
                        asset = cfassetService.findByName(imagefilename);
                        imagefilename = asset.getName();
                    }
                    String mediaid = acontext.getRequest().getParameter("mediaid");
                    if (mediaid != null) {
                        //asset = (Knasset) em.createNamedQuery("Knasset.findById").setParameter("id", Long.parseLong(mediaid)).getSingleResult();
                        asset = cfassetService.findById(Long.parseLong(mediaid));
                        imagefilename = asset.getName();
                    }
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
                        InputStream in = null;
                        try (OutputStream out = new GZIPOutputStream(acontext.getResponse().getOutputStream())) {
                            in = new FileInputStream(f);
                            IOUtils.copy(in, out);
                        } catch (IOException ex) {
                            Logger.getLogger(GetAsset.class.getName()).log(Level.SEVERE, null, ex);
                            acontext.complete();
                        }
                    } else {
                        acontext.getResponse().setContentType(asset.getMimetype());
                        InputStream in = null;
                        File f = new File(mediapath + File.separator + imagefilename);

                        if ((width > 0) || (height > 0)) {
                            BufferedImage result = AsyncScalr.resize(ImageIO.read(f), width).get();
                            ByteArrayOutputStream os = new ByteArrayOutputStream();
                            ImageIO.write(result, "jpg", os);
                            ImageIO.write(result, "jpg", new File(cachepath + File.separator + cacheKey));

                            try (OutputStream out = new GZIPOutputStream(acontext.getResponse().getOutputStream())) {
                                in = new ByteArrayInputStream(os.toByteArray());
                                IOUtils.copy(in, out);
                            } catch (IOException ex) {
                                Logger.getLogger(GetAsset.class.getName()).log(Level.SEVERE, null, ex);
                                acontext.complete();
                            }
                        } else {
                        try (OutputStream out = new GZIPOutputStream(acontext.getResponse().getOutputStream())) {
                                in = new FileInputStream(f);
                                IOUtils.copy(in, out);
                            } catch (IOException ex) {
                                Logger.getLogger(GetAsset.class.getName()).log(Level.SEVERE, null, ex);
                                acontext.complete();
                            }
                        }
                    }
                    acontext.complete();
                    
                } catch (javax.persistence.NoResultException | java.lang.IllegalArgumentException ex) {
                    acontext.getResponse().setContentType("text/html;charset=UTF-8");
                    try (PrintWriter out = acontext.getResponse().getWriter()) {
                        out.print("No image");
                        acontext.complete();
                    } catch (IOException ex1) {
                        Logger.getLogger(GetAsset.class.getName()).log(Level.SEVERE, null, ex1);
                        acontext.complete();
                    }
                } catch (IOException | InterruptedException | ExecutionException ex) {
                    Logger.getLogger(GetAsset.class.getName()).log(Level.SEVERE, null, ex);
                }
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
