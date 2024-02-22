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
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAssetlistcontent;
import io.clownfish.clownfish.dbentities.CfAssetlistcontentPK;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.dbentities.CfListcontentPK;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.jdbc.DatatableProperties;
import io.clownfish.clownfish.jdbc.JDBCUtil;
import io.clownfish.clownfish.jdbc.TableField;
import io.clownfish.clownfish.jdbc.TableFieldStructure;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistcontentService;
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
import javax.persistence.NoResultException;
import lombok.Getter;
import lombok.Setter;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.uri.UriParameter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

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
    @Autowired private CfAssetlistService cfassetlistService;
    @Autowired private CfAssetlistcontentService cfassetlistcontentService;
    @Autowired private ContentUtil contentUtil;
    @Autowired HibernateUtil hibernateUtil;
    
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
                    if (0 == ((String) value).compareToIgnoreCase("1")) {
                        value = "true";
                    } else {
                        value = "false";
                    }
                    prop.setValue(ValueType.PRIMITIVE, Boolean.parseBoolean((String)value));
                } else {
                    prop.setValue(ValueType.PRIMITIVE, ((Boolean)value));
                }    
                break;
            case "Edm.Date":
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
    
    public Entity createEntity(EdmEntitySet edmEntitySet, Entity requestEntity, SourceStructure source) {
        Entity entity = requestEntity;
        if (0 == source.getSource()) {
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

                        if (0 != attribut.getAttributetype().getName().compareToIgnoreCase("datetime")) {
                            newcontent = contentUtil.setAttributValue(newcontent, entity.getProperty(attribut.getName()).getValue().toString());
                        } else {
                            newcontent = contentUtil.setAttributValue(newcontent, ((GregorianCalendar)entity.getProperty(attribut.getName()).getValue()).toZonedDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                        }
                        cfattributcontentService.create(newcontent);
                        contentUtil.indexContent();
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
                }
            });
            hibernateUtil.updateContent(newclasscontent);

            try {
                entity.setId(new URI(String.valueOf(newmax)));
            } catch (URISyntaxException ex) {
                LOGGER.error(ex.getMessage());
            }
            return entity;
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
                }
                hibernateUtil.updateContent(cfclasscontent);
                return true;
            } else {
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
                case "Edm.Date":
                    if (null != prop.getValue()) {
                        values.put(prop.getName(), ((GregorianCalendar)prop.getValue()).toZonedDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
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
}
