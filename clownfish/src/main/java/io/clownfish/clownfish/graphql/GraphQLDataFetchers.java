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
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.EncryptUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired ContentUtil contentUtil;
    @Autowired private PropertyUtil propertyUtil;
    
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
    
    private Map<String, String> getSingle(CfClass clazz, String attributname, Object attributvalue) {
        Map<String, String> result = new HashMap<>();
        List<CfClasscontent> classcontentList = cfclasscontentService.findByClassref(clazz);
        for (CfClasscontent cc : classcontentList) {
            if (!cc.isScrapped()) {
                HashMap<String, String> attributmap = new HashMap<>();
                List<CfAttributcontent> aclist = cfattributcontentservice.findByClasscontentref(cc);
                if (checkCompare(aclist, cc, attributname, attributvalue)) {
                    List keyvals = contentUtil.getContentOutputKeyval(aclist);
                
                    result.putAll((Map)keyvals.get(0));
                }
            }
        }
        return result;
    }
    
    private List<Map<String, String>> getList(CfClass clazz, String attributname, Object attributvalue) {
        List<Map<String, String>> result = new ArrayList<>();
        List<CfClasscontent> classcontentList = cfclasscontentService.findByClassref(clazz);
        for (CfClasscontent cc : classcontentList) {
            if (!cc.isScrapped()) {
                HashMap<String, String> attributmap = new HashMap<>();
                if (!attributname.isEmpty()) {
                    List<CfAttributcontent> aclist = cfattributcontentservice.findByClasscontentref(cc);
                    if (checkCompare(aclist, cc, attributname, attributvalue)) {
                        List keyvals = contentUtil.getContentOutputKeyval(aclist);
                        result.add((Map)keyvals.get(0));
                    }
                } else {
                    List<CfAttributcontent> aclist = cfattributcontentservice.findByClasscontentref(cc);
                    List keyvals = contentUtil.getContentOutputKeyval(aclist);
                    result.add((Map)keyvals.get(0));
                }
            }
        }
        return result;
    }
    
    private boolean checkCompare(List<CfAttributcontent> aclist, CfClasscontent cc, String attributname, Object attributvalue) {
        boolean found = false;
        for (CfAttributcontent ac : aclist) {
            if ((!found) && (0 == ac.getAttributref().getName().compareToIgnoreCase(attributname))) {
                switch (ac.getAttributref().getAttributetype().getName()) {
                    case "string":
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
