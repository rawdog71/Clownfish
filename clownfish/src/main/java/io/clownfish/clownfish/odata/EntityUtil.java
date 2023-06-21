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
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfContentversionService;
import io.clownfish.clownfish.utils.ContentUtil;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author raine
 */
@Component
public class EntityUtil {
    @Autowired private CfAttributService cfattributservice;
    @Autowired private CfClasscontentService cfClasscontentService;
    @Autowired private CfContentversionService cfcontentversionService;
    @Autowired private ContentUtil contentUtil;

    final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EntityUtil.class);
    public Entity makeEntity(ContentDataOutput contentdataoutput) {
        Entity entity = new Entity();
        String id = "";
        for (HashMap hm : contentdataoutput.getKeyvals()) {
            for (Object attributname : hm.keySet()) {
                try {
                    CfAttribut attribut = cfattributservice.findByNameAndClassref((String) attributname, contentdataoutput.getContent().getClassref());
                    if ((0 != attribut.getAttributetype().getName().compareToIgnoreCase("classref")) && (0 != attribut.getAttributetype().getName().compareToIgnoreCase("assetref"))) {
                        //System.out.println(attribut.getAttributetype().getName());
                        if (attribut.getIdentity()) {
                            id = (String) hm.get(attributname).toString();
                        }
                        Property prop = new Property();
                        prop.setName(attribut.getName());
                        prop.setValue(ValueType.PRIMITIVE, hm.get(attributname));
                        entity.addProperty(prop);
                    } else {
                        if ((0 == attribut.getAttributetype().getName().compareToIgnoreCase("classref")) && (1 == attribut.getRelationtype())) { // 1:n
                            Property prop = new Property();
                            prop.setName(attribut.getName());
                            Long content_id = (Long)((HashMap)hm.get(attributname)).get("cf_contentref");
                            CfClasscontent cfclasscontent = cfClasscontentService.findById(content_id);
                            HashMap attributes = (HashMap) hm.get(attributname);

//                            if (Objects.equals(attribute.toString(), "cf_contentref")) {
//                                continue;
//                            }
                            if (cfclasscontent != null && !cfclasscontent.isScrapped()) {
                                ContentDataOutput cdo = new ContentDataOutput();
                                cdo.setContent(cfclasscontent);
                                if (cfclasscontent.getClassref().isEncrypted()) {
                                    cdo.setKeyvals(contentUtil.getContentMapDecrypted(attributes, cfclasscontent.getClassref()));
                                } else {
                                    cdo.setKeyvals(contentUtil.getContentMap(attributes));
                                }
                                contentUtil.setClassrefVals(cdo.getKeyvals().get(0), cfclasscontent.getClassref(), null);
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
                                CfAttribut cfAtt = cfattributservice.findByNameAndClassref(key, cfclasscontent.getClassref());
                                Property class_prop = new Property();
                                class_prop.setName(key);
                                class_prop.setType(GenericEdmProvider.getODataType(cfAtt).getFullQualifiedNameAsString());
                                class_prop.setValue(ValueType.PRIMITIVE, value);
                                val.getValue().add(class_prop);
                            }
                            prop.setValue(ValueType.COMPLEX, val);
                            prop.setType("OData.Complex." + cfclasscontent.getClassref().getName());
                            entity.addProperty(prop);
                            }

                        }
                    }
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
        }
        entity.setId(createId(contentdataoutput.getContent().getClassref().getName(), id));
        // entity.setType("OData.Entity." + contentdataoutput.getContent().getClassref().getName());
        return entity;
    }
    
    private URI createId(String entitySetName, Object id) {
        try {
            return new URI(entitySetName + "(" + id + ")");
        } catch (URISyntaxException e) {
            throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
        }
    }
}
