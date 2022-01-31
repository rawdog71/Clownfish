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
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
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
import io.clownfish.clownfish.datamodels.GetContentParameter;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.ContentUtil;
import java.io.BufferedReader;
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
    @Autowired transient CfClasscontentKeywordService cfcontentkeywordService;
    @Autowired ContentUtil contentUtil;
    @Autowired ApiKeyUtil apikeyutil;
    
    private static transient @Getter @Setter String klasse;
    private static transient @Getter @Setter String identifier;
    private static transient @Getter @Setter String datalist;
    private static transient @Getter @Setter String apikey;
    private static transient @Getter @Setter String range;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(GetContent.class);
    
    private class SearchValues {
        private @Getter @Setter String comparartor;
        private @Getter @Setter String searchvalue;
        
        SearchValues(String comparator, String searchvalue) {
            this.comparartor = comparator;
            this.searchvalue = searchvalue;
        }
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String inst_klasse;
        String inst_identifier;
        String inst_datalist;
        String inst_range;
        String inst_apikey = "";
        HashMap<String, String> searchmap;
        ArrayList<String> searchkeywords;
        HashMap<String, String> outputmap;
        ArrayList<ContentDataOutput> outputlist;
        List<CfListcontent> listcontent = null;
        int range_start;
        int range_end;
        
        outputlist = new ArrayList<>();
        outputmap = new HashMap<>();
        Map<String, String[]> parameters = request.getParameterMap();
        parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("apikey") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
            apikey = values[0];
        });
        inst_apikey = apikey;
        if (apikeyutil.checkApiKey(inst_apikey, "GetContent")) {
            klasse = "";
            parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("class") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
                klasse = values[0];
            });
            inst_klasse = klasse;
            identifier = "";
            parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("identifier") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
                identifier = values[0];
            });
            inst_identifier = identifier;
            datalist = "";
            parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("datalist") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
                datalist = values[0];
            });
            inst_datalist = datalist;
            range = "";
            parameters.keySet().stream().filter((paramname) -> (paramname.compareToIgnoreCase("range") == 0)).map((paramname) -> parameters.get(paramname)).forEach((values) -> {
                range = values[0];
            });
            inst_range = range;
            range_start = 0;
            range_end = 0;
            if (!inst_range.isEmpty()) {
                if (inst_range.contains("-")) {
                    String[] ranges = inst_range.split("-");
                    range_start = Integer.parseInt(ranges[0]);
                    range_end = Integer.parseInt(ranges[1]);
                    if (range_start > range_end) {
                        int dummy = range_start;
                        range_start = range_end;
                        range_end = dummy;
                    }
                } else {
                    range_start = Integer.parseInt(inst_range);
                    range_end = range_start;
                }
            }
            
            listcontent = null;
            if (!inst_datalist.isEmpty()) {
                CfList dataList = cflistService.findByName(inst_datalist);
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
            CfClass cfclass = cfclassService.findByName(inst_klasse);
            List<CfClasscontent> classcontentList = cfclasscontentService.findByClassref(cfclass);
            boolean found = true;
            int listcounter = 0;
            for (CfClasscontent classcontent : classcontentList) {
                boolean inList = true;
                // Check if identifier is set and matches classcontent
                if ((!inst_identifier.isEmpty()) && (0 != inst_identifier.compareToIgnoreCase(classcontent.getName()))) {
                    inList = false;
                }
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
                    boolean putToList = true;
                    List<CfAttributcontent> attributcontentList = cfattributcontentService.findByClasscontentref(classcontent);
                    ArrayList<HashMap> keyvals = contentUtil.getContentOutputKeyval(attributcontentList);
                        ArrayList<String> keywords = contentUtil.getContentOutputKeywords(classcontent, true);
                    if (!searchmap.isEmpty()) {
                        for (String searchcontent : searchmap.keySet()) {
                            String searchvalue = searchmap.get(searchcontent);
                            SearchValues sv = getSearchValues(searchvalue);
                            if (!compareAttribut(keyvals, sv, searchcontent)) {
                                putToList = false;
                                break;
                            } 
                        }
                    }
                    // Check the keyword filter (at least one keyword must be found (OR))
                    if (!searchkeywords.isEmpty()) {
                        boolean dummyfound = false;
                        for (String keyword : searchkeywords) {
                            if (keywords.contains(keyword.toLowerCase())) {
                                dummyfound = true;
                            }
                        }
                        putToList = dummyfound;
                    }
                    if (putToList) {
                        found = true;
                        
                        listcounter++;
                        if (range_start > 0) {
                            if ((listcounter >= range_start) && (listcounter <= range_end)) {
                                ContentDataOutput contentdataoutput = new ContentDataOutput();
                                contentdataoutput.setContent(classcontent);
                                contentdataoutput.setKeywords(keywords);
                                contentdataoutput.setKeyvals(keyvals);
                                outputlist.add(contentdataoutput);
                                //System.out.println(inst_klasse + " - " + listcounter);
                            }
                        } else {
                            ContentDataOutput contentdataoutput = new ContentDataOutput();
                            contentdataoutput.setContent(classcontent);
                            contentdataoutput.setKeywords(keywords);
                            contentdataoutput.setKeyvals(keyvals);
                            outputlist.add(contentdataoutput);
                            //System.out.println(inst_klasse + " - " + listcounter);
                        }
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
                LOGGER.error(ex.getMessage());
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
        String inst_klasse;
        String inst_identifier;
        String inst_datalist;
        String inst_apikey = "";
        HashMap<String, String> searchmap;
        ArrayList<String> searchkeywords;
        HashMap<String, String> outputmap;
        ArrayList<ContentDataOutput> outputlist;
        List<CfListcontent> listcontent = null;
        int range_start = 0;
        int range_end = 0;
        if (!gcp.getRange().isEmpty()) {
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
        outputlist = new ArrayList<>();
        outputmap = new HashMap<>();
        inst_apikey = gcp.getApikey();
        if (apikeyutil.checkApiKey(inst_apikey, "GetContent")) {
            inst_klasse = gcp.getClassname();
            inst_identifier = gcp.getIdentifier();
            if (null == inst_identifier) {
                inst_identifier = "";
            }
            inst_datalist = gcp.getListname();
            listcontent = null;
            if ((null != inst_datalist) && (!inst_datalist.isEmpty())) {
                CfList dataList = cflistService.findByName(inst_datalist);
                listcontent = cflistcontentService.findByListref(dataList.getId());
            }
            searchmap = new HashMap<>();
            String searches = gcp.getSearches();
            if (null != searches) {
                String[] keys = searches.split("\\$");
                int counter = 0;
                for (String key : keys) {
                    if ((counter > 0) && ((counter%2) == 0)) {
                        searchmap.put(keys[counter-1], keys[counter]);
                    }
                    counter++;
                }
            }
            searchkeywords = new ArrayList<>();
            String keywordlist = gcp.getKeywords();
            if (null != keywordlist) {
                String[] keys = keywordlist.split("\\$");
                int counter = 0;
                for (String key : keys) {
                    if (counter > 0) {
                        searchkeywords.add(key);
                    }
                    counter++;
                }
            }

            CfClass cfclass = cfclassService.findByName(inst_klasse);
            List<CfClasscontent> classcontentList = cfclasscontentService.findByClassref(cfclass);
            boolean found = false;
            int listcounter = 0;
            for (CfClasscontent classcontent : classcontentList) {
                boolean inList = true;
                // Check if identifier is set and matches classcontent
                if ((!inst_identifier.isEmpty()) && (0 != inst_identifier.compareToIgnoreCase(classcontent.getName()))) {
                    inList = false;
                }
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
                    boolean putToList = true;
                    List<CfAttributcontent> attributcontentList = cfattributcontentService.findByClasscontentref(classcontent);
                    ArrayList<HashMap> keyvals = contentUtil.getContentOutputKeyval(attributcontentList);
                    ArrayList<String> keywords = contentUtil.getContentOutputKeywords(classcontent, true);
                    if (!searchmap.isEmpty()) {
                        for (String searchcontent : searchmap.keySet()) {
                            String searchvalue = searchmap.get(searchcontent);
                            SearchValues sv = getSearchValues(searchvalue);
                            if (!compareAttribut(keyvals, sv, searchcontent)) {
                                putToList = false;
                                break;
                            }
                        }
                    }
                    // Check the keyword filter (at least one keyword must be found (OR))
                    if (!searchkeywords.isEmpty()) {
                        boolean dummyfound = false;
                        for (String keyword : searchkeywords) {
                            if (keywords.contains(keyword.toLowerCase())) {
                                dummyfound = true;
                            }
                        }
                        putToList = dummyfound;
                    }
                    if (putToList) {
                        found = true;

                        listcounter++;
                        if (range_start > 0){
                            if ((listcounter >= range_start) && (listcounter <= range_end)) {
                                ContentDataOutput contentdataoutput = new ContentDataOutput();
                                contentdataoutput.setContent(classcontent);
                                contentdataoutput.setKeywords(keywords);
                                contentdataoutput.setKeyvals(keyvals);
                                outputlist.add(contentdataoutput);
                                //System.out.println(inst_klasse + " - " + listcounter);
                            }
                        } else {
                            ContentDataOutput contentdataoutput = new ContentDataOutput();
                            contentdataoutput.setContent(classcontent);
                            contentdataoutput.setKeywords(keywords);
                            contentdataoutput.setKeyvals(keyvals);
                            outputlist.add(contentdataoutput);
                            //System.out.println(inst_klasse + " - " + listcounter);
                        }
                    }
                }
            }
            if (!found) {
                outputmap.put("contentfound", "false");
            }
            Gson gson = new Gson();
            String json = gson.toJson(outputlist);
            gcp.setJson(json);
            gcp.setReturncode("TRUE");
            return gcp;
        } else {
            gcp.setReturncode("FALSE");
            gcp.setJson("[]");
            return gcp;
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
        processRequest(gcp, response);
        
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
    
    private SearchValues getSearchValues(String searchvalue) {
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
        return new SearchValues(comparator, searchvalue);
    }

    private ArrayList getContentKeywords(CfClasscontent content, boolean toLower) {
        ArrayList<String> keywords = new ArrayList<>();
        List<CfClasscontentkeyword> keywordlist = cfcontentkeywordService.findByClassContentRef(content.getId());
        if (keywordlist.size() > 0) {
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
    
    private boolean compareAttribut(ArrayList<HashMap> keyvals, SearchValues sv, String searchcontent) {
        boolean found = true;
        String searchvalue = sv.getSearchvalue().toLowerCase();
        for (HashMap keyval : keyvals) {
            if (null != keyval.get(searchcontent)) {
                if ((sv.getComparartor().compareToIgnoreCase("co") == 0) && (!((String)(keyval.get(searchcontent))).toLowerCase().contains(searchvalue))) {
                    found = false;
                    break;
                }
                if ((sv.getComparartor().compareToIgnoreCase("sw") == 0) && (!((String)(keyval.get(searchcontent))).toLowerCase().startsWith(searchvalue))) {
                    found = false;
                    break;
                }
                if ((sv.getComparartor().compareToIgnoreCase("eq") == 0) && (((String)(keyval.get(searchcontent))).toLowerCase().compareToIgnoreCase(searchvalue) != 0)) {
                    found = false;
                    break;
                }
                if ((sv.getComparartor().compareToIgnoreCase("ew") == 0) && (!((String)(keyval.get(searchcontent))).toLowerCase().endsWith(searchvalue))) {
                    found = false;
                    break;
                }
            } else {
                found = false;
                break;
            }
        }
        return found;
    }
}
