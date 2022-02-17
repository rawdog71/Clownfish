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
import io.clownfish.clownfish.datamodels.AssetDataOutput;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetkeyword;
import io.clownfish.clownfish.serviceinterface.CfAssetKeywordService;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
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
@WebServlet(name = "GetAssetData", urlPatterns = {"/GetAssetData"})
@Component
public class GetAssetData extends HttpServlet {
    @Autowired transient CfAssetService cfassetService;
    @Autowired transient CfAssetKeywordService cfassetkeywordService;
    @Autowired transient CfKeywordService cfkeywordService;
    @Autowired ApiKeyUtil apikeyutil;
        
    final transient Logger LOGGER = LoggerFactory.getLogger(GetAsset.class);

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
            if (apikeyutil.checkApiKey(apikey, "RestService")) {
                CfAsset asset = null;
                String imagefilename = request.getParameter("file");
                if (imagefilename != null) {
                    asset = cfassetService.findByName(imagefilename);
                    if (null != asset) {
                        imagefilename = asset.getName();
                    }
                }
                String mediaid = request.getParameter("mediaid");
                if (mediaid != null) {
                    asset = cfassetService.findById(Long.parseLong(mediaid));
                    if (null != asset) {
                        imagefilename = asset.getName();
                    }
                }

                if (null != asset) {
                    if (!asset.isScrapped()) {
                        ArrayList<String> keywords = getAssetKeywords(asset, true);
                        AssetDataOutput assetdataoutput = new AssetDataOutput();

                        assetdataoutput.setAsset(asset);
                        assetdataoutput.setKeywords(keywords);

                        Gson gson = new Gson(); 
                        String json = gson.toJson(assetdataoutput);
                        response.setContentType("application/json;charset=UTF-8");
                        try (PrintWriter out = response.getWriter()) {
                            out.print(json);
                        } catch (IOException ex) {
                            LOGGER.error(ex.getMessage());
                        }
                    }
                } else {
                    PrintWriter out = response.getWriter();
                    out.print("No asset found");
                }
            } else {
                PrintWriter out = response.getWriter();
                out.print("Wrong API KEY");
            }
        } catch (javax.persistence.NoResultException | java.lang.IllegalArgumentException ex) {
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print("No image");
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

    private ArrayList getAssetKeywords(CfAsset asset, boolean toLower) {
        ArrayList<String> keywords = new ArrayList<>();
        List<CfAssetkeyword> keywordlist = cfassetkeywordService.findByAssetRef(asset.getId());
        if (!keywordlist.isEmpty()) {
            for (CfAssetkeyword ak : keywordlist) {
                if (toLower) {
                    keywords.add(cfkeywordService.findById(ak.getCfAssetkeywordPK().getKeywordref()).getName().toLowerCase());
                } else {
                    keywords.add(cfkeywordService.findById(ak.getCfAssetkeywordPK().getKeywordref()).getName());
                }
            }
        }
        return keywords;
    }
}
