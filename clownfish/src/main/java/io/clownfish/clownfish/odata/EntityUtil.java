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
import io.clownfish.clownfish.datamodels.RestDatabaseInsert;
import io.clownfish.clownfish.datamodels.TableData;
import io.clownfish.clownfish.dbentities.*;
import io.clownfish.clownfish.jdbc.DatatableProperties;
import io.clownfish.clownfish.jdbc.JDBCUtil;
import io.clownfish.clownfish.jdbc.TableField;
import io.clownfish.clownfish.jdbc.TableFieldStructure;
import io.clownfish.clownfish.serviceinterface.*;
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.uri.UriParameter;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.joda.time.DateTimeZone;

/**
 *
 * @author raine
 */
@Component
public class EntityUtil {
    @Autowired private CfClassService cfclassService;
    @Autowired private CfAttributService cfattributService;
    @Autowired private CfAttributcontentService cfattributcontentService;
    @Autowired private CfClasscontentService cfclasscontentService;
    @Autowired private CfContentversionService cfcontentversionService;
    @Autowired private CfListService cflistService;
    @Autowired private CfListcontentService cflistcontentService;
    @Autowired private CfSitecontentService cfsitecontentService;
    @Autowired private CfSitelistService cfsitelistService;
    @Autowired private CfClasscontentKeywordService cfclasscontentkeywordService;
    @Autowired private CfAssetlistService cfassetlistService;
    @Autowired private CfAssetlistcontentService cfassetlistcontentService;
    @Autowired private ContentUtil contentUtil;
    @Autowired HibernateUtil hibernateUtil;
    GenericEdmProvider edmprovider;
    
    private @Getter @Setter HashMap<String, CsdlSingleton> singletonlist = new HashMap<>();
    private @Getter @Setter HashMap<String, CsdlEntitySet> entitysetlist = new HashMap<>();
    private @Getter @Setter HashMap<FullQualifiedName, CsdlEntityType> entitytypelist = new HashMap<>();
    private @Getter @Setter HashMap<FullQualifiedName, CsdlComplexType> complextypelist = new HashMap<>();
    private @Getter @Setter HashMap<FullQualifiedName, SourceStructure> entitysourcelist = new HashMap<>();
    private @Getter @Setter HashMap<FullQualifiedName, TableData> entitystructurelist = new HashMap<>();
    private @Getter @Setter CsdlEntityContainer entityContainer = null;
    private @Getter @Setter List<CsdlSchema> schemas = null;
    
    private static final HashMap<String, CfAttribut> attributmap = new HashMap<>();

