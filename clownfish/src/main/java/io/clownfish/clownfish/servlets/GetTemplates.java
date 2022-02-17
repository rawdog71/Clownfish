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

import com.google.gson.Gson;
import io.clownfish.clownfish.datamodels.AuthTokenList;
import io.clownfish.clownfish.datamodels.TemplateDataOutput;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@WebServlet(name = "GetTemplates", urlPatterns = {"/GetTemplates"})
@Component
public class GetTemplates extends HttpServlet {
    @Autowired transient CfTemplateService cftemplateService;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenList authtokenlist;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(GetTemplates.class);

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            String token = request.getParameter("token");
            if (authtokenlist.checkValidToken(token)) {
                String apikey = request.getParameter("apikey");
                if (apikeyutil.checkApiKey(apikey, "RestService")) {
                    CfTemplate template = null;
                    List<CfTemplate> templateList = new ArrayList<>();
                    String templateid = request.getParameter("id");
                    if (templateid != null) {
                        template = cftemplateService.findById(Long.parseLong(templateid));
                        templateList.add(template);
                    }
                    String templatename = request.getParameter("name");
                    if (templatename != null) {
                        template = cftemplateService.findByName(templatename);
                        templateList.clear();
                        templateList.add(template);
                    }
                    if ((null == templateid) && (null == templatename)) {
                        templateList = cftemplateService.findAll();
                    }
                    ArrayList<TemplateDataOutput> templatedataoutputList = new ArrayList<>();
                    for (CfTemplate templateItem : templateList) {
                        TemplateDataOutput templatedataoutput = new TemplateDataOutput();
                        templatedataoutput.setTemplate(templateItem);
                        templatedataoutputList.add(templatedataoutput);
                    }
                    Gson gson = new Gson(); 
                    String json = gson.toJson(templatedataoutputList);
                    response.setContentType("application/json;charset=UTF-8");
                    try (PrintWriter out = response.getWriter()) {
                        out.print(json);
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                } else {
                    PrintWriter out = response.getWriter();
                    out.print("Wrong API KEY");
                }
            } else {
                PrintWriter out = response.getWriter();
                out.print("Invalid Token");
            }
        } catch (javax.persistence.NoResultException | java.lang.IllegalArgumentException ex) {
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print("No class");
            } catch (IOException ex1) {
                LOGGER.error(ex1.getMessage());
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
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
