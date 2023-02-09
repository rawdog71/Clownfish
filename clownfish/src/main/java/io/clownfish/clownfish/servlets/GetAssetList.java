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
import static io.clownfish.clownfish.constants.ClownfishConst.AccessTypes.TYPE_ASSET;
import io.clownfish.clownfish.datamodels.AuthTokenClasscontent;
import io.clownfish.clownfish.datamodels.AuthTokenListClasscontent;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetkeyword;
import io.clownfish.clownfish.serviceinterface.CfAssetKeywordService;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
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
@WebServlet(name = "GetAssetList", urlPatterns = {"/GetAssetList"})
@Component
public class GetAssetList extends HttpServlet {
    @Autowired transient CfAssetService cfassetService;
    @Autowired transient CfAssetKeywordService cfassetkeywordService;
    @Autowired transient CfKeywordService cfkeywordService;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenListClasscontent authtokenlist;
    @Autowired AccessManagerUtil accessmanager;
        
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
            String token = request.getParameter("token");
            if (apikeyutil.checkApiKey(apikey, "RestService")) {
                List<CfAsset> assetlist;
                // !ToDo: #95 check AccessManager
                if ((null != token) && (!token.isEmpty())) {
                    AuthTokenClasscontent classcontent = authtokenlist.getAuthtokens().get(token);
                    if (null != classcontent) {
                        assetlist = cfassetService.findByPublicuseAndScrappedNotInList(true, false, BigInteger.valueOf(classcontent.getUser().getId()));
                    } else {
                        assetlist = cfassetService.findByPublicuseAndScrappedNotInList(true, false, BigInteger.valueOf(0L));
                    }
                } else {
                    assetlist = cfassetService.findByPublicuseAndScrappedNotInList(true, false, BigInteger.valueOf(0L));
                }

                Gson gson = new Gson(); 
                String json = gson.toJson(assetlist);
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
