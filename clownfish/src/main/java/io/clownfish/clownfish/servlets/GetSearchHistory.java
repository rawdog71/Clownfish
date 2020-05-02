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
import io.clownfish.clownfish.dbentities.CfKeyword;
import io.clownfish.clownfish.dbentities.CfSearchhistory;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfSearchhistoryService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@WebServlet(name = "GetSearchHistory", urlPatterns = {"/GetSearchHistory"}, asyncSupported = true)
@Component
public class GetSearchHistory extends HttpServlet {
    @Autowired transient CfSearchhistoryService cfsearchhistoryService;
    @Autowired transient PropertyUtil propertyUtil;
    @Autowired transient CfKeywordService cfkeywordservice;
    @Autowired ApiKeyUtil apikeyutil;
    
    final transient Logger logger = LoggerFactory.getLogger(GetSearchHistory.class);
    
    public GetSearchHistory() {
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        final AsyncContext acontext = request.startAsync();
        
        acontext.start(() -> {
            try {
                String apikey = acontext.getRequest().getParameter("apikey");
                if (apikeyutil.checkApiKey(apikey, "GetSearchHistory")) {
                    String expression = acontext.getRequest().getParameter("expression");
                    int max = 25;
                    String maxentry = acontext.getRequest().getParameter("maxentry");
                    if (null != maxentry) {
                        max = Integer.parseInt(maxentry);
                    }
                    List<CfSearchhistory> searchhistory = cfsearchhistoryService.findByExpressionBeginning(expression);
                    ArrayList<String> searchlist = new ArrayList<>();
                    int counter = 0;
                    for (CfSearchhistory search : searchhistory) {
                        if (counter < max) {
                            counter++;
                            searchlist.add(search.getExpression());
                        } else {
                            break;
                        }
                    }

                    // Add keywords to the output
                    List<CfKeyword> keywordlist = cfkeywordservice.findByNameBeginning(expression);
                    for (CfKeyword keyword : keywordlist) {
                        if (counter < max) {
                            if (!searchlist.contains(keyword.getName().toLowerCase())) {
                                counter++;
                                searchlist.add(keyword.getName().toLowerCase());
                            }
                        } else {
                            break;
                        }
                    }

                    Gson gson = new Gson(); 
                    String json = gson.toJson(searchlist);
                    response.setContentType("application/json;charset=UTF-8");
                    try (PrintWriter out = response.getWriter()) {
                        out.print(json);
                    } catch (IOException ex) {
                        logger.error(ex.getMessage());
                    }
                    acontext.complete();
                } else {
                    OutputStream outputStream = acontext.getResponse().getOutputStream();
                    outputStream.close();
                    acontext.complete();
                }
            } catch (javax.persistence.NoResultException | java.lang.IllegalArgumentException ex) {
                acontext.getResponse().setContentType("application/json;charset=UTF-8");
                try (PrintWriter out = acontext.getResponse().getWriter()) {
                    out.print("[]");
                    acontext.complete();
                } catch (IOException ex1) {
                    logger.error(ex1.getMessage());
                    acontext.complete();
                }
            } catch (IOException ex) {
                logger.error(ex.getMessage());
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
