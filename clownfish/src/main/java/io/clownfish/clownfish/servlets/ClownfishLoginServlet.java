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
import io.clownfish.clownfish.datamodels.AuthResponse;
import io.clownfish.clownfish.datamodels.AuthTokenClasscontent;
import io.clownfish.clownfish.datamodels.AuthTokenListClasscontent;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import io.clownfish.clownfish.utils.PasswordUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author SulzbachR
 */
@WebServlet(name = "Auth", urlPatterns = {"/Auth"})
@Component
public class ClownfishLoginServlet extends HttpServlet {

    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired transient CfAttributService cfattributService;
    @Autowired transient AuthTokenListClasscontent authtokenlist;
    @Autowired private ContentUtil contentUtil;
    @Autowired private HibernateUtil hibernateUtil;
    @Autowired PropertyUtil propertyUtil;
    String klasse, id, clearPw;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(ClownfishLoginServlet.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String inst_klasse;
        String inst_identifier;
        String salt;
        String inst_clearTextPw;
        AuthResponse ar = new AuthResponse();
        ar.setStatus(false);
        ar.setToken("");
        ar.setValiduntil(null);
        try {
            Map<String, String[]> parameters = request.getParameterMap();
            klasse = "";
            parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("class") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
                klasse = values[0];
            });
            clearPw = "";
            parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("clearPw") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
                clearPw = values[0];
            });
            id = "";
            parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("id") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
                id = values[0];
            });

            inst_klasse = klasse;
            inst_identifier = id;
            inst_clearTextPw = clearPw;

            CfClass cfclass = cfclassService.findByName(inst_klasse);
            List<CfClasscontent> classcontentList = cfclasscontentService.findByClassref(cfclass);
            CfAttribut attributField = cfattributService.findByNameAndClassref("email", cfclass);
            CfAttribut attributPassword = cfattributService.findByNameAndClassref("password", cfclass);
            CfAttribut attributAuth = cfattributService.findByNameAndClassref("valid", cfclass);
            CfAttribut attributLastlogin = cfattributService.findByNameAndClassref("lastlogin", cfclass);

            for (CfClasscontent classcontent : classcontentList) {
                CfAttributcontent attributContent = cfattributcontentService.findByAttributrefAndClasscontentref(attributField, classcontent);
                if ((null != attributContent) && (attributContent.getContentString().equals(inst_identifier))) {
                    long cref = attributContent.getClasscontentref().getId();
                    for (CfClasscontent classcontent1 : classcontentList) {
                        CfAttributcontent attributContent1 = cfattributcontentService.findByAttributrefAndClasscontentref(attributPassword, classcontent1);
                        CfAttributcontent attributContent2 = cfattributcontentService.findByAttributrefAndClasscontentref(attributAuth, classcontent1);
                        if (attributContent2.getContentBoolean()) {
                            salt = attributContent1.getSalt();
                            if (null != salt) {
                                String test = PasswordUtil.generateSecurePassword(inst_clearTextPw, salt);
                                test = URLEncoder.encode(test, StandardCharsets.UTF_8.toString());
                                if ((attributContent1.getContentString().compareTo(test) == 0) && (attributContent1.getClasscontentref().getId() == cref)) {
                                    ar.setStatus(PasswordUtil.verifyUserPassword(inst_clearTextPw, PasswordUtil.generateSecurePassword(inst_clearTextPw, salt), salt));
                                    if (ar.isStatus()) {
                                        ar.setToken(AuthTokenClasscontent.generateToken(inst_clearTextPw, salt));
                                        
                                        String logintime = propertyUtil.getPropertyValue("logintime");
                                        int lt = Integer.parseInt(logintime);
                                        
                                        AuthTokenClasscontent at = new AuthTokenClasscontent(ar.getToken(), new DateTime().plusMinutes(lt), classcontent1, "");
                                        ar.setValiduntil(at.getValiduntil());
                                        authtokenlist.getAuthtokens().put(ar.getToken(), at);
                                        
                                        CfAttributcontent attributContentLastlogin = cfattributcontentService.findByAttributrefAndClasscontentref(attributLastlogin, classcontent1);
                                        attributContentLastlogin = contentUtil.setAttributValue(attributContentLastlogin, new DateTime().toString(new DateTime().toString(DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss").withZone(DateTimeZone.forID("Europe/Berlin")))));
                                        cfattributcontentService.edit(attributContentLastlogin);
                                        hibernateUtil.updateContent(classcontent);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException ex) {
            ar.setStatus(false);
            ar.setToken("");
            ar.setValiduntil(null);
            LOGGER.error(ex.getMessage());
        }

        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            out.print(gson.toJson(ar));
        } catch (IOException ex) {
            //LOGGER.error(ex.getMessage());
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
