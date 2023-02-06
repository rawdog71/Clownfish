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
import io.clownfish.clownfish.datamodels.AssetListOutput;
import io.clownfish.clownfish.datamodels.AuthTokenList;
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAssetlistcontent;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistcontentService;
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
@WebServlet(name = "GetAssetLibraries", urlPatterns = {"/GetAssetLibraries"})
@Component
public class GetAssetLibraries extends HttpServlet {
    @Autowired transient CfAssetService cfassetService;
    @Autowired transient CfAssetlistService cfassetlistService;
    @Autowired transient CfAssetlistcontentService cfassetlistcontentService;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired transient AuthTokenList authtokenlist;
        
    final transient Logger LOGGER = LoggerFactory.getLogger(GetAssetLibraries.class);

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
                    CfAssetlist assetlist = null;
                    List<CfAssetlist> assetlistList = new ArrayList<>();
                    String assetlistid = request.getParameter("id");
                    if (assetlistid != null) {
                        assetlist = cfassetlistService.findById(Long.parseLong(assetlistid));
                        assetlistList.add(assetlist);
                    }
                    String assetlistname = request.getParameter("name");
                    if (assetlistname != null) {
                        assetlist = cfassetlistService.findByName(assetlistname);
                        assetlistList.clear();
                        assetlistList.add(assetlist);
                    }
                    if ((null == assetlistid) && (null == assetlistname)) {
                        assetlistList = cfassetlistService.findAll();
                    }
                    // ToDo: #95 check AccessManager
                    ArrayList<AssetListOutput> assetlistoutputList = new ArrayList<>();
                    for (CfAssetlist assetlistItem : assetlistList) {
                        List<CfAsset> assetList = new ArrayList<>();
                        List<CfAssetlistcontent> assetlistcontentList = cfassetlistcontentService.findByAssetlistref(assetlistItem.getId());
                        for (CfAssetlistcontent assetlistcontent : assetlistcontentList) {
                            assetList.add(cfassetService.findById(assetlistcontent.getCfAssetlistcontentPK().getAssetref()));
                        }
                        AssetListOutput assetlistoutput = new AssetListOutput();
                        assetlistoutput.setAssetlist(assetlistItem);
                        assetlistoutput.setAssets(assetList);
                        assetlistoutputList.add(assetlistoutput);
                    }

                    Gson gson = new Gson(); 
                    String json = gson.toJson(assetlistoutputList);
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
                out.print("No asset lists");
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
