/*
 * Copyright 2022 raine.
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
package io.clownfish.clownfish.graphql;

import graphql.schema.DataFetcher;
import io.clownfish.clownfish.datamodels.ContentDataOutput;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAssetlistcontent;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfContentversionService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.EncryptUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author raine
 */
@Component
public class GraphQLDataFetchers {
    @Autowired private CfClassService cfclassservice;
    @Autowired private CfAttributService cfattributservice;
    @Autowired private CfClasscontentService cfclasscontentService;
    @Autowired private CfAttributcontentService cfattributcontentservice;
    @Autowired private CfContentversionService cfcontentversionService;
    @Autowired private CfListService cflistService;
    @Autowired private CfAssetlistService cfassetlistService;
    @Autowired private CfListcontentService cflistcontentService;
    @Autowired CfAssetlistcontentService cfassetlistcontentService;
    @Autowired ContentUtil contentUtil;
    @Autowired private PropertyUtil propertyUtil;
    @Autowired HibernateUtil hibernateUtil;
    
    @Value("${hibernate.use:0}") int useHibernate;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(GraphQLDataFetchers.class);
    
    public DataFetcher getDataByField(String classname, String fieldname) {
        return dataFetchingEnvironment -> {
            CfClass clazz = cfclassservice.findByName(classname);
            
            if (!fieldname.isEmpty()) {
                CfAttribut attribut = cfattributservice.findByNameAndClassref(fieldname, clazz);
                if (attribut.getIdentity()) {
                    Map<String, String> resultlist = null;
                    switch (attribut.getAttributetype().getName()) {
                        case "boolean":
                            boolean bool_value = dataFetchingEnvironment.getArgument(fieldname);
                            resultlist = getSingle(clazz, attribut.getName(), bool_value);
                            break;
                        case "string":
                            String string_value = dataFetchingEnvironment.getArgument(fieldname);
                            resultlist = getSingle(clazz, attribut.getName(), string_value);
                            break;
                        case "integer":
                            long long_value = ((Number) dataFetchingEnvironment.getArgument(fieldname)).longValue();
                            resultlist = getSingle(clazz, attribut.getName(), long_value);
                            break;
                        case "real":
                            double double_value = ((Number) dataFetchingEnvironment.getArgument(fieldname)).doubleValue();
                            resultlist = getSingle(clazz, attribut.getName(), double_value);
                            break;
                    }
                    return resultlist;
                } else {
                    List<Map<String, String>> resultlist = null;
                    switch (attribut.getAttributetype().getName()) {
                        case "boolean":
                            boolean bool_value = dataFetchingEnvironment.getArgument(fieldname);
                            resultlist = getList(clazz, attribut.getName(), bool_value);
                            break;
                        case "string":
                            String string_value = dataFetchingEnvironment.getArgument(fieldname);
                            resultlist = getList(clazz, attribut.getName(), string_value);
                            break;
                        case "integer":
                            long long_value = ((Number) dataFetchingEnvironment.getArgument(fieldname)).longValue();
                            resultlist = getList(clazz, attribut.getName(), long_value);
                            break;
                        case "real":
                            double double_value = ((Number) dataFetchingEnvironment.getArgument(fieldname)).doubleValue();
                            resultlist = getList(clazz, attribut.getName(), double_value);
                            break;
                    }
                    return resultlist;
                }
            } else {
                List<Map<String, String>> resultlist = null;
                resultlist = getList(clazz, "", null);
                return resultlist;
            }
        };
    }
    
    public DataFetcher getDataByFilter(String classname, String fieldname) {
        return dataFetchingEnvironment -> {
            CfClass clazz = cfclassservice.findByName(classname);
            
            if (!fieldname.isEmpty()) {
                
                Map<String, ArrayList> input_values = dataFetchingEnvironment.getArgument(fieldname);
                List<HashMap<String, String>> filter_list = input_values.get("filter");
                for (HashMap<String, String> dummy_filter : filter_list) {
                    if (0 != dummy_filter.get("op").compareToIgnoreCase("bt")) {
                        dummy_filter.put("value2", "");
                    }
                }
                List<Map<String, String>> resultlist = null;
                resultlist = getList(clazz, filter_list);
                
                return resultlist;
            } else {
                List<Map<String, String>> resultlist = null;
                resultlist = getList(clazz, "", null);
                return resultlist;
            }
        };
    }
    
