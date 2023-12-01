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
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.dbentities.CfListcontentPK;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentKeywordService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfContentversionService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.serviceinterface.CfSitecontentService;
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import java.math.BigInteger;
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
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.uri.UriParameter;

/**
 *
 * @author raine
 */
@Component
public class EntityUtil {
    @Autowired private CfClassService cfclassService;
    @Autowired private CfAttributService cfattributService;
    @Autowired private CfAttributcontentService cfattributcontentService;
    @Autowired private CfClasscontentService cfClasscontentService;
    @Autowired private CfContentversionService cfcontentversionService;
    @Autowired private CfListService cflistService;
    @Autowired private CfListcontentService cflistcontentService;
    @Autowired private CfSitecontentService cfsitecontentService;
    @Autowired private CfClasscontentKeywordService cfclasscontentkeywordService;
    @Autowired private ContentUtil contentUtil;
    @Autowired HibernateUtil hibernateUtil;
    
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
                if (value instanceof String) {
                    prop.setValue(ValueType.PRIMITIVE, Boolean.parseBoolean((String)value));
                } else {
                    prop.setValue(ValueType.PRIMITIVE, ((Boolean)value));
                }    
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
                        attribut = cfattributService.findByNameAndClassref((String) attributname, contentdataoutput.getContent().getClassref());
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
    
    public Entity createEntity(EdmEntitySet edmEntitySet, Entity requestEntity) {
        Entity entity = requestEntity;
        
        CfClass clazz = cfclassService.findByName(edmEntitySet.getName());
        long newmax = getMaxID(clazz) + 1;
        
        CfClasscontent newclasscontent = new CfClasscontent();
        newclasscontent.setName(clazz.getName().toUpperCase() + "_" + newmax);
        newclasscontent.setCheckedoutby(BigInteger.valueOf(0));
        newclasscontent.setClassref(clazz);
        CfClasscontent newclasscontent2 = cfClasscontentService.create(newclasscontent);
        hibernateUtil.insertContent(newclasscontent);
        List<CfAttribut> attributlist = cfattributService.findByClassref(newclasscontent2.getClassref());
        attributlist.stream().forEach((attribut) -> {
            if (attribut.getAutoincrementor() == true) {
                List<CfClasscontent> classcontentlist2 = cfClasscontentService.findByClassref(newclasscontent2.getClassref());
                long max = 0;
                int last = classcontentlist2.size();
                if (1 == last) {
                    max = 0;
                } else {
                    CfClasscontent classcontent = classcontentlist2.get(last - 2);
                    CfAttributcontent attributcontent = cfattributcontentService.findByAttributrefAndClasscontentref(attribut, classcontent);        
                    if (attributcontent.getContentInteger().longValue() > max) {
                        max = attributcontent.getContentInteger().longValue();
                    }
                }
                CfAttributcontent newcontent = new CfAttributcontent();
                newcontent.setAttributref(attribut);
                newcontent.setClasscontentref(newclasscontent);
                newcontent.setContentInteger(BigInteger.valueOf(max+1));
                CfAttributcontent newcontent2 = cfattributcontentService.create(newcontent);
                Property prop = entity.getProperty(attribut.getName());
                prop.setType(GenericEdmProvider.getODataType(attribut).getFullQualifiedNameAsString());
                setPropValue(prop, max+1);
            } else {
                if ((0 != attribut.getAttributetype().getName().compareToIgnoreCase("classref")) && (0 != attribut.getAttributetype().getName().compareToIgnoreCase("assetref"))) {
                    CfAttributcontent newcontent = new CfAttributcontent();
                    newcontent.setAttributref(attribut);
                    newcontent.setClasscontentref(newclasscontent);
                    newcontent = contentUtil.setAttributValue(newcontent, entity.getProperty(attribut.getName()).getValue().toString());

                    cfattributcontentService.create(newcontent);
                    contentUtil.indexContent();
                } else {
                    if ((0 == attribut.getAttributetype().getName().compareToIgnoreCase("classref")) && (1 == attribut.getRelationtype())) { // 1:n
                        CfClass relationref = attribut.getRelationref();
                        ComplexValue complex = (ComplexValue) entity.getProperty(attribut.getName()).getValue();
                        for (Property prop : complex.getValue()) {
                            //System.out.println(prop.getName());
                            if (0 == prop.getName().compareToIgnoreCase("id")) {
                                CfClasscontent cfclasscontent = cfClasscontentService.findById(hibernateUtil.getContentRef(relationref.getName(), "id", ((Integer) prop.getValue()).longValue()));

                                CfAttributcontent newcontent = new CfAttributcontent();
                                newcontent.setAttributref(attribut);
                                newcontent.setClasscontentref(newclasscontent);
                                newcontent = contentUtil.setAttributValue(newcontent, cfclasscontent.getId().toString());

                                cfattributcontentService.create(newcontent);
                            }
                        }
                    } else {                                                // n:m Relation
                        // Create Datalist
                        String listname = getNewListName(attribut.getClassref(), attribut.getRelationref());
                        CfList newlist = new CfList();
                        newlist.setName(listname);
                        newlist.setClassref(attribut.getRelationref());

                        CfList newlist2 = cflistService.create(newlist);
                        
                        CfAttributcontent newcontent = new CfAttributcontent();
                        newcontent.setAttributref(attribut);
                        newcontent.setClasscontentref(newclasscontent);
                        newcontent = contentUtil.setAttributValue(newcontent, newlist2.getId().toString());

                        cfattributcontentService.create(newcontent);
                        
                        // Delete listcontent first
                        List<CfListcontent> contentList = cflistcontentService.findByListref(newlist2.getId());
                        for (CfListcontent content : contentList) {
                            cflistcontentService.delete(content);
                        }
                        
                        List<ComplexValue> complexList = (List<ComplexValue>) entity.getProperty(attribut.getName()).getValue();
                        for (ComplexValue complexval : complexList) {
                            for (Property prop : complexval.getValue()) {
                                //System.out.println(prop.getName());
                                if (0 == prop.getName().compareToIgnoreCase("id")) {
                                    // Add selected listcontent
                                    CfClasscontent cfclasscontent = cfClasscontentService.findById(hibernateUtil.getContentRef(attribut.getRelationref().getName(), "id", ((Integer) prop.getValue()).longValue()));
                                    CfListcontent listcontent = new CfListcontent();
                                    CfListcontentPK cflistcontentPK = new CfListcontentPK();
                                    cflistcontentPK.setListref(newlist2.getId());
                                    
                                    cflistcontentPK.setClasscontentref(cfclasscontent.getId());
                                    listcontent.setCfListcontentPK(cflistcontentPK);
                                    cflistcontentService.create(listcontent);
                                }
                            }
                        }
                        hibernateUtil.updateRelation(newlist2);
                    }
                }
            }
        });
        hibernateUtil.updateContent(newclasscontent);
        
        try {
            entity.setId(new URI(String.valueOf(newmax)));
        } catch (URISyntaxException ex) {
            LOGGER.error(ex.getMessage());
        }
        return entity;
    }
    
