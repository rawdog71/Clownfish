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
import io.clownfish.clownfish.utils.HibernateUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.NoResultException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@WebServlet(name = "GetContentHibernate", urlPatterns = {"/GetContentHibernate"})
@Component
public class GetContentHibernate extends HttpServlet {
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
    @Autowired HibernateUtil hibernateUtil;

    private static transient @Getter @Setter String klasse;
    private static transient @Getter @Setter String identifier;
    private static transient @Getter @Setter String datalist;
    private static transient @Getter @Setter String apikey;
    private static transient @Getter @Setter String range;

    final transient Logger LOGGER = LoggerFactory.getLogger(GetContentHibernate.class);

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
            if (!inst_klasse.isEmpty()) {
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
                            searchmap.put(keys[counter-1] + "_" + counter, keys[counter]);
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

                Session session_tables = HibernateUtil.getClasssessions().get("tables").getSessionFactory().openSession();
                Query query = getQuery(session_tables, searchmap, inst_klasse);

                try {
                    List<Map> contentliste = (List<Map>) query.getResultList();

                    session_tables.close();
                    int listcounter = 0;
                    for (Map content : contentliste) {
                        CfClasscontent cfclasscontent = cfclasscontentService.findById((long)content.get("cf_contentref"));
                        if (null != cfclasscontent) {
                            if (!cfclasscontent.isScrapped()) {

                                listcounter++;
                                if (range_start > 0){
                                    if ((listcounter >= range_start) && (listcounter <= range_end)) {
                                        ContentDataOutput contentdataoutput = new ContentDataOutput();
                                        contentdataoutput.setContent(cfclasscontent);
                                        contentdataoutput.setKeywords(getContentKeywords(cfclasscontent, true));
                                        contentdataoutput.setKeyvals(getContentMap(content));
                                        outputlist.add(contentdataoutput);
                                    }
                                } else {
                                    ContentDataOutput contentdataoutput = new ContentDataOutput();
                                    contentdataoutput.setContent(cfclasscontent);
                                    contentdataoutput.setKeywords(getContentKeywords(cfclasscontent, true));
                                    contentdataoutput.setKeyvals(getContentMap(content));
                                    outputlist.add(contentdataoutput);
                                }
                            }
                        }
                    }
                    outputmap.put("contentfound", "true");
                } catch (NoResultException ex) {
                    session_tables.close();
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
                Gson gson = new Gson();
                String json = gson.toJson(outputlist);
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

        String range;

        range = gcp.getRange() == null ? "" : gcp.getRange();

        if (!range.isEmpty()) {
            if (range.contains("-")) {
                String[] ranges = range.split("-");
                range_start = Integer.parseInt(ranges[0]);
                range_end = Integer.parseInt(ranges[1]);
                if (range_start > range_end) {
                    int dummy = range_start;
                    range_start = range_end;
                    range_end = dummy;
                }
            } else {
                range_start = Integer.parseInt(range);
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
                int counter = 1;
                for (String key : keys) {
                    if ((counter > 0) && ((counter % 2) != 0)) {
                        searchmap.put(keys[counter-1] + "_" + counter, keys[counter]);
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

            Session session_tables = HibernateUtil.getClasssessions().get("tables").getSessionFactory().openSession();
            Query query = getQuery(session_tables, searchmap, inst_klasse);

            List<Map> contentliste = (List<Map>) query.getResultList();
            session_tables.close();
            int listcounter = 0;
            for (Map content : contentliste) {
                CfClasscontent cfclasscontent = cfclasscontentService.findById((long)content.get("cf_contentref"));
                if (null != cfclasscontent) {
                    if (!cfclasscontent.isScrapped()) {

                        listcounter++;
                        if (range_start > 0){
                            if ((listcounter >= range_start) && (listcounter <= range_end)) {
                                ContentDataOutput contentdataoutput = new ContentDataOutput();
                                contentdataoutput.setContent(cfclasscontent);
                                contentdataoutput.setKeywords(getContentKeywords(cfclasscontent, true));
                                contentdataoutput.setKeyvals(getContentMap(content));
                                outputlist.add(contentdataoutput);
                            }
                        } else {
                            ContentDataOutput contentdataoutput = new ContentDataOutput();
                            contentdataoutput.setContent(cfclasscontent);
                            contentdataoutput.setKeywords(getContentKeywords(cfclasscontent, true));
                            contentdataoutput.setKeyvals(getContentMap(content));
                            outputlist.add(contentdataoutput);
                        }
                    }
                }
            }
            boolean found = true;
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
        } catch (IOException e) {
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
        // contains
        if (searchvalue.startsWith(":co:")) {
            comparator = "co";
            searchvalue = searchvalue.substring(4);
        }
        // equals
        if (searchvalue.startsWith(":eq:")) {
            comparator = "eq";
            searchvalue = searchvalue.substring(4);
        }
        // ends with
        if (searchvalue.startsWith(":ew:")) {
            comparator = "ew";
            searchvalue = searchvalue.substring(4);
        }
        // starts with
        if (searchvalue.startsWith(":sw:")) {
            comparator = "sw";
            searchvalue = searchvalue.substring(4);
        }
        // not equals
        if (searchvalue.startsWith(":ne:")) {
            comparator = "ne";
            searchvalue = searchvalue.substring(4);
        }
        // greater than
        if (searchvalue.startsWith(":gt:")) {
            comparator = "gt";
            searchvalue = searchvalue.substring(4);
        }
        // less than
        if (searchvalue.startsWith(":lt:")) {
            comparator = "lt";
            searchvalue = searchvalue.substring(4);
        }
        // greater than or equal
        if (searchvalue.startsWith(":gte:")) {
            comparator = "gte";
            searchvalue = searchvalue.substring(4);
        }
        // less than or equal
        if (searchvalue.startsWith(":lte:")) {
            comparator = "lte";
            searchvalue = searchvalue.substring(4);
        }
        searchvalue = searchvalue.toLowerCase();
        return new SearchValues(comparator, searchvalue);
    }

    private ArrayList getContentKeywords(CfClasscontent content, boolean toLower) {
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

    private ArrayList getContentMap(Map content) {
        HashMap<String, String> contentMap = new HashMap<>(content);
        ArrayList contenList = new ArrayList<>();
        contenList.add(contentMap);
        return contenList;
    }
    
    private Query getQuery(Session session_tables, HashMap<String, String> searchmap, String inst_klasse) {
        Query query = null;
        if (!searchmap.isEmpty()) {
            String whereclause = " WHERE ";
            for (String searchcontent : searchmap.keySet()) {
                String searchcontentval = searchcontent.substring(0, searchcontent.length()-2);
                String searchvalue = searchmap.get(searchcontent);
                SearchValues sv = getSearchValues(searchvalue);
                switch (sv.getComparartor()) {
                    case "eq":
                        whereclause += searchcontentval + " = '" + sv.getSearchvalue() + "' AND ";
                        break;
                    case "sw":
                        whereclause += searchcontentval + " LIKE '" + sv.getSearchvalue() + "%' AND ";
                        break;
                    case "ew":
                        whereclause += searchcontentval + " LIKE '%" + sv.getSearchvalue() + "' AND ";
                        break;
                    case "co":
                        whereclause += searchcontentval + " LIKE '%" + sv.getSearchvalue() + "%' AND ";
                        break;
                    case "gt":
                        whereclause += searchcontentval + " > '" + sv.getSearchvalue() + "' AND ";
                        break;
                    case "lt":
                        whereclause += searchcontentval + " < '" + sv.getSearchvalue() + "' AND ";
                        break;
                    case "gte":
                        whereclause += searchcontentval + " >= '" + sv.getSearchvalue() + "' AND ";
                        break;
                    case "lte":
                        whereclause += searchcontentval + " <= '" + sv.getSearchvalue() + "' AND ";
                        break;
                    case "ne":
                        whereclause += searchcontentval + " <> '" + sv.getSearchvalue() + "' AND ";
                        break;
                }

            }
            whereclause = whereclause.substring(0, whereclause.length()-5);
            query = session_tables.createQuery("FROM " + inst_klasse + " c " + whereclause);
        } else {
            query = session_tables.createQuery("FROM " + inst_klasse + " c ");
        }
        return query;
    }
}