    private Map<String, String> getSingle(CfClass clazz, String attributname, Object attributvalue) {
        if (0 == useHibernate) {
            Map<String, String> result = new HashMap<>();
            List<CfClasscontent> classcontentList = cfclasscontentService.findByClassref(clazz);
            for (CfClasscontent cc : classcontentList) {
                if (!cc.isScrapped()) {
                    HashMap<String, String> attributmap = new HashMap<>();
                    List<CfAttributcontent> aclist = cfattributcontentservice.findByClasscontentref(cc);
                    if (checkCompare(aclist, cc, attributname, attributvalue)) {
                        List keyvals = contentUtil.getContentOutputKeyvalList(aclist);

                        result.putAll((Map)keyvals.get(0));
                    }
                }
            }
            return result;
        } else {
            Map<String, String> result = new HashMap<>();
            Session session_tables = HibernateUtil.getClasssessions().get("tables").getSessionFactory().openSession();
            HashMap searchmap = new HashMap<>();
            searchmap.put(attributname+"_1", ":eq:" + (String) attributvalue.toString());
            Query query = hibernateUtil.getQuery(session_tables, searchmap, clazz.getName());
            if (propertyUtil.getPropertyBoolean("sql_debug", true)) {
                LOGGER.info("Query: " + query.getQueryString());
            }
            try {
                List<Map> contentliste = (List<Map>) query.getResultList();

                session_tables.close();
                for (Map content : contentliste) {
                    CfClasscontent cfclasscontent = cfclasscontentService.findById((long)content.get("cf_contentref"));
                    if (null != cfclasscontent) {
                        if (!cfclasscontent.isScrapped()) {
                            ContentDataOutput contentdataoutput = new ContentDataOutput();
                            contentdataoutput.setContent(cfclasscontent);
                            if (cfclasscontent.getClassref().isEncrypted()) {
                                contentdataoutput.setKeyvals(contentUtil.getContentMapListDecrypted(content, cfclasscontent.getClassref()));
                                contentdataoutput.setKeyval(contentUtil.getContentMapDecrypted(content, cfclasscontent.getClassref()));
                            } else {
                                contentdataoutput.setKeyvals(contentUtil.getContentMapList(content));
                                contentdataoutput.setKeyval(contentUtil.getContentMap(content));
                            }
                            setClassrefVals(contentdataoutput.getKeyvals().get(0), clazz, null);
                            setAssetrefVals(contentdataoutput.getKeyvals().get(0), clazz);
                            try {
                                contentdataoutput.setDifference(contentUtil.hasDifference(cfclasscontent));
                                contentdataoutput.setMaxversion(cfcontentversionService.findMaxVersion(cfclasscontent.getId()));
                            } catch (Exception ex) {

                            }
                            result.putAll(contentdataoutput.getKeyvals().get(0));
                        }
                    }
                }
            } catch (NoResultException ex) {
                session_tables.close();
            }
            
            return result;
        }
    }
    
