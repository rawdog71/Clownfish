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
    @Autowired ContentUtil contentUtil;
    
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
                            resultlist = contentUtil.getSingle(clazz, attribut.getName(), bool_value);
                            break;
                        case "string":
                            String string_value = dataFetchingEnvironment.getArgument(fieldname);
                            resultlist = contentUtil.getSingle(clazz, attribut.getName(), string_value);
                            break;
                        case "integer":
                            long long_value = ((Number) dataFetchingEnvironment.getArgument(fieldname)).longValue();
                            resultlist = contentUtil.getSingle(clazz, attribut.getName(), long_value);
                            break;
                        case "real":
                            double double_value = ((Number) dataFetchingEnvironment.getArgument(fieldname)).doubleValue();
                            resultlist = contentUtil.getSingle(clazz, attribut.getName(), double_value);
                            break;
                    }
                    return resultlist;
                } else {
                    List<Map<String, String>> resultlist = null;
                    switch (attribut.getAttributetype().getName()) {
                        case "boolean":
                            boolean bool_value = dataFetchingEnvironment.getArgument(fieldname);
                            resultlist = contentUtil.getList(clazz, attribut.getName(), bool_value);
                            break;
                        case "string":
                            String string_value = dataFetchingEnvironment.getArgument(fieldname);
                            resultlist = contentUtil.getList(clazz, attribut.getName(), string_value);
                            break;
                        case "integer":
                            long long_value = ((Number) dataFetchingEnvironment.getArgument(fieldname)).longValue();
                            resultlist = contentUtil.getList(clazz, attribut.getName(), long_value);
                            break;
                        case "real":
                            double double_value = ((Number) dataFetchingEnvironment.getArgument(fieldname)).doubleValue();
                            resultlist = contentUtil.getList(clazz, attribut.getName(), double_value);
                            break;
                    }
                    return resultlist;
                }
            } else {
                List<Map<String, String>> resultlist = null;
                resultlist = contentUtil.getList(clazz, "", null);
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
                resultlist = contentUtil.getList(clazz, filter_list);
                
                return resultlist;
            } else {
                List<Map<String, String>> resultlist = null;
                resultlist = contentUtil.getList(clazz, "", null);
                return resultlist;
            }
        };
    }
}
