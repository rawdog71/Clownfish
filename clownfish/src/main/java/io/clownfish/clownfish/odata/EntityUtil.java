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
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfContentversionService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author raine
 */
@Component
public class EntityUtil {
    @Autowired private CfAttributService cfattributservice;
    @Autowired private CfListService cflistService;
    @Autowired private CfListcontentService cflistcontentService;
    @Autowired private CfClasscontentService cfclasscontentService;
    @Autowired private CfContentversionService cfcontentversionService;
    @Autowired ContentUtil contentUtil;
    @Autowired HibernateUtil hibernateUtil;
    
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
                        if (0 == attribut.getAttributetype().getName().compareToIgnoreCase("classref")) {
                            if (1 == attribut.getRelationtype()) {  // 1:n
                                Property prop = new Property();
                                prop.setName(attribut.getName());
                                prop.setValue(ValueType.PRIMITIVE, hm.get(attributname));
                                entity.addProperty(prop);
                            } else {                                // n:m
                                Property prop = new Property();
                                prop.setName(attribut.getName());
                                
                                CfList contentlist = cflistService.findByClassrefAndName(attribut.getRelationref(), (String) hm.get(attributname));
                                List<CfListcontent> listcontent = cflistcontentService.findByListref(contentlist.getId());
                                List<Map<String, String>> result = new ArrayList<>();
                                for (CfListcontent contentitem : listcontent) {
                                    Map output = hibernateUtil.getContent(attribut.getRelationref().getName(), contentitem.getCfListcontentPK().getClasscontentref(), attribut.getName(), null);
                                    if (!output.isEmpty()) {
                                        CfClasscontent cfclasscontent = cfclasscontentService.findById((long)output.get("cf_contentref"));
                                        if (null != cfclasscontent) {
                                            if (!cfclasscontent.isScrapped()) {
                                                ContentDataOutput contentdataoutput2 = new ContentDataOutput();
                                                contentdataoutput2.setContent(cfclasscontent);
                                                if (cfclasscontent.getClassref().isEncrypted()) {
                                                    contentdataoutput2.setKeyvals(contentUtil.getContentMapDecrypted(output, cfclasscontent.getClassref()));
                                                } else {
                                                    contentdataoutput2.setKeyvals(contentUtil.getContentMap(output));
                                                }
                                                //setClassrefVals(contentdataoutput2.getKeyvals().get(0), attribut.getRelationref(), null);
                                                //setAssetrefVals(contentdataoutput2.getKeyvals().get(0), attribut.getRelationref());
                                                try {
                                                    contentdataoutput2.setDifference(contentUtil.hasDifference(cfclasscontent));
                                                    contentdataoutput2.setMaxversion(cfcontentversionService.findMaxVersion(cfclasscontent.getId()));
                                                } catch (Exception ex) {

                                                }
                                                result.add(contentdataoutput2.getKeyvals().get(0));
                                            }
                                        }
                                    }
                                }
                                prop.setValue(ValueType.COLLECTION_PRIMITIVE, result);
                                entity.addProperty(prop);
                            }
                        }
                    }
                } catch (Exception ex) {
                    
                }
            }
        }
        entity.setId(createId(contentdataoutput.getContent().getClassref().getName(), id));
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
