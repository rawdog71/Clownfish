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
package io.clownfish.clownfish.odata;

import io.clownfish.clownfish.datamodels.ContentDataOutput;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfContentversionService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.utils.ContentUtil;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.NoResultException;

/**
 *
 * @author raine
 */
@Component
public class EntityUtil {
    @Autowired private CfAttributService cfattributservice;
    @Autowired private CfAttributcontentService cfattributcontentService;
    @Autowired private CfClasscontentService cfClasscontentService;
    @Autowired private CfContentversionService cfcontentversionService;
    @Autowired private CfListService cflistService;
    @Autowired private CfListcontentService cflistcontentService;
    @Autowired private ContentUtil contentUtil;
    
    private static final HashMap<String, CfAttribut> attributmap = new HashMap<>();

    final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EntityUtil.class);
    
    private static void setPropValue(Property prop, Object value) {
        if (value == null) {
            prop.setValue(ValueType.PRIMITIVE, null);
            return;
        }
        switch (prop.getType()) {
            case "Edm.String":
                prop.setValue(ValueType.PRIMITIVE, (String)value);
                break;
            case "Edm.Int32":
                if (value instanceof String) {
                    prop.setValue(ValueType.PRIMITIVE, Integer.parseInt((String)value));
                } else if (value instanceof Long) {
                    prop.setValue(ValueType.PRIMITIVE, ((Long) value).intValue());
                } else if (value instanceof Integer) {
                    prop.setValue(ValueType.PRIMITIVE, (Integer) value);
                }
                break;
            case "Edm.Double":
                if (value instanceof String) {
                    prop.setValue(ValueType.PRIMITIVE, Double.parseDouble((String)value));
                } else if (value instanceof Long) {
                    prop.setValue(ValueType.PRIMITIVE, ((Double)value));
                } else if (value instanceof Double) {
                    prop.setValue(ValueType.PRIMITIVE, ((Double)value));
                }
                break;
            case "Edm.Boolean":
                prop.setValue(ValueType.PRIMITIVE, ((Boolean)value));
                break;
            case "Edm.Date":
                prop.setValue(ValueType.PRIMITIVE, ((Date)value));
                break;
            default:
                prop.setValue(ValueType.PRIMITIVE, null);
        }
    }
    
    public Entity makeEntity(ContentDataOutput contentdataoutput) {
        Entity entity = new Entity();
        String id = "";
        for (HashMap hm : contentdataoutput.getKeyvals()) {
            for (Object attributname : hm.keySet()) {
                CfAttribut attribut = null;
                if (attributmap.containsKey(contentdataoutput.getContent().getClassref().getName() + "_" +attributname)) {
                    attribut = attributmap.get(contentdataoutput.getContent().getClassref().getName() + "_" +attributname);
                } else {
                    try {
                        attribut = cfattributservice.findByNameAndClassref((String) attributname, contentdataoutput.getContent().getClassref());
                        attributmap.put(contentdataoutput.getContent().getClassref().getName() + "_" +attributname, attribut);
                    } catch (Exception ex) {
                        attributmap.put(contentdataoutput.getContent().getClassref().getName() + "_" +attributname, null);
                    }
                }
                if (null != attribut) {
                    if ((0 != attribut.getAttributetype().getName().compareToIgnoreCase("classref")) && (0 != attribut.getAttributetype().getName().compareToIgnoreCase("assetref"))) {
                        //System.out.println(attribut.getAttributetype().getName());
                        if (attribut.getIdentity()) {
                            id = (String) hm.get(attributname).toString();
                        }
                        Property prop = new Property();
                        prop.setName(attribut.getName());
                        prop.setType(GenericEdmProvider.getODataType(attribut).getFullQualifiedNameAsString());
                        setPropValue(prop, hm.get(attributname));
                        entity.addProperty(prop);
                    } else {
                        if ((0 == attribut.getAttributetype().getName().compareToIgnoreCase("classref")) && (1 == attribut.getRelationtype())) { // 1:n
                            Property prop = new Property();
                            prop.setName(attribut.getName());
                            Long content_id = (Long)hm.get(attributname);
                            CfClasscontent cfclasscontent = cfClasscontentService.findById(content_id);
                            prop.setValue(ValueType.COMPLEX, createComplexVal(cfclasscontent));
                            prop.setType("OData.Complex." + cfclasscontent.getClassref().getName());
                            entity.addProperty(prop);
                        } else {                                                // n:m Relation
                            Property coll_prop = new Property();
                            List<ComplexValue> values = new ArrayList<>();
                            String datalistname = (String) hm.get(attributname);
                            if (attribut.getIdentity()) {
                                id = (String) hm.get(attributname).toString();
                            }
                            try {
                                CfList datalist = cflistService.findByName(datalistname);
                                List<CfListcontent> contentlist = cflistcontentService.findByListref(datalist.getId());
                                for (CfListcontent listcontent : contentlist) {
                                    CfClasscontent cfclasscontent = cfClasscontentService.findById(listcontent.getCfListcontentPK().getClasscontentref());
                                    values.add(createComplexVal(cfclasscontent));
                                }
                                coll_prop.setValue(ValueType.COLLECTION_COMPLEX, values);
                                coll_prop.setName((String)attributname);
                                coll_prop.setType("Collection(OData.Complex." + datalist.getClassref().getName() + ")");
                                entity.addProperty(coll_prop);
                            } catch (NoResultException nre) {
                                LOGGER.warn("Datalist not set for attribute " + (String)attributname + " of class " + contentdataoutput.getContent().getClassref().getName());
                                coll_prop.setValue(ValueType.COLLECTION_COMPLEX, null);
                                entity.addProperty(coll_prop);
                            }
                        }
                    }
                }
            }
        }
        entity.setId(createId(contentdataoutput.getContent().getClassref().getName(), id));
        return entity;
    }
    
    private URI createId(String entitySetName, Object id) {
        try {
            return new URI(entitySetName + "(" + id.toString().replaceAll(" ", "_") + ")");
        } catch (URISyntaxException e) {
            throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
        }
    }
    
    private ComplexValue createComplexVal(CfClasscontent cfclasscontent) {
        List<CfAttributcontent> attributcontentList = cfattributcontentService.findByClasscontentref(cfclasscontent);
        HashMap attributes = contentUtil.getContentOutputKeyval(attributcontentList);
        if (cfclasscontent != null && !cfclasscontent.isScrapped()) {
            ContentDataOutput cdo = new ContentDataOutput();
            cdo.setContent(cfclasscontent);
            if (cfclasscontent.getClassref().isEncrypted()) {
                cdo.setKeyvals(contentUtil.getContentMapListDecrypted(attributes, cfclasscontent.getClassref()));
                cdo.setKeyval(contentUtil.getContentMapDecrypted(attributes, cfclasscontent.getClassref()));
            } else {
                cdo.setKeyvals(contentUtil.getContentMapList(attributes));
                cdo.setKeyval(contentUtil.getContentMap(attributes));
            }
            contentUtil.setClassrefVals(cdo.getKeyvals().get(0), cfclasscontent.getClassref(), null);
            cdo.setKeyval(cdo.getKeyvals().get(0));
            contentUtil.setAssetrefVals(cdo.getKeyvals().get(0), cfclasscontent.getClassref());
            try {
                cdo.setDifference(contentUtil.hasDifference(cfclasscontent));
                cdo.setMaxversion(cfcontentversionService.findMaxVersion(cfclasscontent.getId()));
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage());
            }
            ComplexValue val = new ComplexValue();
            val.setTypeName("OData.Complex." + cfclasscontent.getClassref().getName());
            for (Object att : attributes.entrySet()) {
                String key = (String)((Map.Entry)att).getKey();
                var value = ((Map.Entry<?, ?>) att).getValue();
                if (Objects.equals(key, "cf_contentref")
                        || Objects.equals(key, "$type$")
                        || Objects.equals(key, "cf_id")) {
                    continue;
                }
                CfAttribut cfAtt = null;
                if (attributmap.containsKey(cfclasscontent.getClassref().getName() + "_" + key)) {
                    cfAtt = attributmap.get(cfclasscontent.getClassref().getName() + "_" + key);
                } else {
                    cfAtt = cfattributservice.findByNameAndClassref(key, cfclasscontent.getClassref());
                    attributmap.put(cfclasscontent.getClassref().getName() + "_" + key, cfAtt);
                }
                
                Property class_prop = new Property();
                class_prop.setName(key);
                class_prop.setType(GenericEdmProvider.getODataType(cfAtt).getFullQualifiedNameAsString());
                if ((0 == cfAtt.getAttributetype().getName().compareToIgnoreCase("classref")) && (1 == cfAtt.getRelationtype())) { // 1:n
                    Long content_id = Long.valueOf((String)value);
                    CfClasscontent cfclasscontentref = cfClasscontentService.findById(content_id);
                    class_prop.setValue(ValueType.COMPLEX, createComplexVal(cfclasscontentref));
                    class_prop.setType("OData.Complex." + cfclasscontentref.getClassref().getName());
                } else {
                    setPropValue(class_prop, value);
                }
                val.getValue().add(class_prop);
            }
            return val;
        }
        return null;
    }
}
