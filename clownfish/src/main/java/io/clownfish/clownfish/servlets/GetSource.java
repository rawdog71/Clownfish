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
import io.clownfish.clownfish.datamodels.GetContentParameter;
import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.serviceinterface.CfJavascriptService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.JavascriptUtil;
import io.clownfish.clownfish.utils.StylesheetUtil;
import io.clownfish.clownfish.utils.TemplateUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet(name = "GetSource", urlPatterns = {"/GetSource"})
@Component
public class GetSource extends HttpServlet {
    @Autowired transient CfTemplateService cftemplateService;
    @Autowired transient CfStylesheetService cfstylesheetService;
    @Autowired transient CfJavascriptService cfjavascriptService;
    @Autowired TemplateUtil templateUtil;
    @Autowired JavascriptUtil javascriptUtil;
    @Autowired StylesheetUtil stylesheetUtil;
    @Autowired ApiKeyUtil apikeyutil;
    
    private static transient @Getter @Setter String name;
    private static transient @Getter @Setter String type;
    private static transient @Getter @Setter long version;
    private static transient @Getter @Setter String apikey;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(GetSource.class);
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String inst_name;
        String inst_type;
        long inst_version;
        String inst_apikey = "";
        
        Map<String, String[]> parameters = request.getParameterMap();
        parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("apikey") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
            apikey = values[0];
        });
        
        inst_apikey = apikey;
        if (apikeyutil.checkApiKey(inst_apikey, "GetContent")) {
            name = "";
            parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("name") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
                name = values[0];
            });
            inst_name = name;
            type = "";
            parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("type") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
                type = values[0];
            });
            inst_type = type;
            version = 0;
            parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("version") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
                version = Long.parseLong(values[0]);
            });
            inst_version = version;
            
            String source = "";
            long maxversion = 0;
            
            switch (inst_type) {
                case "template":
                    CfTemplate cftemplate = cftemplateService.findByName(inst_name);
                    maxversion = templateUtil.getCurrentVersionNumber(inst_name);
                    
                    if (inst_version != 0) {
                        if ((inst_version > maxversion) || (inst_version < 0)) {
                            inst_version = maxversion;
                        }
                    } else {
                        inst_version = maxversion;
                    }
                    source = templateUtil.getVersion(cftemplate.getId(), inst_version);
                    break;
                case "stylesheet":
                    CfStylesheet cfstylesheet = cfstylesheetService.findByName(inst_name);
                    maxversion = stylesheetUtil.getCurrentVersionNumber(inst_name);
                    
                    if (inst_version != 0) {
                        if ((inst_version > maxversion) || (inst_version < 0)) {
                            inst_version = maxversion;
                        }
                    } else {
                        inst_version = maxversion;
                    }
                    source = stylesheetUtil.getVersion(cfstylesheet.getId(), inst_version);
                    break;
                case "javascript":
                    CfJavascript cfjavascript = cfjavascriptService.findByName(inst_name);
                    maxversion = javascriptUtil.getCurrentVersionNumber(inst_name);
                    
                    if (inst_version != 0) {
                        if ((inst_version > maxversion) || (inst_version < 0)) {
                            inst_version = maxversion;
                        }
                    } else {
                        inst_version = maxversion;
                    }
                    
                    source = javascriptUtil.getVersion(cfjavascript.getId(), inst_version);
                    break;
            }
            
            PrintWriter out = null;
            try {
                out = response.getWriter();
                out.print(source);
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            } finally {
                out.close();
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
        } catch (Exception e) {
            /*report an error*/ 
        }

        Gson gson = new Gson();
        GetContentParameter gcp = gson.fromJson(jb.toString(), GetContentParameter.class);
        //processRequest(gcp, response);
        
        String json = gson.toJson(gcp);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(json);
        out.flush();
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
