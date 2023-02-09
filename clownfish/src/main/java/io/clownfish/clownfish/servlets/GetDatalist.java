/*
 * Copyright 2020 sulzbachr.
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
import static io.clownfish.clownfish.constants.ClownfishConst.AccessTypes.TYPE_CONTENT;
import static io.clownfish.clownfish.constants.ClownfishConst.AccessTypes.TYPE_CONTENTLIST;
import io.clownfish.clownfish.datamodels.ContentOutput;
import io.clownfish.clownfish.datamodels.DatalistOutput;
import io.clownfish.clownfish.datamodels.GetContentParameter;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.utils.AccessManagerUtil;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.ContentUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@WebServlet(name = "GetDatalist", urlPatterns = {"/GetDatalist"})
@Component
public class GetDatalist extends HttpServlet {
    @Autowired transient CfListService cflistService;
    @Autowired transient CfListcontentService cflistcontentService;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired ContentUtil contentUtil;
    @Autowired ApiKeyUtil apikeyutil;
    @Autowired AccessManagerUtil accessmanager;
    
    private static transient @Getter @Setter String name;
    private static transient @Getter @Setter String apikey;
    private static transient @Getter @Setter String token;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(GetDatalist.class);
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
        StringBuffer jb = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                jb.append(line);
            }
        } catch (IOException e) {
            /*report an error*/ 
        }

        Gson gson = new Gson();
        GetContentParameter gcp = gson.fromJson(jb.toString(), GetContentParameter.class);
        if (null != gcp) {
            processRequest(gcp, response);
        }
        
        String json = gson.toJson(gcp);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(json);
        out.flush();
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
        DatalistOutput datalistoutput = new DatalistOutput();
        ArrayList<ContentOutput> outputlist = new ArrayList<>();
        String inst_apikey = "";
        String inst_name = "";
        String inst_token = "";
        
        Map<String, String[]> parameters = request.getParameterMap();
        parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("apikey") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
            apikey = values[0];
        });
        inst_apikey = apikey;
        parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("token") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
            token = values[0];
        });
        inst_token = token;
        if (apikeyutil.checkApiKey(inst_apikey, "RestService")) {
            name = "";
            parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("name") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
                name = values[0];
            });
            inst_name = name;
            CfList cflist = cflistService.findByName(inst_name);
            // !ToDo: #95 check AccessManager
            if (accessmanager.checkAccess(inst_token, TYPE_CONTENTLIST.getValue(), BigInteger.valueOf(cflist.getId()))) {
                List<CfListcontent> listcontentList = cflistcontentService.findByListref(cflist.getId());

                List<CfClasscontent> classcontentList = new ArrayList<>();
                for (CfListcontent listcontent : listcontentList) {
                    CfClasscontent classcontent = cfclasscontentService.findById(listcontent.getCfListcontentPK().getClasscontentref());
                    if (null != classcontent) {
                        // !ToDo: #95 check AccessManager
                        if (accessmanager.checkAccess(inst_token, TYPE_CONTENT.getValue(), BigInteger.valueOf(classcontent.getId()))) {
                            classcontentList.add(classcontent);
                        }
                    } else {
                        LOGGER.warn("Classcontent does not exist: " + inst_name + " - " + listcontent.getCfListcontentPK().getClasscontentref());
                    }
                }

                for (CfClasscontent classcontent : classcontentList) {    
                    List<CfAttributcontent> attributcontentList = cfattributcontentService.findByClasscontentref(classcontent);
                    ContentOutput co = new ContentOutput();
                    co.setIdentifier(classcontent.getName());
                    co.setKeyvals(contentUtil.getContentOutputKeyval(attributcontentList));
                    co.setKeywords(contentUtil.getContentOutputKeywords(classcontent, false));
                    outputlist.add(co);
                }

                datalistoutput.setCflist(cflist);
                datalistoutput.setOutputlist(outputlist);
                Gson gson = new Gson(); 
                String json = gson.toJson(datalistoutput);
                response.setContentType("application/json;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.print(json);
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
        } else {
            PrintWriter out = null;
            try {
                out = response.getWriter();
                out.print("Wrong API KEY");
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            } finally {
                out.close();
            }
        }    
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param gcp GetContentParameters
     * @param response servlet response
     * @return GetContentParameters
     */
    protected GetContentParameter processRequest(GetContentParameter gcp, HttpServletResponse response) {
        DatalistOutput datalistoutput = new DatalistOutput();
        ArrayList<ContentOutput> outputlist = new ArrayList<>();
        
        outputlist = new ArrayList<>();
        //outputmap = new HashMap<>();
        apikey = gcp.getApikey();
        if (apikeyutil.checkApiKey(apikey, "RestService")) {
            String inst_name = "";
            int range_start = 0;
            int range_end = 0;
            if ((null != gcp.getRange()) && (!gcp.getRange().isEmpty())) {
                if (gcp.getRange().contains("-")) {
                    String[] ranges = gcp.getRange().split("-");
                    range_start = Integer.parseInt(ranges[0]);
                    range_end = Integer.parseInt(ranges[1]);
                    if (range_start > range_end) {
                        int dummy = range_start;
                        range_start = range_end;
                        range_end = dummy;
                    }
                } else {
                    range_start = Integer.parseInt(gcp.getRange());
                    range_end = range_start;
                }
            }
            
            name = "";
            name = gcp.getListname();
            inst_name = name;
            CfList cflist = cflistService.findByName(inst_name);
            List<CfListcontent> listcontentList = cflistcontentService.findByListref(cflist.getId());
            
            List<CfClasscontent> classcontentList = new ArrayList<>();
            for (CfListcontent listcontent : listcontentList) {
                CfClasscontent classcontent = cfclasscontentService.findById(listcontent.getCfListcontentPK().getClasscontentref());
                if (null != classcontent) {
                    // ToDo: #95 check AccessManager
                    classcontentList.add(classcontent);
                } else {
                    LOGGER.warn("Classcontent does not exist: " + inst_name + " - " + listcontent.getCfListcontentPK().getClasscontentref());
                }
            }
            
            for (CfClasscontent classcontent : classcontentList) {    
                List<CfAttributcontent> attributcontentList = cfattributcontentService.findByClasscontentref(classcontent);
                ContentOutput co = new ContentOutput();
                co.setIdentifier(classcontent.getName());
                co.setKeyvals(contentUtil.getContentOutputKeyval(attributcontentList));
                co.setKeywords(contentUtil.getContentOutputKeywords(classcontent, false));
                outputlist.add(co);
            }

            datalistoutput.setCflist(cflist);
            datalistoutput.setOutputlist(outputlist);
            Gson gson = new Gson();
            String json = gson.toJson(datalistoutput);
            gcp.setJson(json);
            gcp.setReturncode("TRUE");
            return gcp;
        } else {
            gcp.setReturncode("FALSE");
            gcp.setJson("[]");
            return gcp;
        }    
    }
}