    private List<Map<String, String>> getList(CfClass clazz, String attributname, Object attributvalue) {
        if (0 == useHibernate) {
            List<Map<String, String>> result = new ArrayList<>();
            List<CfClasscontent> classcontentList = cfclasscontentService.findByClassref(clazz);
            for (CfClasscontent cc : classcontentList) {
                if (!cc.isScrapped()) {
                    HashMap<String, String> attributmap = new HashMap<>();
                    if (!attributname.isEmpty()) {
                        List<CfAttributcontent> aclist = cfattributcontentservice.findByClasscontentref(cc);
                        if (checkCompare(aclist, cc, attributname, attributvalue)) {
                            List keyvals = contentUtil.getContentOutputKeyvalList(aclist);
                            result.add((Map)keyvals.get(0));
                        }
                    } else {
                        List<CfAttributcontent> aclist = cfattributcontentservice.findByClasscontentref(cc);
                        List keyvals = contentUtil.getContentOutputKeyvalList(aclist);
                        result.add((Map)keyvals.get(0));
                    }
                }
            }
            return result;
        } else {
            List<Map<String, String>> result = new ArrayList<>();
            Session session_tables = HibernateUtil.getClasssessions().get("tables").getSessionFactory().openSession();
            HashMap searchmap = new HashMap<>();
            if (null != attributvalue) {
                searchmap.put(attributname+"_1", (String) attributvalue.toString());
            }
            Query query = hibernateUtil.getQuery(session_tables, searchmap, clazz.getName());
            if (propertyUtil.getPropertyBoolean("sql_debug", true)) {
                LOGGER.info("Query: " + query.getQueryString());
            }
            try {
                List<Map> contentliste = (List<Map>) query.getResultList();

                session_tables.close();
                for (Map content : contentliste) {
                    CfClasscontent cfclasscontent = cfclasscontentService.findById((long)content.get("cf_contentref"));
                    if (null != cfclasscontent) {
                        if (!cfclasscontent.isScrapped()) {
                            ContentDataOutput contentdataoutput = new ContentDataOutput();
                            contentdataoutput.setContent(cfclasscontent);
                            if (cfclasscontent.getClassref().isEncrypted()) {
                                contentdataoutput.setKeyvals(contentUtil.getContentMapListDecrypted(content, cfclasscontent.getClassref()));
                                contentdataoutput.setKeyval(contentUtil.getContentMapDecrypted(content, cfclasscontent.getClassref()));
                            } else {
                                contentdataoutput.setKeyvals(contentUtil.getContentMapList(content));
                                contentdataoutput.setKeyval(contentUtil.getContentMap(content));
                            }
                            setClassrefVals(contentdataoutput.getKeyvals().get(0), clazz, null);
                            setAssetrefVals(contentdataoutput.getKeyvals().get(0), clazz);
                            try {
                                contentdataoutput.setDifference(contentUtil.hasDifference(cfclasscontent));
                                contentdataoutput.setMaxversion(cfcontentversionService.findMaxVersion(cfclasscontent.getId()));
                            } catch (Exception ex) {

                            }
                            result.add(contentdataoutput.getKeyvals().get(0));
                        }
                    }
                }
            } catch (NoResultException ex) {
                session_tables.close();
            }
            
            return result;
        }
    }
    