    public void updateEntity(EdmEntitySet edmEntitySet, List<UriParameter> keyParams, Entity entity, HttpMethod httpMethod) {
        CfClass clazz = cfclassService.findByName(edmEntitySet.getName());
        CfClasscontent cfclasscontent = null;
        for (UriParameter param : keyParams) {
            if (0 == param.getName().compareToIgnoreCase("id")) {
                cfclasscontent = cfClasscontentService.findById(hibernateUtil.getContentRef(clazz.getName(), "id", Long.parseLong(param.getText())));
            }
        }
        if (null != cfclasscontent) {
            List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByClasscontentref(cfclasscontent);
            for (CfAttributcontent attributcontent : attributcontentlist) {
                if (!attributcontent.getAttributref().getIdentity()) {
                    CfAttribut attribut = attributcontent.getAttributref();
                    
                    if ((0 != attribut.getAttributetype().getName().compareToIgnoreCase("classref")) && (0 != attribut.getAttributetype().getName().compareToIgnoreCase("assetref"))) {
                        Property p = entity.getProperty(attributcontent.getAttributref().getName());
                        if (null != p) {
                            contentUtil.setAttributValue(attributcontent, p.getValue().toString());
                            cfattributcontentService.edit(attributcontent);
                            contentUtil.indexContent();
                        } else {
                            if (httpMethod.equals(HttpMethod.PUT)) {
                                contentUtil.setAttributValue(attributcontent, null);
                                cfattributcontentService.edit(attributcontent);
                                contentUtil.indexContent();
                            }
                        }
                    } else {
                        if ((0 == attribut.getAttributetype().getName().compareToIgnoreCase("classref")) && (1 == attribut.getRelationtype())) { // 1:n
                            CfClass relationref = attribut.getRelationref();
                            try {
                                ComplexValue complex = (ComplexValue) entity.getProperty(attribut.getName()).getValue();
                                for (Property prop : complex.getValue()) {
                                    //System.out.println(prop.getName());
                                    if (0 == prop.getName().compareToIgnoreCase("id")) {
                                        CfClasscontent cfclasscontentref = cfClasscontentService.findById(hibernateUtil.getContentRef(relationref.getName(), "id", ((Integer) prop.getValue()).longValue()));

                                        contentUtil.setAttributValue(attributcontent, cfclasscontentref.getId().toString());
                                        cfattributcontentService.edit(attributcontent);
                                    }
                                }
                            } catch (Exception ex) {
                                LOGGER.debug(ex.getMessage());
                            }
                        } else {                                                // n:m Relation
                            // Delete listcontent first
                            List<CfListcontent> contentList = cflistcontentService.findByListref(attributcontent.getClasscontentlistref().getId());
                            for (CfListcontent content : contentList) {
                                cflistcontentService.delete(content);
                            }

                            List<ComplexValue> complexList = (List<ComplexValue>) entity.getProperty(attribut.getName()).getValue();
                            for (ComplexValue complexval : complexList) {
                                for (Property prop : complexval.getValue()) {
                                    //System.out.println(prop.getName());
                                    if (0 == prop.getName().compareToIgnoreCase("id")) {
                                        // Add selected listcontent
                                        CfClasscontent cfclasscontententry = cfClasscontentService.findById(hibernateUtil.getContentRef(attribut.getRelationref().getName(), "id", ((Integer) prop.getValue()).longValue()));
                                        CfListcontent listcontent = new CfListcontent();
                                        CfListcontentPK cflistcontentPK = new CfListcontentPK();
                                        cflistcontentPK.setListref(attributcontent.getClasscontentlistref().getId());

                                        cflistcontentPK.setClasscontentref(cfclasscontententry.getId());
                                        listcontent.setCfListcontentPK(cflistcontentPK);
                                        cflistcontentService.create(listcontent);
                                    }
                                }
                            }
                            hibernateUtil.updateRelation(attributcontent.getClasscontentlistref());
                        }
                    }
                }
            }
            hibernateUtil.updateContent(cfclasscontent);
        }
    }
    
