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

import io.clownfish.clownfish.datamodels.AuthResponse;
import io.clownfish.clownfish.datamodels.AuthTokenClasscontent;
import io.clownfish.clownfish.datamodels.AuthTokenListClasscontent;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.utils.HibernateUtil;
import io.clownfish.clownfish.utils.MailUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Level;
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
@WebServlet(name = "Confirm", urlPatterns = {"/Confirm"})
@Component
public class ClownfishConfirmServlet extends HttpServlet {
    @Autowired HibernateUtil hibernateUtil;
    @Autowired PropertyUtil propertyUtil;
    @Autowired transient AuthTokenListClasscontent confirmtokenlist;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired transient CfAttributService cfattributService;
    @Autowired CfTemplateService cftemplateService;
    String token;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(ClownfishConfirmServlet.class);

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
            CfClass cfclass = user.getClassref();
            
            CfAttribut attributConfirm = cfattributService.findByNameAndClassref("confirmed", cfclass);
            CfAttributcontent attributContent1 = cfattributcontentService.findByAttributrefAndClasscontentref(attributConfirm, user);
            attributContent1.setContentBoolean(Boolean.TRUE);
            cfattributcontentService.edit(attributContent1);
            hibernateUtil.updateContent(attributContent1.getClasscontentref());
            
            confirmtokenlist.getAuthtokens().remove(token);
            
            MailUtil mailutil = new MailUtil(propertyUtil);
            try {
                mailutil.sendRespondMail(propertyUtil.getPropertyValue("email_admin"), "Freischaltung des Accounts für die Seite " + at.getSite(), "Freischaltung des Accounts: " + user.getName());
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(ClownfishSendConfirmMail.class.getName()).log(Level.SEVERE, null, ex);
            }

            String template = propertyUtil.getPropertyValue("template_confirmed");
            CfTemplate cftemplate = cftemplateService.findByName(template);
            
            response.setContentType("text/html");
            try (PrintWriter out = response.getWriter()) {
                out.print(cftemplate.getContent());
            } catch (IOException ex) {
                //LOGGER.error(ex.getMessage());
            }
        } else {
            String template = propertyUtil.getPropertyValue("template_notconfirmed");
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
