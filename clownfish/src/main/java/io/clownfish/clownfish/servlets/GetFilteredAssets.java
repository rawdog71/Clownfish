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
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetkeyword;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAssetlistcontent;
import io.clownfish.clownfish.serviceinterface.CfAssetKeywordService;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistcontentService;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.utils.PropertyUtil;
import java.io.IOException;
import java.io.PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@WebServlet(name = "GetFilteredAssets", urlPatterns = {"/GetFilteredAssets"}, asyncSupported = true)
@Component
public class GetFilteredAssets extends HttpServlet {
    @Autowired transient CfAssetService cfassetService;
    @Autowired transient PropertyUtil propertyUtil;
    @Autowired transient CfAssetlistService cfassetlistService;
    @Autowired transient CfAssetlistcontentService cfassetlistcontentService;
    @Autowired transient CfAssetKeywordService cfassetkeywordService;
    @Autowired transient CfKeywordService cfkeywordService;
    
    private static transient @Getter @Setter String assetlibrary;
    private static transient @Getter @Setter ArrayList<String> searchkeywords;
    private List<CfAssetlistcontent> assetlistcontent = null;
    private static transient @Getter @Setter HashMap<String, String> outputmap;
    private static transient @Getter @Setter ArrayList<AssetDataOutput> outputlist;
    
    final transient Logger logger = LoggerFactory.getLogger(GetFilteredAssets.class);
    
    public GetFilteredAssets() {
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
        outputlist = new ArrayList<>();
        outputmap = new HashMap<>();
        Map<String, String[]> parameters = request.getParameterMap();
        parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("assetlibrary") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
            assetlibrary = values[0];
        });
        
        assetlistcontent = null;
        if (null != assetlibrary) {
            CfAssetlist assetList = cfassetlistService.findByName(assetlibrary);
            assetlistcontent = cfassetlistcontentService.findByAssetlistref(assetList.getId());
        }
        
        searchkeywords = new ArrayList<>();
        parameters.keySet().stream().filter((paramname) -> (paramname.startsWith("keywords"))).forEach((paramname) -> {
            String[] keys = paramname.split("\\$");
            int counter = 0;
            for (String key : keys) {
                if (counter > 0) {
                    searchkeywords.add(key);
                }
                counter++;
            }
        });
        
        boolean found = true;
        for (CfAssetlistcontent assetcontent : assetlistcontent) {
            CfAsset asset = cfassetService.findById(assetcontent.getCfAssetlistcontentPK().getAssetref());
            
            // Check the keyword filter (at least one keyword must be found (OR))
            if (searchkeywords.size() > 0) {
                ArrayList contentkeywords = getContentOutputKeywords(asset, true);
                boolean dummyfound = false;
                for (String keyword : searchkeywords) {
                    if (contentkeywords.contains(keyword.toLowerCase())) {
                        dummyfound = true;
                    }
                }
                if (dummyfound) {
                    found = true;
                } else {
                    found = false;
                }
            } else {
                found = true;
            }
            
            if (found) {
                AssetDataOutput ao = new AssetDataOutput();
                ao.setAsset(asset);
                ao.setKeywords(getContentOutputKeywords(asset, false));
                outputlist.add(ao);
            }
        }
        
        if (!found) {
            outputmap.put("contentfound", "false");
        }
        Gson gson = new Gson(); 
        String json = gson.toJson(outputlist);
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(json);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
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

    private ArrayList getContentOutputKeywords(CfAsset asset, boolean toLower) {
        ArrayList<String> keywords = new ArrayList<>();
        List<CfAssetkeyword> keywordlist = cfassetkeywordService.findByAssetRef(asset.getId());
        if (keywordlist.size() > 0) {
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
