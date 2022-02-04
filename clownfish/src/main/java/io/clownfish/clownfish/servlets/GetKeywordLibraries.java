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
import io.clownfish.clownfish.datamodels.KeywordListOutput;
import io.clownfish.clownfish.dbentities.CfKeyword;
import io.clownfish.clownfish.dbentities.CfKeywordlist;
import io.clownfish.clownfish.dbentities.CfKeywordlistcontent;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfKeywordlistService;
import io.clownfish.clownfish.serviceinterface.CfKeywordlistcontentService;
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
@WebServlet(name = "GetKeywordLibraries", urlPatterns = {"/GetKeywordLibraries"})
@Component
public class GetKeywordLibraries extends HttpServlet {
    @Autowired transient CfKeywordService cfkeywordService;
    @Autowired transient CfKeywordlistService cfkeywordlistService;
    @Autowired transient CfKeywordlistcontentService cfkeywordlistcontentService;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenList authtokenlist;
        
    final transient Logger LOGGER = LoggerFactory.getLogger(GetKeywordLibraries.class);

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
            String apikey = request.getParameter("apikey");
            String token = request.getParameter("token");
            if (authtokenlist.checkValidToken(token)) {
                if (apikeyutil.checkApiKey(apikey, "GetKeywordLibraries")) {
                    CfKeywordlist keywordlist = null;
                    List<CfKeywordlist> keywordlistList = new ArrayList<>();
                    String keywordlistid = request.getParameter("id");
                    if (keywordlistid != null) {
                        keywordlist = cfkeywordlistService.findById(Long.parseLong(keywordlistid));
                        keywordlistList.add(keywordlist);
                    }
                    String keywordlistname = request.getParameter("name");
                    if (keywordlistname != null) {
                        keywordlist = cfkeywordlistService.findByName(keywordlistname);
                        keywordlistList.clear();
                        keywordlistList.add(keywordlist);
                    }
                    if ((null == keywordlistid) && (null == keywordlistname)) {
                        keywordlistList = cfkeywordlistService.findAll();
                    }

                    ArrayList<KeywordListOutput> keywordlistoutputList = new ArrayList<>();
                    for (CfKeywordlist keywordlistItem : keywordlistList) {
                        List<CfKeyword> keywordList = new ArrayList<>();
                        List<CfKeywordlistcontent> keywordlistcontentList = cfkeywordlistcontentService.findByKeywordlistref(keywordlistItem.getId());
                        for (CfKeywordlistcontent keywordlistcontent : keywordlistcontentList) {
                            keywordList.add(cfkeywordService.findById(keywordlistcontent.getCfKeywordlistcontentPK().getKeywordref()));
                        }
                        KeywordListOutput keywordlistoutput = new KeywordListOutput();
                        keywordlistoutput.setKeywordlist(keywordlistItem);
                        keywordlistoutput.setKeywords(keywordList);
                        keywordlistoutputList.add(keywordlistoutput);
                    }

                    Gson gson = new Gson(); 
                    String json = gson.toJson(keywordlistoutputList);
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
                out.print("No keyword lists");
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
