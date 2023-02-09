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
import static io.clownfish.clownfish.constants.ClownfishConst.AccessTypes.TYPE_CLASS;
import io.clownfish.clownfish.datamodels.AuthTokenClasscontent;
import io.clownfish.clownfish.datamodels.AuthTokenListClasscontent;
import io.clownfish.clownfish.datamodels.ClassDataOutput;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributetypeService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.utils.AccessManagerUtil;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
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
@WebServlet(name = "GetClasses", urlPatterns = {"/GetClasses"})
@Component
public class GetClasses extends HttpServlet {
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfAttributService cfattributService;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenListClasscontent authtokenlist;
    @Autowired AccessManagerUtil accessmanager;
        
    final transient Logger LOGGER = LoggerFactory.getLogger(GetClasses.class);

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
                    CfClass clazz = null;
                    List<CfClass> classList = new ArrayList<>();
                    String classid = request.getParameter("id");
                    if (classid != null) {
                        clazz = cfclassService.findById(Long.parseLong(classid));
                        // !ToDo: #95 check AccessManager
                        if (accessmanager.checkAccess(token, TYPE_CLASS.getValue(), BigInteger.valueOf(clazz.getId()))) {
                            classList.add(clazz);
                        }
                    }
                    String classname = request.getParameter("name");
                    if (classname != null) {
                        clazz = cfclassService.findByName(classname);
                        classList.clear();
                        // !ToDo: #95 check AccessManager
                        if (accessmanager.checkAccess(token, TYPE_CLASS.getValue(), BigInteger.valueOf(clazz.getId()))) {
                            classList.add(clazz);
                        }
                    }
                    if ((null == classid) && (null == classname)) {
                        // !ToDo: #95 check AccessManager
                        if ((null != token) && (!token.isEmpty())) {
                            AuthTokenClasscontent classcontent = authtokenlist.getAuthtokens().get(token);
                            if (null != classcontent) {
                                classList = cfclassService.findNotInList(BigInteger.valueOf(classcontent.getUser().getId()));
                            } else {
                                classList = cfclassService.findNotInList(BigInteger.valueOf(0L));
                            }
                        } else {
                            classList = cfclassService.findNotInList(BigInteger.valueOf(0L));
                        }
                    }
                    ArrayList<ClassDataOutput> classdataoutputList = new ArrayList<>();
                    for (CfClass classItem : classList) {
                        List<CfAttribut> attributList = cfattributService.findByClassref(classItem);
                        ClassDataOutput classdataoutput = new ClassDataOutput();
                        classdataoutput.setClazz(classItem);
                        classdataoutput.setAttributlist(attributList);
                        classdataoutputList.add(classdataoutput);
                    }
                    Gson gson = new Gson(); 
                    String json = gson.toJson(classdataoutputList);
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
