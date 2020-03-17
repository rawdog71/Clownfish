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
import io.clownfish.clownfish.datamodels.AttributDef;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfAttributetype;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributetypeService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentKeywordService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.datamodels.ContentOutput;
import io.clownfish.clownfish.utils.PasswordUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
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
@WebServlet(name = "GetContent", urlPatterns = {"/GetContent"})
@Component
public class GetContent extends HttpServlet {
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfAttributService cfattributService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired transient CfAttributetypeService cfattributetypeService;
    @Autowired transient CfListService cflistService;
    @Autowired transient CfListcontentService cflistcontentService;
    @Autowired transient CfClasscontentKeywordService cfclasscontentkeywordService;
    @Autowired transient CfKeywordService cfkeywordService;
    
    private static transient @Getter @Setter String klasse;
    private static transient @Getter @Setter String datalist;
    private static transient @Getter @Setter HashMap<String, String> searchmap;
    private static transient @Getter @Setter ArrayList<String> searchkeywords;
    private static transient @Getter @Setter HashMap<String, String> outputmap;
    private static transient @Getter @Setter ArrayList<ContentOutput> outputlist;
    private List<CfListcontent> listcontent = null;
    
    final transient Logger logger = LoggerFactory.getLogger(GetAsset.class);
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
        outputlist = new ArrayList<>();
        outputmap = new HashMap<>();
        Map<String, String[]> parameters = request.getParameterMap();
        parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("class") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
            klasse = values[0];
        });
        parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("datalist") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
            datalist = values[0];
        });
        listcontent = null;
        if (null != datalist) {
            CfList dataList = cflistService.findByName(datalist);
            listcontent = cflistcontentService.findByListref(dataList.getId());
        }
        searchmap = new HashMap<>();
        parameters.keySet().stream().filter((paramname) -> (paramname.startsWith("search$"))).forEach((paramname) -> {
            String[] keys = paramname.split("\\$");
            int counter = 0;
            for (String key : keys) {
                if ((counter > 0) && ((counter%2) == 0)) {
                    searchmap.put(keys[counter-1], keys[counter]);
                }
                counter++;
            }
        });
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
        CfClass cfclass = cfclassService.findByName(klasse);
        List<CfClasscontent> classcontentList = cfclasscontentService.findByClassref(cfclass);
        boolean found = true;
        for (CfClasscontent classcontent : classcontentList) {
            boolean inList = true;
            // Check if content is in datalist 
            if (null != listcontent) {
                boolean foundinlist = false;
                for (CfListcontent lc : listcontent) {
                    if (lc.getCfListcontentPK().getClasscontentref() == classcontent.getId()) {
                        foundinlist = true;
                        break;
                    }
                }
                inList = foundinlist;
            }
            if (inList) {
                List<CfAttributcontent> attributcontentList = cfattributcontentService.findByClasscontentref(classcontent);
                found = true;
                for (CfAttributcontent attributcontent : attributcontentList) {
                    CfAttribut knattribut = cfattributService.findById(attributcontent.getAttributref().getId());
                    for (String searchcontent : searchmap.keySet()) {
                        String searchvalue = searchmap.get(searchcontent);
                        String comparator = "eq";
                        if (searchvalue.startsWith(":co:")) {
                            comparator = "co";
                            searchvalue = searchvalue.substring(4);
                        }
                        if (searchvalue.startsWith(":eq:")) {
                            comparator = "eq";
                            searchvalue = searchvalue.substring(4);
                        }
                        if (searchvalue.startsWith(":ew:")) {
                            comparator = "ew";
                            searchvalue = searchvalue.substring(4);
                        }
                        if (searchvalue.startsWith(":sw:")) {
                            comparator = "sw";
                            searchvalue = searchvalue.substring(4);
                        }
                        searchvalue = searchvalue.toLowerCase();
                        if (knattribut.getName().compareToIgnoreCase(searchcontent) == 0) {
                            long attributtypeid = knattribut.getAttributetype().getId();
                            AttributDef attributdef = getAttributContent(attributtypeid, attributcontent);
                            if (null != attributdef) {
                                if (attributdef.getType().compareToIgnoreCase("hashstring") == 0) {
                                    String salt = attributcontent.getSalt();
                                    if (salt != null) {
                                        searchvalue = PasswordUtil.generateSecurePassword(searchvalue, salt);
                                    }
                                }
                                if (attributdef.getValue() == null) {
                                    found = false;
                                } else {
                                    if ((comparator.compareToIgnoreCase("co") == 0) && (!attributdef.getValue().toLowerCase().contains(searchvalue))) {
                                        found = false;
                                    }
                                    if ((comparator.compareToIgnoreCase("sw") == 0) && (!attributdef.getValue().toLowerCase().startsWith(searchvalue))) {
                                        found = false;
                                    }
                                    if ((comparator.compareToIgnoreCase("eq") == 0) && (attributdef.getValue().toLowerCase().compareToIgnoreCase(searchvalue) != 0)) {
                                        found = false;
                                    }
                                    if ((comparator.compareToIgnoreCase("ew") == 0) && (!attributdef.getValue().toLowerCase().endsWith(searchvalue))) {
                                        found = false;
                                    }
                                }
                            }
                        }
                    }
                }
                // Check the keyword filter (MUST find all keywords)
                if (searchkeywords.size() > 0) {
                    ArrayList contentkeywords = getContentOutputKeywords(classcontent, true);
                    boolean dummyfound = true;
                    for (String keyword : searchkeywords) {
                        if (!contentkeywords.contains(keyword)) {
                            dummyfound = false;
                        }
                    }
                    if (dummyfound) {
                        found = true;
                    } else {
                        found = false;
                    }
                }
                
                if (found) {
                    ContentOutput co = new ContentOutput();
                    co.setKeyvals(getContentOutputKeyval(attributcontentList));
                    co.setKeywords(getContentOutputKeywords(classcontent, false));
                    outputlist.add(co);
                }
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
        processRequest(request, response);
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

    
    private AttributDef getAttributContent(long attributtypeid, CfAttributcontent attributcontent) {
        CfAttributetype knattributtype = cfattributetypeService.findById(attributtypeid);
        switch (knattributtype.getName()) {
            case "boolean":
                if (null != attributcontent.getContentBoolean()) {
                    return new AttributDef(attributcontent.getContentBoolean().toString(), "boolean");
                } else {
                    return new AttributDef(null, "boolean");
                }
            case "string":
                if (null != attributcontent.getContentString()) {
                    return new AttributDef(attributcontent.getContentString(), "string");
                } else {
                    return new AttributDef(null, "string");
                }
            case "hashstring":
                if (null != attributcontent.getContentString()) {
                    return new AttributDef(attributcontent.getContentString(), "hashstring");
                } else {
                    return new AttributDef(null, "hashstring");
                }
            case "integer":
                if (null != attributcontent.getContentInteger()) {
                    return new AttributDef(attributcontent.getContentInteger().toString(), "integer");
                } else {
                    return new AttributDef(null, "integer");
                }
            case "real":
                if (null != attributcontent.getContentReal()) {
                    return new AttributDef(attributcontent.getContentReal().toString(), "real");
                } else {
                    return new AttributDef(null, "real");
                }
            case "htmltext":
                if (null != attributcontent.getContentText()) {
                    return new AttributDef(attributcontent.getContentText(), "htmltext");
                } else {
                    return new AttributDef(null, "htmltext");
                }
            case "markdown":
                if (null != attributcontent.getContentText()) {
                    return new AttributDef(attributcontent.getContentText(), "markdown");
                } else {
                    return new AttributDef(null, "markdown");
                }
            case "datetime":
                if (null != attributcontent.getContentDate()) {
                    return new AttributDef(attributcontent.getContentDate().toString(), "datetime");
                } else {
                    return new AttributDef(null, "datetime");
                }
            case "media":
                if (null != attributcontent.getContentInteger()) {
                    return new AttributDef(attributcontent.getContentInteger().toString(), "media");
                } else {
                    return new AttributDef(null, "media");
                }
            case "text":
                if (null != attributcontent.getContentText()) {
                    return new AttributDef(attributcontent.getContentText().toString(), "text");
                } else {
                    return new AttributDef(null, "text");
                }
            case "classref":
                if (null != attributcontent.getContentInteger()) {
                    return new AttributDef(attributcontent.getContentInteger().toString(), "classref");
                } else {
                    return new AttributDef(null, "classref");
                }
            default:
                return null;
        }
    }
    
    private ArrayList getContentOutputKeyval(List<CfAttributcontent> attributcontentList) {
        ArrayList<HashMap> output = new ArrayList<>();
        HashMap<String, String> dummyoutputmap = new HashMap<>();
        attributcontentList.stream().forEach((attributcontent) -> {
            CfAttribut knattribut = cfattributService.findById(attributcontent.getAttributref().getId());
            long attributtypeid = knattribut.getAttributetype().getId();
            AttributDef attributdef = getAttributContent(attributtypeid, attributcontent);
            if (attributdef.getType().compareToIgnoreCase("hashstring") != 0) {
                dummyoutputmap.put(knattribut.getName(), attributdef.getValue());
            }
        });
        output.add(dummyoutputmap);
        return output;
    }
    
    private ArrayList getContentOutputKeywords(CfClasscontent classcontent, boolean toLower) {
        ArrayList<String> keywords = new ArrayList<>();
        List<CfClasscontentkeyword> keywordlist = cfclasscontentkeywordService.findByClassContentRef(classcontent.getId());
        if (keywordlist.size() > 0) {
            for (CfClasscontentkeyword cck : keywordlist) {
                if (toLower) {
                    keywords.add(cfkeywordService.findById(cck.getCfClasscontentkeywordPK().getKeywordref()).getName().toLowerCase());
                } else {
                    keywords.add(cfkeywordService.findById(cck.getCfClasscontentkeywordPK().getKeywordref()).getName());
                }
            }
        }
        return keywords;
    }
}