    private List<Map<String, String>> getList(CfClass clazz, List<HashMap<String, String>> filter_list) {
        if (0 == useHibernate) {
            List<Map<String, String>> result = new ArrayList<>();
            List<CfClasscontent> classcontentList = cfclasscontentService.findByClassref(clazz);
            for (CfClasscontent cc : classcontentList) {
                if (!cc.isScrapped()) {
                    HashMap<String, String> attributmap = new HashMap<>();
                    
                    for (HashMap<String, String> filter : filter_list) {
                        String field = filter.get("field");
                        String op = filter.get("op");
                        String value1 = filter.get("value1");
                        String value2 = filter.get("value2");

                        CfAttribut attribut = cfattributservice.findByNameAndClassref(field, clazz);
                        if (!field.isEmpty()) {
                            List<CfAttributcontent> aclist = cfattributcontentservice.findByClasscontentref(cc);
                            if (checkCompare(aclist, cc, field, value1)) {
                                List keyvals = contentUtil.getContentOutputKeyvalList(aclist);
                                result.add((Map)keyvals.get(0));
                            }
                        } else {
                            List<CfAttributcontent> aclist = cfattributcontentservice.findByClasscontentref(cc);
                            List keyvals = contentUtil.getContentOutputKeyvalList(aclist);
                            result.add((Map)keyvals.get(0));
                        }

                    }
                }
            }
            return result;
        } else {
            List<Map<String, String>> result = new ArrayList<>();
            Session session_tables = HibernateUtil.getClasssessions().get("tables").getSessionFactory().openSession();
            HashMap searchmap = new HashMap<>();
            for (HashMap<String, String> filter : filter_list) {
                String field = filter.get("field");
                String op = filter.get("op");
                String value1 = filter.get("value1");
                String value2 = filter.get("value2");
                if ((null != field) && (!field.contains("."))) {
                    if ((!value2.isEmpty()) && (0 == op.compareToIgnoreCase("bt"))) {
                        searchmap.put(field+"_1", ":" + op + ":" + value1 + ":" + value2);
                    } else {
                        searchmap.put(field+"_1", ":" + op + ":" + value1);
                    }
                }
            }
            Query query = hibernateUtil.getQuery(session_tables, searchmap, clazz.getName());
            if (propertyUtil.getPropertyBoolean("sql_debug", true)) {
                LOGGER.info("Query: " + query.getQueryString());
            }
            try {
                List<Map> contentliste = (List<Map>) query.getResultList();

                session_tables.close();
                for (Map content : contentliste) {
                    CfClasscontent cfclasscontent = cfclasscontentService.findById((long)content.get("cf_contentref"));
                    if (null != cfclasscontent) {
                        if (!cfclasscontent.isScrapped()) {
                            boolean found = false;
                            ContentDataOutput contentdataoutput = new ContentDataOutput();
                            contentdataoutput.setContent(cfclasscontent);
                            if (cfclasscontent.getClassref().isEncrypted()) {
                                contentdataoutput.setKeyvals(contentUtil.getContentMapListDecrypted(content, cfclasscontent.getClassref()));
                                contentdataoutput.setKeyval(contentUtil.getContentMapDecrypted(content, cfclasscontent.getClassref()));
                            } else {
                                contentdataoutput.setKeyvals(contentUtil.getContentMapList(content));
                                contentdataoutput.setKeyval(contentUtil.getContentMap(content));
                            }
                            found = setClassrefVals(contentdataoutput.getKeyvals().get(0), clazz, filter_list);
                            setAssetrefVals(contentdataoutput.getKeyvals().get(0), clazz);
                            try {
                                contentdataoutput.setDifference(contentUtil.hasDifference(cfclasscontent));
                                contentdataoutput.setMaxversion(cfcontentversionService.findMaxVersion(cfclasscontent.getId()));
                            } catch (Exception ex) {

                            }
                            if (found)
                                result.add(contentdataoutput.getKeyvals().get(0));
                        }
                    }
                }
            } catch (NoResultException ex) {
                session_tables.close();
            }
            
            return result;
        }
    }
    
