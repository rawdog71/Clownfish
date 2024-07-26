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

import io.clownfish.clownfish.beans.ContentList;
import io.clownfish.clownfish.beans.ScrapyardList;
import io.clownfish.clownfish.datamodels.AuthResponse;
import io.clownfish.clownfish.datamodels.AuthTokenClasscontent;
import io.clownfish.clownfish.datamodels.AuthTokenListClasscontent;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.utils.PropertyUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.ServletException;
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
 * @author SulzbachR
 */
@WebServlet(name = "ConfirmDelete", urlPatterns = {"/ConfirmDelete"})
@Component
public class ClownfishConfirmDeleteServlet extends HttpServlet {
    @Autowired PropertyUtil propertyUtil;
    @Autowired transient AuthTokenListClasscontent confirmtokenlist;
    @Autowired CfTemplateService cftemplateService;
    @Autowired transient ContentList contentlist;
    @Autowired transient ScrapyardList scrapyardlist;
    String token;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(ClownfishConfirmDeleteServlet.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
        AuthResponse ar = new AuthResponse();
        ar.setStatus(false);
        ar.setToken("");
        ar.setValiduntil(null);
        Map<String, String[]> parameters = request.getParameterMap();
        
        token = "";
        parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("token") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
            token = values[0];
        });
        
        AuthTokenClasscontent at = confirmtokenlist.getAuthtokens().get(token);
        if (null != at) {
            CfClasscontent user = at.getUser();
            
            contentlist.deleteContent(user);
            scrapyardlist.destroyContent(user);
            
            confirmtokenlist.getAuthtokens().remove(token);

            String template = propertyUtil.getPropertyValue("template_deleted");
            CfTemplate cftemplate = cftemplateService.findByName(template);
            
            response.setContentType("text/html");
            try (PrintWriter out = response.getWriter()) {
                out.print(cftemplate.getContent());
            } catch (IOException ex) {
                //LOGGER.error(ex.getMessage());
            }
        } else {
            String template = propertyUtil.getPropertyValue("template_notdeleted");
            CfTemplate cftemplate = cftemplateService.findByName(template);
            
            response.setContentType("text/html");
            try (PrintWriter out = response.getWriter()) {
                out.print(cftemplate.getContent());
            } catch (IOException ex) {
                //LOGGER.error(ex.getMessage());
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);

    }
}
