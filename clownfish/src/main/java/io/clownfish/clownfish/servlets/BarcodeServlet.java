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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.*;
import org.krysalis.barcode4j.impl.codabar.CodabarBean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.impl.int2of5.Interleaved2Of5Bean;
import org.krysalis.barcode4j.impl.postnet.POSTNETBean;
import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.impl.upcean.EAN8Bean;
import org.krysalis.barcode4j.impl.upcean.UPCABean;
import org.krysalis.barcode4j.impl.upcean.UPCEBean;
import org.krysalis.barcode4j.impl.pdf417.PDF417Bean;
import org.krysalis.barcode4j.impl.datamatrix.DataMatrixBean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@WebServlet(name = "BarcodeServlet", urlPatterns = {"/Barcode"}, asyncSupported = true)
@Component
public class BarcodeServlet extends HttpServlet {
    
    final Logger logger = LoggerFactory.getLogger(BarcodeServlet.class);
    
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
        final AsyncContext acontext = request.startAsync();
        
        acontext.start(new Runnable() {
            public void run() {
                OutputStream out = null;
                try {
                    int barcodeHeight = 5;
                    int barcodeDpi = 150;
                    Object bean = null;
                    String barcode = acontext.getRequest().getParameter("barcode");
                    String type = acontext.getRequest().getParameter("type");
                    String height = acontext.getRequest().getParameter("height");
                    String dpi = acontext.getRequest().getParameter("dpi");
                    String message = acontext.getRequest().getParameter("message");
                    if (height != null) {
                        barcodeHeight = Integer.parseInt(height);
                    }   if (dpi != null) {
                        barcodeDpi = Integer.parseInt(dpi);
                    }   if (type == null) {
                        type = "Code128";
                    }   if (message == null) {
                        message = "yes";
                    }   if (type.compareToIgnoreCase("code128") == 0) {
                        bean = new Code128Bean();
                    }   if (type.compareToIgnoreCase("code39") == 0) {
                        bean = new Code39Bean();
                    }   if (type.compareToIgnoreCase("codabar") == 0) {
                        bean = new CodabarBean();
                    }   if (type.compareToIgnoreCase("int2of5") == 0) {
                        bean = new Interleaved2Of5Bean();
                    }   if (type.compareToIgnoreCase("postnet") == 0) {
                        bean = new POSTNETBean();
                    }   if (type.compareToIgnoreCase("upcean13") == 0) {
                        bean = new EAN13Bean();
                    }   if (type.compareToIgnoreCase("upcean8") == 0) {
                        bean = new EAN8Bean();
                    }   if (type.compareToIgnoreCase("upca") == 0) {
                        bean = new UPCABean();
                    }   if (type.compareToIgnoreCase("upce") == 0) {
                        bean = new UPCEBean();
                    }   if (type.compareToIgnoreCase("pdf417") == 0) {
                        bean = new PDF417Bean();
                    }   if (type.compareToIgnoreCase("datamatrix") == 0) {
                        bean = new DataMatrixBean();
                    }   ((AbstractBarcodeBean) bean).setBarHeight(barcodeHeight);
                    if (message.compareToIgnoreCase("no") == 0) {
                        ((AbstractBarcodeBean) bean).setMsgPosition(HumanReadablePlacement.HRP_NONE);
                    }   out = new java.io.FileOutputStream(new File("output.png"));
                    BitmapCanvasProvider provider = new BitmapCanvasProvider(out, "image/x-png", barcodeDpi, BufferedImage.TYPE_BYTE_GRAY, true, 0);
                    ((AbstractBarcodeBean) bean).generateBarcode(provider, barcode);
                    provider.finish();
                    BufferedImage barcodeImage = provider.getBufferedImage();
                    acontext.getResponse().setContentType("image/x-png");
                    OutputStream outputStream = acontext.getResponse().getOutputStream();
                    ImageIO.write(barcodeImage, "png", outputStream);
                    outputStream.close();
                } catch (FileNotFoundException ex) {
                    logger.error(ex.getMessage());
                    acontext.complete();
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                    acontext.complete();
                } finally {
                    try {
                        out.close();
                        acontext.complete();
                    } catch (IOException ex) {
                        logger.error(ex.getMessage());
                        acontext.complete();
                    }
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