    private boolean setClassrefVals(HashMap hm, CfClass clazz, List<HashMap<String, String>> filter_list) {
        boolean found = true;
        for (Object key : hm.keySet()) {
            try {
                CfAttribut attr = cfattributservice.findByNameAndClassref((String) key, clazz);
                if (0 == attr.getAttributetype().getName().compareToIgnoreCase("classref")) {
                    if (0 == attr.getRelationtype()) {          // n:m
                        CfList contentlist = cflistService.findByClassrefAndName(attr.getRelationref(), (String) hm.get(key));
                        List<CfListcontent> listcontent = cflistcontentService.findByListref(contentlist.getId());
                        List<Map<String, String>> result = new ArrayList<>();
                        for (CfListcontent contentitem : listcontent) {
                            Map output = hibernateUtil.getContent(attr.getRelationref().getName(), contentitem.getCfListcontentPK().getClasscontentref(), attr.getName(), filter_list);
                            if (!output.isEmpty()) {
                                CfClasscontent cfclasscontent = cfclasscontentService.findById((long)output.get("cf_contentref"));
                                if (null != cfclasscontent) {
                                    if (!cfclasscontent.isScrapped()) {
                                        ContentDataOutput contentdataoutput = new ContentDataOutput();
                                        contentdataoutput.setContent(cfclasscontent);
                                        if (cfclasscontent.getClassref().isEncrypted()) {
                                            contentdataoutput.setKeyvals(contentUtil.getContentMapListDecrypted(output, cfclasscontent.getClassref()));
                                            contentdataoutput.setKeyval(contentUtil.getContentMapDecrypted(output, cfclasscontent.getClassref()));
                                        } else {
                                            contentdataoutput.setKeyvals(contentUtil.getContentMapList(output));
                                            contentdataoutput.setKeyval(contentUtil.getContentMap(output));
                                        }
                                        setClassrefVals(contentdataoutput.getKeyvals().get(0), clazz, filter_list);
                                        setAssetrefVals(contentdataoutput.getKeyvals().get(0), clazz);
                                        try {
                                            contentdataoutput.setDifference(contentUtil.hasDifference(cfclasscontent));
                                            contentdataoutput.setMaxversion(cfcontentversionService.findMaxVersion(cfclasscontent.getId()));
                                        } catch (Exception ex) {

                                        }
                                        result.add(contentdataoutput.getKeyvals().get(0));
                                    }
                                }
                            }
                        }
                        hm.put(attr.getName(), result);
                        if (result.isEmpty()) found = false;
                    } else {                                    // 1:n
                        Map output = hibernateUtil.getContent(attr.getRelationref().getName(), (long) hm.get(key), attr.getName(), filter_list);
                        hm.put(attr.getName(), output);
                        if (output.isEmpty()) found = false;
                    }
                }
            } catch (Exception ex) {
                //System.out.println(ex.getMessage());
            }
        }
        return found;
    }
    
    private void setAssetrefVals(HashMap hm, CfClass clazz) {
        for (Object key : hm.keySet()) {
            try {
                CfAttribut attr = cfattributservice.findByNameAndClassref((String) key, clazz);
                if (0 == attr.getAttributetype().getName().compareToIgnoreCase("assetref")) {
                    CfAssetlist assetlist = cfassetlistService.findByName((String)hm.get(key));
                    List<CfAssetlistcontent> assetcontentlist = cfassetlistcontentService.findByAssetlistref(assetlist.getId());
                    
                    List<Long> result = new ArrayList<>();
                    for (CfAssetlistcontent assetitem : assetcontentlist) {
                        result.add(assetitem.getCfAssetlistcontentPK().getAssetref());
                    }
                    hm.put(attr.getName(), result);
                }
            } catch (Exception ex) {
                
            }
        }
    }
    
    private boolean checkCompare(List<CfAttributcontent> aclist, CfClasscontent cc, String attributname, Object attributvalue) {
        boolean found = false;
        for (CfAttributcontent ac : aclist) {
            if ((!found) && (0 == ac.getAttributref().getName().compareToIgnoreCase(attributname))) {
                switch (ac.getAttributref().getAttributetype().getName()) {
                    case "string":
                    case "text":
                    case "htmltext":
                    case "markdown":
                        if ((ac.getClasscontentref().getClassref().isEncrypted()) && (!ac.getAttributref().getIdentity())) {
                            if (0 == EncryptUtil.decrypt(ac.getContentString(), propertyUtil.getPropertyValue("aes_key")).compareTo((String) attributvalue)) {
                                found = true;
                            }
                        } else {
                            if ((null != ac.getContentString()) && (0 == ac.getContentString().compareTo((String) attributvalue))) {
                                found = true;
                            }
                        }
                        break;
                    case "boolean":
                        if ((null != ac.getContentBoolean()) && (ac.getContentBoolean() == (boolean) attributvalue)) {
                            found = true;
                        }
                        break;
                    case "integer":
                        if ((null != ac.getContentInteger()) && (ac.getContentInteger().longValue() == (long) attributvalue)) {
                            found = true;
                        }
                        break;
                    case "real":
                        if ((null != ac.getContentReal()) && (ac.getContentReal().floatValue()  == (float) attributvalue)) {
                            found = true;
                        }
                        break;
                }
            }
        }
        return found;
    }
}
