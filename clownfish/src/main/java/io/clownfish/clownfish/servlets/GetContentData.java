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
import io.clownfish.clownfish.datamodels.ContentDataOutput;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributetypeService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentKeywordService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.ContentUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
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
@WebServlet(name = "GetContentData", urlPatterns = {"/GetContentData"})
@Component
public class GetContentData extends HttpServlet {
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfClasscontentKeywordService cfcontentkeywordService;
    @Autowired transient CfKeywordService cfkeywordService;
    @Autowired transient CfAttributetypeService cfattributetypeService;
    @Autowired transient CfAttributService cfattributService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired ContentUtil contentUtil;
    @Autowired ApiKeyUtil apikeyutil;
        
    final transient Logger LOGGER = LoggerFactory.getLogger(GetContentData.class);

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
            if (apikeyutil.checkApiKey(apikey, "GetContentData")) {
                CfClasscontent content = null;
                String contentid = request.getParameter("contentid");
                if (contentid != null) {
                    content = cfclasscontentService.findById(Long.parseLong(contentid));
                }

                if (null != content) {
                    List<CfAttributcontent> attributcontentList = cfattributcontentService.findByClasscontentref(content);
                    ArrayList<HashMap> keyvals = contentUtil.getContentOutputKeyval(attributcontentList);
                    ArrayList<String> keywords = getAssetKeywords(content, true);
                    ContentDataOutput contentdataoutput = new ContentDataOutput();

                    contentdataoutput.setContent(content);
                    contentdataoutput.setKeywords(keywords);
                    contentdataoutput.setKeyvals(keyvals);

                    Gson gson = new Gson(); 
                    String json = gson.toJson(contentdataoutput);
                    response.setContentType("application/json;charset=UTF-8");
                    try (PrintWriter out = response.getWriter()) {
                        out.print(json);
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                }
            } else {
                PrintWriter out = response.getWriter();
                out.print("Wrong API KEY");
            }
        } catch (javax.persistence.NoResultException | java.lang.IllegalArgumentException ex) {
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print("No content");
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

    private ArrayList getAssetKeywords(CfClasscontent content, boolean toLower) {
        ArrayList<String> keywords = new ArrayList<>();
        List<CfClasscontentkeyword> keywordlist = cfcontentkeywordService.findByClassContentRef(content.getId());
        if (!keywordlist.isEmpty()) {
            for (CfClasscontentkeyword ak : keywordlist) {
                if (toLower) {
                    keywords.add(cfkeywordService.findById(ak.getCfClasscontentkeywordPK().getKeywordref()).getName().toLowerCase());
                } else {
                    keywords.add(cfkeywordService.findById(ak.getCfClasscontentkeywordPK().getKeywordref()).getName());
                }
            }
        }
        return keywords;
    }
}
