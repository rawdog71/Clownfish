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
import io.clownfish.clownfish.datamodels.AuthToken;
import io.clownfish.clownfish.datamodels.AuthTokenList;
import io.clownfish.clownfish.dbentities.CfUser;
import io.clownfish.clownfish.serviceinterface.CfUserService;
import io.clownfish.clownfish.utils.PasswordUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author SulzbachR
 */
@WebServlet(name = "BackendAuth", urlPatterns = {"/BackendAuth"})
@Component
public class BackendLoginServlet extends HttpServlet {
    @Autowired transient CfUserService cfuserService;
    @Autowired transient AuthTokenList authtokenlist;
    
    String email;
    String password;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(BackendLoginServlet.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> parameters = request.getParameterMap();
        
        parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("email") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
            email = values[0];
        });
        String inst_email = email;
        
        parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("password") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
            password = values[0];
        });
        String inst_password = password;
        AuthToken at = null;
        if ((null != inst_email) && (null != inst_password)) {
            try {
                CfUser cfuser = cfuserService.findByEmail(inst_email);
                String salt = cfuser.getSalt();
                String secure = PasswordUtil.generateSecurePassword(inst_password, salt);
                if (secure.compareTo(cfuser.getPasswort()) == 0) {
                    String token = AuthToken.generateToken(inst_password, salt);
                    at = new AuthToken(token, new DateTime().plusMinutes(60));      // Tokens valid for 60 minutes
                    authtokenlist.getAuthtokens().put(token, at);
                } else {
                    at = new AuthToken("", new DateTime());      // Invalid token
                }
            } catch (Exception ex) {
                at = new AuthToken("", new DateTime());      // Invalid token
            }
        }
        
        Gson gson = new Gson();
        String json = gson.toJson(at);
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(json);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
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
