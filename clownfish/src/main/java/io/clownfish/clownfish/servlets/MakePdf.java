package io.clownfish.clownfish.servlets;

import io.clownfish.clownfish.utils.PDFUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;

@WebServlet(name = "MakePdf", urlPatterns = {"/MakePdf"})
@Component
public class MakePdf extends HttpServlet
{
    @Autowired transient PDFUtil pdfutil;

    final transient Logger LOGGER = LoggerFactory.getLogger(MakePdf.class);

    public MakePdf() {
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws java.io.IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = request.getRequestURL().toString();
        LOGGER.info(url);
        String name = request.getParameter("site");
        String param = request.getParameter("param");
        response.setHeader("Content-disposition", "inline; filename=" + URLEncoder.encode(name, StandardCharsets.UTF_8.toString()));
        response.setContentType("application/pdf");
        ServletOutputStream out = response.getOutputStream();
        ByteArrayOutputStream out1 = pdfutil.createPDF(name, param);
        byte[] bytes = out1.toByteArray();
        out.write(bytes, 0, bytes.length);
        out.flush();
        out.close();
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