    final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EntityUtil.class);
    
    public void init(GenericEdmProvider edmprovider) {
        this.edmprovider = edmprovider;
    }
    
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
            case "Edm.Int64":
                if (value instanceof String) {
                    prop.setValue(ValueType.PRIMITIVE, Long.parseLong((String)value));
                } else if (value instanceof Long) {
                    prop.setValue(ValueType.PRIMITIVE, ((Long) value).intValue());
                } else if (value instanceof Integer) {
                    prop.setValue(ValueType.PRIMITIVE, (Long) value);
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
                    if (((((String) value).compareToIgnoreCase("true")) != 0) && (((String) value).compareToIgnoreCase("false")) != 0 ) {
                        if (0 == ((String) value).compareToIgnoreCase("1")) {
                            value = "true";
                        } else {
                            value = "false";
                        }
                    }
                    prop.setValue(ValueType.PRIMITIVE, Boolean.parseBoolean((String)value));
                } else {
                    prop.setValue(ValueType.PRIMITIVE, ((Boolean)value));
                }    
                break;
            case "Edm.DateTimeOffset":
                if (value instanceof String) {
                    String pattern = "dd.MM.yyyy HH:mm:ss";
                    DateTime dt = null;
                    try {
                        dt = DateTime.parse((String)value, DateTimeFormat.forPattern(pattern));
                    } catch (Exception ex) {
                        try {
                            pattern = "yyyy-MM-dd HH:mm:ss";
                            dt = DateTime.parse((String)value, DateTimeFormat.forPattern(pattern));
                        } catch (Exception ex1) {
                            try {
                                pattern = "yyyy-MM-dd HH:mm:ss.SSS";
                                dt = DateTime.parse((String)value, DateTimeFormat.forPattern(pattern));
                            } catch (Exception ex2) {

                            }
                        }
                    }
                    prop.setValue(ValueType.PRIMITIVE, dt.toDate());
                } else {
                    prop.setValue(ValueType.PRIMITIVE, ((Date)value));
                }
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
                        if (0 == attribut.getAttributetype().getName().compareToIgnoreCase("assetref")) {
                            Property coll_prop = new Property();
                            List<Long> values = new ArrayList<>();
                            String assetlistname = (String) hm.get(attributname);
                            if (attribut.getIdentity()) {
                                id = (String) hm.get(attributname).toString();
                            }
                            try {
                                CfAssetlist assetlist = cfassetlistService.findByName(assetlistname);
                                List<CfAssetlistcontent> medialist = cfassetlistcontentService.findByAssetlistref(assetlist.getId());
                                for (CfAssetlistcontent listcontent : medialist) {
                                    values.add(listcontent.getCfAssetlistcontentPK().getAssetref());
                                }
                                coll_prop.setValue(ValueType.COLLECTION_PRIMITIVE, values);
                                coll_prop.setName((String)attributname);
                                coll_prop.setType(ValueType.COLLECTION_PRIMITIVE.toString());
                                entity.addProperty(coll_prop);
                            } catch (NoResultException nre) {
                                LOGGER.warn("Datalist not set for attribute " + (String)attributname + " of class " + contentdataoutput.getContent().getClassref().getName());
                                coll_prop.setValue(ValueType.COLLECTION_PRIMITIVE, null);
                                entity.addProperty(coll_prop);
                            }
                        } else {
                            if ((0 == attribut.getAttributetype().getName().compareToIgnoreCase("classref")) && (1 == attribut.getRelationtype())) { // 1:n
                                Property prop = new Property();
                                prop.setName(attribut.getName());
                                Long content_id = (Long)hm.get(attributname);
                                if (null != content_id) {
                                    CfClasscontent cfclasscontent = cfclasscontentService.findById(content_id);
                                    prop.setValue(ValueType.COMPLEX, createComplexVal(cfclasscontent));
                                    prop.setType("OData.Complex." + cfclasscontent.getClassref().getName());
                                    entity.addProperty(prop);
                                }
                            } else {                                                // n:m Relation
                                Property coll_prop = new Property();
                                List<ComplexValue> values = new ArrayList<>();
                                String datalistname = (String) hm.get(attributname);
                                if (null != datalistname) {
                                    if (attribut.getIdentity()) {
                                        id = (String) hm.get(attributname).toString();
                                    }
                                    try {
                                        CfList datalist = cflistService.findByName(datalistname);
                                        List<CfListcontent> contentlist = cflistcontentService.findByListref(datalist.getId());
                                        for (CfListcontent listcontent : contentlist) {
                                            CfClasscontent cfclasscontent = cfclasscontentService.findById(listcontent.getCfListcontentPK().getClasscontentref());
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
                                } else {
                                    LOGGER.warn("Datalist not set for attribute " + (String)attributname + " of class " + contentdataoutput.getContent().getClassref().getName());
                                    coll_prop.setValue(ValueType.COLLECTION_COMPLEX, null);
                                    entity.addProperty(coll_prop);
                                }
                            }
                        }
                    }
                }
            }
        }
        entity.setId(createId(contentdataoutput.getContent().getClassref().getName(), id));
        return entity;
    }
    
    public Entity makeEntity(String tablename, HashMap hm, TableFieldStructure tfs) {
        Entity entity = new Entity();
        String id = "";
        
        for (Object attributname : hm.keySet()) {
            TableField tf = tfs.getTableFieldByName((String)attributname);
            if (tf.isPrimaryKey()) {
                id += (String) hm.get(attributname).toString();
            }
            Property prop = new Property();
            prop.setName(tf.getName());
            prop.setType(GenericEdmProvider.getODataDB2Type(tf.getType()).getFullQualifiedNameAsString());
            setPropValue(prop, hm.get(attributname));
            entity.addProperty(prop);
        }

        entity.setId(createId(tablename, id));
        return entity;
    }
    
    public Entity createEntity(EdmEntitySet edmEntitySet, Entity requestEntity, EdmEntityType edmEntityType, SourceStructure source) {
        Entity entity = requestEntity;
        if (0 == source.getSource()) {
            switch (source.getList()) {
                case 0:
                    CfClass clazz = cfclassService.findByName(edmEntitySet.getName());
                    long newmax = getMaxID(clazz);

                    CfClasscontent newclasscontent = new CfClasscontent();
                    // get all primary attributes
                    List<CfAttribut> identities = cfattributService.findByClassref(clazz).stream().filter(CfAttribut::getIdentity).collect(Collectors.toList());
                    // get key names
                    List<String> id_names = identities.stream().map(CfAttribut::getName).collect(Collectors.toList());
                    // filter property values by key names
                    List<Object> prop_vals = requestEntity.getProperties().stream()
                            .filter(prop -> !prop.getName().equals("id") && id_names.contains(prop.getName()))
                            .map(Property::getValue).collect(Collectors.toList());
                    StringBuilder sb = new StringBuilder();
                    sb.append(newmax).append("_");
                    for (var id : prop_vals) {
                        String val = ((String)id).replaceAll("[^a-zA-Z0-9]", "_");
                        sb.append(val).append("_");
                    }
                    newclasscontent.setName(clazz.getName().toUpperCase() + "_" + sb.deleteCharAt(sb.lastIndexOf("_")));
                    newclasscontent.setCheckedoutby(BigInteger.valueOf(0));
                    newclasscontent.setClassref(clazz);
                    CfClasscontent newclasscontent2 = cfclasscontentService.create(newclasscontent);
                    List<CfAttribut> attributlist = cfattributService.findByClassref(newclasscontent2.getClassref());
                    boolean canCreate = true;
                    for (CfAttribut attribut : attributlist) {
                        if (attribut.getAutoincrementor() == true) {
                            List<CfClasscontent> classcontentlist2 = cfclasscontentService.findByClassref(newclasscontent2.getClassref());
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
                                boolean found = false;
                                if (attribut.getIdentity()) {
                                    List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByAttributref(attribut);
                                    for (CfAttributcontent attributcontent : attributcontentlist) {
                                        switch (attribut.getAttributetype().getName()) {
                                            case "string":
                                                if (0 == attributcontent.getContentString().compareTo(entity.getProperty(attribut.getName()).getValue().toString())) {
                                                    found = true;
                                                    canCreate = false;
                                                }
                                                break;
                                        }
                                        if (found) break;
                                    }
                                }
                                if ((attribut.getClassref().isLoginclass()) && (0 == attribut.getName().compareToIgnoreCase("created"))) {
                                    CfAttributcontent newcontent = new CfAttributcontent();
                                    newcontent.setAttributref(attribut);
                                    newcontent.setClasscontentref(newclasscontent);
                                    newcontent = contentUtil.setAttributValue(newcontent, new DateTime().toString(new DateTime().toString(DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss").withZone(DateTimeZone.forID("Europe/Berlin")))));
                                    cfattributcontentService.create(newcontent);

                                    contentUtil.indexContent();
                                    found = true;
                                }
                                if (!found) {
                                    CfAttributcontent newcontent = new CfAttributcontent();
                                    newcontent.setAttributref(attribut);
                                    newcontent.setClasscontentref(newclasscontent);
                                    Property prop = entity.getProperty(attribut.getName());
                                    if (null != prop) {
                                        if (!attribut.getExt_mutable()) {
                                            if ((null != attribut.getDefault_val()) && (!attribut.getDefault_val().isBlank())) {
                                                newcontent = contentUtil.setAttributValue(newcontent, attribut.getDefault_val());
                                            } else {
                                                newcontent = contentUtil.setAttributValue(newcontent, null);
                                            }
                                        } else {
                                            newcontent = contentUtil.setAttributValue(newcontent, entity.getProperty(attribut.getName()).getValue().toString());
                                        }
                                    } else {
                                        if ((null != attribut.getDefault_val()) && (!attribut.getDefault_val().isBlank())) {
                                            newcontent = contentUtil.setAttributValue(newcontent, attribut.getDefault_val());
                                        } else {
                                            newcontent = contentUtil.setAttributValue(newcontent, null);
                                        }
                                    }
                                    cfattributcontentService.create(newcontent);

                                    contentUtil.indexContent();
                                }
                            } else {
                                if (0 == attribut.getAttributetype().getName().compareToIgnoreCase("assetref")) {
                                    // Create Assetlist
                                    String listname = getNewAssetListName(attribut.getClassref(), attribut.getName());
                                    CfAssetlist newlist = new CfAssetlist();
                                    newlist.setName(listname);

                                    CfAssetlist newlist2 = cfassetlistService.create(newlist);

                                    CfAttributcontent newcontent = new CfAttributcontent();
                                    newcontent.setAttributref(attribut);
                                    newcontent.setClasscontentref(newclasscontent);
                                    newcontent = contentUtil.setAttributValue(newcontent, listname);
                                    cfattributcontentService.create(newcontent);

                                    // Delete assetlistcontent first
                                    List<CfAssetlistcontent> contentList = cfassetlistcontentService.findByAssetlistref(newlist2.getId());
                                    for (CfAssetlistcontent content : contentList) {
                                        cfassetlistcontentService.delete(content);
                                    }

                                    List<Integer> longList = (List<Integer>) entity.getProperty(attribut.getName()).getValue();
                                    for (Integer val : longList) {
                                        // Add selected assetlistcontent
                                        CfAssetlistcontent assetlistcontent = new CfAssetlistcontent();
                                        CfAssetlistcontentPK cfassetlistcontentPK = new CfAssetlistcontentPK();
                                        cfassetlistcontentPK.setAssetlistref(newlist2.getId());
                                        cfassetlistcontentPK.setAssetref(val.longValue());
                                        assetlistcontent.setCfAssetlistcontentPK(cfassetlistcontentPK);
                                        cfassetlistcontentService.create(assetlistcontent);
                                    }
                                } else {
                                    if ((0 == attribut.getAttributetype().getName().compareToIgnoreCase("classref")) && (1 == attribut.getRelationtype())) { // 1:n
                                        CfClass relationref = attribut.getRelationref();
                                        ComplexValue complex = (ComplexValue) entity.getProperty(attribut.getName()).getValue();
                                        for (Property prop : complex.getValue()) {
                                            //System.out.println(prop.getName());
                                            if (0 == prop.getName().compareToIgnoreCase("id")) {
                                                CfClasscontent cfclasscontent = cfclasscontentService.findById(hibernateUtil.getContentRef(relationref.getName(), "id", ((Integer) prop.getValue()).longValue()));

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
                                                    CfClasscontent cfclasscontent = cfclasscontentService.findById(hibernateUtil.getContentRef(attribut.getRelationref().getName(), "id", ((Integer) prop.getValue()).longValue()));
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
                        }
                    };
                    if (canCreate) {
                        hibernateUtil.insertContent(newclasscontent);
                        contentUtil.commit(newclasscontent);
                        try {
                            entity.setId(new URI(String.valueOf(newmax)));
                        } catch (URISyntaxException ex) {
                            LOGGER.error(ex.getMessage());
                        }
                        return entity;
                    } else {
                        cfattributcontentService.delete(newclasscontent.getId());
                        cfclasscontentService.delete(newclasscontent2);
                        return null;
                    }
                case 1:
                    clazz = cfclassService.findByName(edmEntityType.getName());
                    String name = edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-4);
                    CfList dummylist = cflistService.findByClassrefAndName(clazz, name);
                    
                    Session session_tables = HibernateUtil.getClasssessions().get("tables").getSessionFactory().openSession();
                    Query query = session_tables.createQuery("FROM " + clazz.getName() + " c WHERE id_ = " + entity.getProperty("id").getValue());
                    Map content = (Map) query.getSingleResult();

                    CfClasscontent cfclasscontent = cfclasscontentService.findById((long)content.get("cf_contentref"));
                    CfListcontentPK lcpk = new CfListcontentPK();
                    lcpk.setListref(dummylist.getId());
                    lcpk.setClasscontentref(cfclasscontent.getId());
                    CfListcontent lc = new CfListcontent(lcpk);
                    cflistcontentService.create(lc);
                    return entity;
                case 2:
                    clazz = cfclassService.findByName(edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-5));
                    List<ContentDataOutput> genericPropListSet = hibernateUtil.getDatalist(clazz.getName());
                    
                    name = entity.getProperty("name").getValue().toString().replaceAll("[^a-zA-Z0-9]", "_");
                    dummylist = cflistService.findByClassrefAndName(clazz, name);
                    if (null == dummylist) {
                        CfList newlist = new CfList();
                        newlist.setClassref(clazz);
                        newlist.setName(name);
                        newlist = cflistService.create(newlist);
                        
                        List<CfListcontent> listcontent = cflistcontentService.findByListref(newlist.getId());
                        for (CfListcontent entry : listcontent) {
                            cflistcontentService.delete(entry);
                        }
                        ArrayList listset = (ArrayList) entity.getProperty("listset").getValue();
                        for (Object entry : listset) {
                            CfListcontent listcontententry = new CfListcontent();
                            listcontententry.setCfListcontentPK(new CfListcontentPK(newlist.getId(), getContentId(genericPropListSet, Long.valueOf((Integer)entry))));
                            cflistcontentService.create(listcontententry);
                        }
                        Property prop_listset = new Property();
                        prop_listset.setName("listset");
                        prop_listset.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName().getFullQualifiedNameAsString());
                        prop_listset.setValue(ValueType.COLLECTION_PRIMITIVE, listset);
                        
                        edmprovider.init();
                        Thread edmprovider_thread = new Thread(edmprovider);
                        edmprovider_thread.start();
                        
                        entity.getProperty("id").setValue(ValueType.PRIMITIVE, newlist.getId());
                        return entity;
                    } else {
                        return null;
                    }
                default:
                    return null;
            }
        } else {
            JDBCUtil jdbcutil = new JDBCUtil(source.getClassname(), source.getUrl(), source.getUser(), source.getPassword());
            Connection con = jdbcutil.getConnection();
            RestDatabaseInsert rdi = null;
            if (null != con) {
                try {
                    DatabaseMetaData dmd = con.getMetaData();
                    DatatableProperties datatableproperties = new DatatableProperties();
                    datatableproperties.setTablename(source.getClassname());
                    ResultSet resultSetTables = dmd.getTables(null, null, null, new String[]{"TABLE"});
                    boolean found = false;
                    while(resultSetTables.next() && !found)
                    {
                        String tablename = resultSetTables.getString("TABLE_NAME");
                        if (0 == source.getTablename().compareToIgnoreCase(tablename)) {
                            rdi = jdbcutil.manageTableInsert(con, dmd, tablename, entityValues(entity));
                            found = true;
                        }
                    }
                    con.close();
                } catch (SQLException ex) {
                    LOGGER.error(ex.getMessage());
                }
                finally {
                    try {
                        con.close();
                    }
                    catch (SQLException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            } else {
                return null;
            }
            if ((null == rdi) || (0 == rdi.getCount())) {
                return null;
            } else {
                return entity;
            }
        }
    }
    
    public boolean updateEntity(EdmEntitySet edmEntitySet, List<UriParameter> keyParams, Entity entity, HttpMethod httpMethod, SourceStructure source) {
        if (0 == source.getSource()) {
            switch (source.getList()) {
                case 0:
                    CfClass clazz = cfclassService.findByName(edmEntitySet.getName());
                    CfClasscontent cfclasscontent = null;
                    for (UriParameter param : keyParams) {
                        if (0 == param.getName().compareToIgnoreCase("id")) {
                            cfclasscontent = cfclasscontentService.findById(hibernateUtil.getContentRef(clazz.getName(), "id", Long.parseLong(param.getText())));
                        }
                    }
                    if (null != cfclasscontent) {
                        List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByClasscontentref(cfclasscontent);
                        for (CfAttributcontent attributcontent : attributcontentlist) {
                            if (!attributcontent.getAttributref().getIdentity()) {
                                CfAttribut attribut = attributcontent.getAttributref();

                                if ((0 != attribut.getAttributetype().getName().compareToIgnoreCase("classref")) && (0 != attribut.getAttributetype().getName().compareToIgnoreCase("assetref"))) {
                                    if (attribut.getExt_mutable()) {
                                        Property p = entity.getProperty(attributcontent.getAttributref().getName());
                                        if (null != p) {
                                            if (0 != attribut.getAttributetype().getName().compareToIgnoreCase("datetime")) {
                                                contentUtil.setAttributValue(attributcontent, p.getValue().toString());
                                            } else {
                                                contentUtil.setAttributValue(attributcontent, ((GregorianCalendar) p.getValue()).toZonedDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                                            }
                                            cfattributcontentService.edit(attributcontent);
                                            contentUtil.indexContent();
                                        } else {
                                            if (httpMethod.equals(HttpMethod.PUT)) {
                                                contentUtil.setAttributValue(attributcontent, null);
                                                cfattributcontentService.edit(attributcontent);
                                                contentUtil.indexContent();
                                            }
                                        }
                                    }
                                } else {
                                    if (0 == attribut.getAttributetype().getName().compareToIgnoreCase("assetref")) {
                                        // Delete assetlistcontent first
                                        List<CfAssetlistcontent> contentList = cfassetlistcontentService.findByAssetlistref(attributcontent.getAssetcontentlistref().getId());
                                        for (CfAssetlistcontent content : contentList) {
                                            cfassetlistcontentService.delete(content);
                                        }

                                        List<Integer> longList = (List<Integer>) entity.getProperty(attribut.getName()).getValue();
                                        for (Integer val : longList) {
                                            // Add selected assetlistcontent
                                            CfAssetlistcontent assetlistcontent = new CfAssetlistcontent();
                                            CfAssetlistcontentPK cfassetlistcontentPK = new CfAssetlistcontentPK();
                                            cfassetlistcontentPK.setAssetlistref(attributcontent.getAssetcontentlistref().getId());
                                            cfassetlistcontentPK.setAssetref(val.longValue());
                                            assetlistcontent.setCfAssetlistcontentPK(cfassetlistcontentPK);
                                            cfassetlistcontentService.create(assetlistcontent);
                                        }
                                    } else {
                                        if ((0 == attribut.getAttributetype().getName().compareToIgnoreCase("classref")) && (1 == attribut.getRelationtype())) { // 1:n
                                            CfClass relationref = attribut.getRelationref();
                                            try {
                                                ComplexValue complex = (ComplexValue) entity.getProperty(attribut.getName()).getValue();
                                                for (Property prop : complex.getValue()) {
                                                    //System.out.println(prop.getName());
                                                    if (0 == prop.getName().compareToIgnoreCase("id")) {
                                                        CfClasscontent cfclasscontentref = cfclasscontentService.findById(hibernateUtil.getContentRef(relationref.getName(), "id", ((Integer) prop.getValue()).longValue()));

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
                                                        CfClasscontent cfclasscontententry = cfclasscontentService.findById(hibernateUtil.getContentRef(attribut.getRelationref().getName(), "id", ((Integer) prop.getValue()).longValue()));
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
                        }
                        hibernateUtil.updateContent(cfclasscontent);
                        contentUtil.commit(cfclasscontent);
                        return true;
                    } else {
                        return false;
                    }
                case 2:
                    clazz = cfclassService.findByName(edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-5));
                    List<ContentDataOutput> genericPropListSet = hibernateUtil.getDatalist(clazz.getName());
                    String name = "";
                    Long id = 0L;
                    for (UriParameter param : keyParams) {
                        if (0 == param.getName().compareToIgnoreCase("name")) {
                            name = param.getText().substring(1, param.getText().length()-1);
                        }
                        if (0 == param.getName().compareToIgnoreCase("id")) {
                            id = Long.valueOf(param.getText());
                        }
                    }
                    CfList dummylist = null;
                    if (0 != id) {
                        dummylist = cflistService.findById(id);
                    }
                    if (null == dummylist) {
                        return false;
                    } else {
                        dummylist.setName(entity.getProperty("name").getValue().toString().replaceAll("[^a-zA-Z0-9]", "_"));
                        cflistService.edit(dummylist);
                        
                        List<CfListcontent> listcontent = cflistcontentService.findByListref(dummylist.getId());
                        for (CfListcontent entry : listcontent) {
                            cflistcontentService.delete(entry);
                        }
                        ArrayList listset = (ArrayList) entity.getProperty("listset").getValue();
                        for (Object entry : listset) {
                            CfListcontent lc = new CfListcontent();
                            lc.setCfListcontentPK(new CfListcontentPK(dummylist.getId(), getContentId(genericPropListSet, Long.valueOf((Integer)entry))));
                            cflistcontentService.create(lc);
                        }
                        Property prop_listset = new Property();
                        prop_listset.setName("listset");
                        prop_listset.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName().getFullQualifiedNameAsString());
                        prop_listset.setValue(ValueType.COLLECTION_PRIMITIVE, listset);
                        
                        edmprovider.init();
                        Thread edmprovider_thread = new Thread(edmprovider);
                        edmprovider_thread.start();
                        return true;
                    }
                default:
                    return false;
            }
        } else {
            JDBCUtil jdbcutil = new JDBCUtil(source.getClassname(), source.getUrl(), source.getUser(), source.getPassword());
            Connection con = jdbcutil.getConnection();
            boolean modified = false;
            if (null != con) {
                try {
                    DatabaseMetaData dmd = con.getMetaData();
                    DatatableProperties datatableproperties = new DatatableProperties();
                    datatableproperties.setTablename(source.getTablename());
                    ResultSet resultSetTables = dmd.getTables(null, null, null, new String[]{"TABLE"});
                    boolean found = false;
                    while(resultSetTables.next() && !found)
                    {
                        String tablename = resultSetTables.getString("TABLE_NAME");
                        if (0 == datatableproperties.getTablename().compareToIgnoreCase(tablename)) {
                            int count = jdbcutil.manageTableUpdate(con, dmd, tablename, entityValues(keyParams), entityValues(entity));
                            if (count > 0) {
                                modified = true;
                            }
                            found = true;
                        }
                    }
                    con.close();
                    return modified;
                } catch (SQLException ex) {
                    LOGGER.error(ex.getMessage());
                }
                finally {
                    try {
                        con.close();
                        return modified;
                    }
                    catch (SQLException e) {
                        LOGGER.error(e.getMessage());
                        return modified;
                    }
                }
            } else {
                return modified;
            }
        }
    }
    
    public boolean deleteEntity(EdmEntitySet edmEntitySet, List<UriParameter> keyParams, SourceStructure source) {
        if (0 == source.getSource()) {
            String name = "";
            CfList dummylist = null;
            CfClasscontent cfclasscontent = null;
            switch (source.getList()) {
                case 0:
                    CfClass clazz = cfclassService.findByName(edmEntitySet.getName());
                    try {
                        cfclasscontent = null;
                        for (UriParameter param : keyParams) {
                            if (0 == param.getName().compareToIgnoreCase("id")) {
                                cfclasscontent = cfclasscontentService.findById(hibernateUtil.getContentRef(clazz.getName(), "id", Long.parseLong(param.getText())));
                            }
                        }
                        if (null != cfclasscontent) {
                            cfclasscontent.setScrapped(true);

                            // Delete from Listcontent - consistency
                            List<CfListcontent> listcontent = cflistcontentService.findByClasscontentref(cfclasscontent.getId());
                            for (CfListcontent lc : listcontent) {
                                cflistcontentService.delete(lc);
                                hibernateUtil.deleteRelation(cflistService.findById(lc.getCfListcontentPK().getListref()), cfclasscontentService.findById(lc.getCfListcontentPK().getClasscontentref()));
                            }

                            // Delete from Sitecontent - consistency
                            List<CfSitecontent> sitecontent = cfsitecontentService.findByClasscontentref(cfclasscontent.getId());
                            for (CfSitecontent sc : sitecontent) {
                                cfsitecontentService.delete(sc);
                            }

                            cfclasscontentService.edit(cfclasscontent);
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

                            cfclasscontentService.delete(cfclasscontent);
                            try {
                                hibernateUtil.deleteContent(cfclasscontent);
                                return true;
                            } catch (javax.persistence.NoResultException ex) {
                                LOGGER.warn(ex.getMessage());
                                return false;
                            }
                        } else {
                            return false;
                        }
                    } catch (javax.persistence.NoResultException ex) {
                        return false;
                    }
                case 1:
                    name = edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-4);
                    clazz = cfclassService.findByName(edmEntitySet.getEntityType().getName());
                    dummylist = cflistService.findByClassrefAndName(clazz, name);
                    
                    cfclasscontent = null;
                    for (UriParameter param : keyParams) {
                        if (0 == param.getName().compareToIgnoreCase("id")) {
                            cfclasscontent = cfclasscontentService.findById(hibernateUtil.getContentRef(clazz.getName(), "id", Long.parseLong(param.getText())));
                        }
                    }
                    CfListcontentPK lcpk = new CfListcontentPK();
                    lcpk.setListref(dummylist.getId());
                    lcpk.setClasscontentref(cfclasscontent.getId());
                    CfListcontent lcdel = new CfListcontent(lcpk);
                    cflistcontentService.delete(lcdel);
                    return true;    
                case 2:
                    clazz = cfclassService.findByName(edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-5));
                    name = "";
                    Long id = 0L;
                    for (UriParameter param : keyParams) {
                        if (0 == param.getName().compareToIgnoreCase("name")) {
                            name = param.getText().substring(1, param.getText().length()-1);
                        }
                        if (0 == param.getName().compareToIgnoreCase("id")) {
                            id = Long.valueOf(param.getText());
                        }
                    }
                    dummylist = null;
                    if (!name.isBlank()) {
                        dummylist = cflistService.findByClassrefAndName(clazz, name);
                    }
                    if (0 != id) {
                        dummylist = cflistService.findById(id);
                    }
                    if (null == dummylist) {
                        return false;
                    } else {
                        cflistService.delete(dummylist);
                        
                        // Delete from Listcontent - consistency
                        List<CfListcontent> listcontent = cflistcontentService.findByListref(dummylist.getId());
                        for (CfListcontent lc : listcontent) {
                            cflistcontentService.delete(lc);
                            hibernateUtil.deleteRelation(cflistService.findById(lc.getCfListcontentPK().getListref()), cfclasscontentService.findById(lc.getCfListcontentPK().getClasscontentref()));
                        }

                        // Delete from Sitecontent - consistency
                        List<CfSitelist> sitecontent = cfsitelistService.findByListref(dummylist.getId());
                        for (CfSitelist sc : sitecontent) {
                            cfsitelistService.delete(sc);
                        }
                        edmprovider.init();
                        Thread edmprovider_thread = new Thread(edmprovider);
                        edmprovider_thread.start();
                        return true;
                    }
                default:
                    return false;
            }
        } else {
            JDBCUtil jdbcutil = new JDBCUtil(source.getClassname(), source.getUrl(), source.getUser(), source.getPassword());
            Connection con = jdbcutil.getConnection();
            boolean deleted = false;
            if (null != con) {
                try {
                    DatabaseMetaData dmd = con.getMetaData();
                    DatatableProperties datatableproperties = new DatatableProperties();
                    datatableproperties.setTablename(source.getTablename());
                    ResultSet resultSetTables = dmd.getTables(null, null, null, new String[]{"TABLE"});
                    boolean found = false;
                    while(resultSetTables.next() && !found)
                    {
                        String tablename = resultSetTables.getString("TABLE_NAME");
                        if (0 == datatableproperties.getTablename().compareToIgnoreCase(tablename)) {
                            int count = jdbcutil.manageTableDelete(con, dmd, tablename, entityValues(keyParams));
                            if (count > 0) {
                                deleted = true;
                            }
                            found = true;
                        }
                    }
                    con.close();
                    return deleted;
                } catch (SQLException ex) {
                    LOGGER.error(ex.getMessage());
                    return deleted;
                }
                finally {
                    try {
                        con.close();
                        return deleted;
                    }
                    catch (SQLException e) {
                        LOGGER.error(e.getMessage());
                        return deleted;
                    }
                }
            } else {
                return deleted;
            }
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
                LOGGER.warn("ContentID: " + cfclasscontent.getId() + " Name: " + cfclasscontent.getName() + " has no version info.");
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
                    CfClasscontent cfclasscontentref = cfclasscontentService.findById(content_id);
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
                List<CfClasscontent> classcontentlist2 = cfclasscontentService.findByClassref(clazz);
                int last = classcontentlist2.size();
                if (last <= 1)  {
                    max = last+1;
                } else {
                    CfClasscontent classcontent = classcontentlist2.get(last - 1);
                    CfAttributcontent attributcontent = cfattributcontentService.findByAttributrefAndClasscontentref(attribut, classcontent);        
                    if (attributcontent.getContentInteger().longValue() > max) {
                        max = attributcontent.getContentInteger().longValue()+1;
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
    
    private String getNewAssetListName(CfClass clazz, String attributename) {
        String listname = clazz.getName() + "_" + attributename + "_%";
        List<CfAssetlist> dummylist = cfassetlistService.findByNameLike(listname);
        long max = 0;
        for (CfAssetlist listentry : dummylist) {
            String part = listentry.getName().substring(listentry.getName().lastIndexOf("_") + 1, listentry.getName().length());
            try {
                long id = Long.parseLong(part);
                if (id > max) {
                    max = id;
                }
            } catch (Exception ex) {
                //
            }
        }
        return clazz.getName() + "_" + attributename + "_" + (max + 1);
    }

    private HashMap<String, String> entityValues(Entity edmEntitySet) {
        HashMap values = new HashMap<>();
        for (Property prop : edmEntitySet.getProperties()) {
            switch (prop.getType()) {
                case "Edm.String":
                    if (null != prop.getValue()) {
                        values.put(prop.getName(), (String)prop.getValue());
                    } else {
                        values.put(prop.getName(), prop.getValue());
                    }
                    break;
                case "Edm.DateTimeOffset":
                    if (null != prop.getValue()) {
                        values.put(prop.getName(), prop.getValue());
                        //values.put(prop.getName(), ((GregorianCalendar)prop.getValue()).toZonedDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss")));
                    } else {
                        values.put(prop.getName(), prop.getValue());
                    }
                    break;
                case "Edm.Int32":
                    if (null != prop.getValue()) {
                        values.put(prop.getName(), ((Integer)prop.getValue()).toString());
                    } else {
                        values.put(prop.getName(), prop.getValue());
                    }
                    break;
                case "Edm.Int64":
                    if (null != prop.getValue()) {
                        values.put(prop.getName(), ((Long)prop.getValue()).toString());
                    } else {
                        values.put(prop.getName(), prop.getValue());
                    }
                    break;
                case "Edm.Double":
                    if (null != prop.getValue()) {
                        values.put(prop.getName(), ((Double)prop.getValue()).toString());
                    } else {
                        values.put(prop.getName(), prop.getValue());
                    }
                    break;
                case "Edm.Boolean":
                    if (null != prop.getValue()) {
                        values.put(prop.getName(), ((Boolean)prop.getValue()).toString());
                    } else {
                        values.put(prop.getName(), prop.getValue());
                    }
                    break;
                default:
                    if (null != prop.getValue()) {
                        values.put(prop.getName(), (String)prop.getValue());
                    } else {
                        values.put(prop.getName(), prop.getValue());
                    }
            }
        }
        return values;
    }

    private HashMap<String, String[]> entityValues(List<UriParameter> keyParams) {
        HashMap values = new HashMap<>();
        for (UriParameter up : keyParams) {
            values.put(up.getName(), new String[]{up.getText()});
        }
        return values;
    }
    
    private Long getContentId(List<ContentDataOutput> genericPropListSet, long classcontentref) {
        for (ContentDataOutput cdo : genericPropListSet) {
            if (classcontentref == (long) cdo.getKeyval().get("id")) {
                return (long) cdo.getKeyval().get("cf_contentref");
            }
        }
        return -1L;
    }
}
