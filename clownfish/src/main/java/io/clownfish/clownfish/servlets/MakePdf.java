package io.clownfish.clownfish.servlets;

import io.clownfish.clownfish.dbentities.*;
import io.clownfish.clownfish.jasperreports.JasperReportCompiler;
import io.clownfish.clownfish.serviceinterface.*;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import io.clownfish.clownfish.utils.TemplateUtil;
import net.sf.jasperreports.engine.JasperRunManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "MakePdf", urlPatterns = {"/MakePdf"})
@Component
public class MakePdf extends HttpServlet
{
    @Autowired transient CfTemplateService cfTemplateService;
    @Autowired transient CfTemplateversionService cfTemplateversionService;
    @Autowired transient CfSitedatasourceService cfSitedatasourceService;
    @Autowired transient CfDatasourceService cfDatasourceService;
    @Autowired transient CfSiteService cfSiteService;
    @Autowired transient PropertyUtil propertyUtil;
    @Autowired transient TemplateUtil templateUtil;
    @Autowired
    ApiKeyUtil apikeyutil;
    CfTemplate cfTemplate;

    final transient Logger LOGGER = LoggerFactory.getLogger(GetAsset.class);

    public MakePdf() {
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = request.getRequestURL().toString();
        LOGGER.info(url);

        //request.getParameter("apikey");
        String name = request.getParameter("site");
        CfSite site = cfSiteService.findByName(name);

        List<CfSitedatasource> sitedatasourcelist = cfSitedatasourceService.findBySiteref(site.getId());

        for (CfSitedatasource source : sitedatasourcelist)
        {
            long currentTemplateVersion;
            try {
                cfTemplate = cfTemplateService.findById(site.getTemplateref().longValue());
                currentTemplateVersion = cfTemplateversionService.findMaxVersion(cfTemplate.getId());
            } catch (NullPointerException ex) {
                currentTemplateVersion = 0;
            }
            String templateContent = templateUtil.getVersion(cfTemplate.getId(), currentTemplateVersion);
            CfDatasource datasource = cfDatasourceService.findById(source.getCfSitedatasourcePK().getDatasourceref());
            InputStream template = new ByteArrayInputStream(templateContent.getBytes(StandardCharsets.UTF_8));
            ServletOutputStream out = response.getOutputStream();
            ByteArrayOutputStream out1 = JasperReportCompiler.exportToPdf(datasource.getUser(), datasource.getPassword(), datasource.getUrl(), template, datasource.getDriverclass());

            byte[] bytes = out1.toByteArray();
            out.write(bytes, 0, bytes.length);
            out.flush();
            out.close();
        }
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