    void deleteEntity(EdmEntitySet edmEntitySet, List<UriParameter> keyParams) {
        CfClass clazz = cfclassService.findByName(edmEntitySet.getName());

        try {
            CfClasscontent cfclasscontent = null;
            for (UriParameter param : keyParams) {
                if (0 == param.getName().compareToIgnoreCase("id")) {
                    cfclasscontent = cfClasscontentService.findById(hibernateUtil.getContentRef(clazz.getName(), "id", Long.parseLong(param.getText())));
                }
            }
            if (null != cfclasscontent) {
                cfclasscontent.setScrapped(true);

                // Delete from Listcontent - consistency
                List<CfListcontent> listcontent = cflistcontentService.findByClasscontentref(cfclasscontent.getId());
                for (CfListcontent lc : listcontent) {
                    cflistcontentService.delete(lc);
                    hibernateUtil.deleteRelation(cflistService.findById(lc.getCfListcontentPK().getListref()), cfClasscontentService.findById(lc.getCfListcontentPK().getClasscontentref()));
                }

                // Delete from Sitecontent - consistency
                List<CfSitecontent> sitecontent = cfsitecontentService.findByClasscontentref(cfclasscontent.getId());
                for (CfSitecontent sc : sitecontent) {
                    cfsitecontentService.delete(sc);
                }

                cfClasscontentService.edit(cfclasscontent);
                hibernateUtil.updateContent(cfclasscontent);
                
                // Delete corresponding attributcontent entries
                List<CfAttributcontent> attributcontentlistdummy = cfattributcontentService.findByClasscontentref(cfclasscontent);
                for (CfAttributcontent attributcontent : attributcontentlistdummy) {
                    cfattributcontentService.delete(attributcontent);
                }
                // Delete corresponding keywordcontent entries
                List<CfClasscontentkeyword> keywordcontentdummy = cfclasscontentkeywordService.findByClassContentRef(cfclasscontent.getId());
                for (CfClasscontentkeyword keywordcontent : keywordcontentdummy) {
                    cfclasscontentkeywordService.delete(keywordcontent);
                }

                cfClasscontentService.delete(cfclasscontent);
                try {
                    hibernateUtil.deleteContent(cfclasscontent);
                } catch (javax.persistence.NoResultException ex) {
                    LOGGER.warn(ex.getMessage());
                }
            }
        } catch (javax.persistence.NoResultException ex) {
            
        }
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
                    cfAtt = cfattributService.findByNameAndClassref(key, cfclasscontent.getClassref());
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
    
    private long getMaxID(CfClass clazz) {
        long max = 0;
        List<CfAttribut> attributlist = cfattributService.findByClassref(clazz);
        for (CfAttribut attribut : attributlist) {
            if (attribut.getAutoincrementor()) {
                List<CfClasscontent> classcontentlist2 = cfClasscontentService.findByClassref(clazz);
                int last = classcontentlist2.size();
                if (1 == last) {
                    max = 0;
                } else {
                    CfClasscontent classcontent = classcontentlist2.get(last - 2);
                    CfAttributcontent attributcontent = cfattributcontentService.findByAttributrefAndClasscontentref(attribut, classcontent);        
                    if (attributcontent.getContentInteger().longValue() > max) {
                        max = attributcontent.getContentInteger().longValue();
                    }
                }
            }
        }
        return max;
    }
    
    private String getNewListName(CfClass clazz, CfClass clazz_ref) {
        String listname = clazz.getName() + "_" + clazz_ref.getName() + "_%";
        List<CfList> dummylist = cflistService.findByNameLike(listname);
        long max = 0;
        for (CfList listentry : dummylist) {
            String part = listentry.getName().substring(listentry.getName().lastIndexOf("_") + 1, listentry.getName().length());
            System.out.println(part);
            try {
                long id = Long.parseLong(part);
                if (id > max) {
                    max = id;
                }
            } catch (Exception ex) {
                //
            }
        }
        return clazz.getName() + "_" + clazz_ref.getName() + "_" + (max + 1);
    }
}
