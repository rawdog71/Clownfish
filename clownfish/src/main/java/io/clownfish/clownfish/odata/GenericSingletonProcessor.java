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
import io.clownfish.clownfish.datamodels.SearchValues;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.jdbc.DatatableProperties;
import io.clownfish.clownfish.jdbc.JDBCUtil;
import io.clownfish.clownfish.jdbc.TableFieldStructure;
import static io.clownfish.clownfish.odata.GenericEntityCollectionProcessor.NAMESPACE_ENTITY;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.EncryptUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.persistence.NoResultException;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.core.uri.UriParameterImpl;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
/**
 *
 * @author raine
 */
@Component
public class GenericSingletonProcessor implements EntityProcessor {
    @Autowired private CfClassService cfclassservice;
    @Autowired private CfClasscontentService cfclasscontentService;
    @Autowired private CfAttributcontentService cfattributcontentservice;
    @Autowired private CfListService cflistservice;
    @Autowired private CfListcontentService cflistcontentservice;
    @Autowired ContentUtil contentUtil;
    @Autowired HibernateUtil hibernateUtil;
    @Autowired EntityUtil entityUtil;
    
    @Value("${hibernate.use:0}") int useHibernate;
    
    final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GenericSingletonProcessor.class);

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        OrderByOption orderbyoption = uriInfo.getOrderByOption();
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        
        String entityname;
        if (edmEntitySet.getName().endsWith("Set")) {
            entityname = edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-3);
        } else {
            entityname = edmEntitySet.getName();
        }
        EntityCollection entitySet = getData(edmEntitySet, uriResourceEntitySet.getKeyPredicates(), orderbyoption, entityUtil.getEntitysourcelist().get(new FullQualifiedName(NAMESPACE_ENTITY, entityname)));
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();
        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();

        final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
        EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with().id(id).contextURL(contextUrl).build();
        SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, entitySet, opts);
        InputStream serializedContent = serializerResult.getContent();

        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }
    
    private EntityCollection getData(EdmEntitySet edmEntitySet, List keypredicates, OrderByOption orderbyoption, SourceStructure source) {
        HashMap searchmap = new HashMap<>();
        HashMap attributmap = new HashMap<String, String[]>();
        for (Object entry : keypredicates) {
            String attributname = ((UriParameterImpl) entry).getName();
            String attributvalue = ((UriParameterImpl) entry).getText().replaceAll("^'", "")
                    .replaceAll("'$", "");
            searchmap.put(attributname+"_1", ":eq:" + attributvalue);
            String[] values = new String[]{attributvalue};
            attributmap.put(attributname, values);
        }
        String classname;
        if (edmEntitySet.getName().endsWith("Set")) {
            classname = edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-3);
        } else {
            if (edmEntitySet.getName().endsWith("Lists")) {
                classname = edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-5);
            } else {
                classname = edmEntitySet.getName();
            }
        }
        EntityCollection genericCollection = new EntityCollection();
        
        if (0 == source.getSource()) {
            if (0 != edmEntitySet.getEntityType().getName().compareToIgnoreCase("CFListEntry")) {
                getList(cfclassservice.findByName(classname), searchmap, genericCollection, orderbyoption);
            } else {
                getDataLists(classname, genericCollection, searchmap, orderbyoption);
            }
        } else {
            getListDB(classname, genericCollection, attributmap, source);
        }
        return genericCollection;
    }
    
    private void getList(CfClass clazz, HashMap searchmap, EntityCollection genericCollection, OrderByOption orderbyoption) {
        List<Entity> genericList = genericCollection.getEntities();
        if (0 == useHibernate) {
            List<CfClasscontent> classcontentList = cfclasscontentService.findByClassref(clazz);
            for (CfClasscontent cc : classcontentList) {
                if (!cc.isScrapped()) {
                    HashMap<String, String> attributmap = new HashMap<>();
                    List<CfAttributcontent> aclist = cfattributcontentservice.findByClasscontentref(cc);
                    List keyvals = contentUtil.getContentOutputKeyvalList(aclist);
                    Entity entity = new Entity();
                    // ToDo: fill out entity
                }
            }
        } else {
            Session session_tables = HibernateUtil.getClasssessions().get("tables").getSessionFactory().openSession();
            Query query = hibernateUtil.getQuery(session_tables, searchmap, clazz.getName(), getOrderbyMap(orderbyoption));
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
                            Entity entity = entityUtil.makeEntity(contentdataoutput);
                            genericList.add(entity);
                        }
                    }
                }
            } catch (NoResultException ex) {
                session_tables.close();
            }
        }
    }
    
    private void getDataLists(String classname, EntityCollection genericCollection, HashMap<String, String> searchmap, OrderByOption orderbyoption) {
        List<ContentDataOutput> genericPropListSet = new ArrayList<>();
        Session session_tables = HibernateUtil.getClasssessions().get("tables").getSessionFactory().openSession();
        Query query = hibernateUtil.getQuery(session_tables, null, classname, getOrderbyMap(orderbyoption));

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
                        genericPropListSet.add(contentdataoutput);
                    }
                }
            }
        } catch (NoResultException ex) {
            session_tables.close();
        }
        
        String searchcontentval = "";
        String searchvalue = "";
        
        for (String searchcontent : searchmap.keySet()) {
            searchcontentval = searchcontent.substring(0, searchcontent.length()-2);
            searchvalue = searchmap.get(searchcontent);
            
            SearchValues sv = getSearchValues(searchvalue);
            searchvalue = sv.getSearchvalue1();
        }
        
        List<Entity> genericList = genericCollection.getEntities();
        List<CfList> cflist = cflistservice.findByClassref(cfclassservice.findByName(classname));
        for (CfList list : cflist) {
            //System.out.println(list.getName());
            
            if (Objects.equals(list.getId(), Long.valueOf(searchvalue))) {
                Property prop_id = new Property();

                prop_id.setName("id");
                prop_id.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName().getFullQualifiedNameAsString());
                prop_id.setValue(ValueType.PRIMITIVE, list.getId());

                Property prop_name = new Property();
                prop_name.setName("name");
                prop_name.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString());
                prop_name.setValue(ValueType.PRIMITIVE, list.getName());

                List<CfListcontent> listcontent = cflistcontentservice.findByListref(list.getId());
                ArrayList<Long> listset = new ArrayList<>();
                for (CfListcontent entry : listcontent) {
                    listset.add(getContentId(genericPropListSet, entry.getCfListcontentPK().getClasscontentref()));
                }
                Property prop_listset = new Property();
                prop_listset.setName("listset");
                prop_listset.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName().getFullQualifiedNameAsString());
                prop_listset.setValue(ValueType.COLLECTION_PRIMITIVE, listset);

                Entity entity = new Entity();
                entity.addProperty(prop_id);
                entity.addProperty(prop_name);
                entity.addProperty(prop_listset);
                genericList.add(entity);
            }
        }
    }
    
    private void getListDB(String table, EntityCollection genericCollection, HashMap<String, String[]> searchmap, SourceStructure source) {
        table = table.substring(table.indexOf("_")+1);
        List<Entity> genericList = genericCollection.getEntities();
        JDBCUtil jdbcutil = new JDBCUtil(source.getClassname(), source.getUrl(), source.getUser(), source.getPassword());
        Connection con = jdbcutil.getConnection();
        try {
            if (null != con) {
                DatabaseMetaData dmd = con.getMetaData();
                DatatableProperties datatableproperties = new DatatableProperties();
                datatableproperties.setTablename(table);
                datatableproperties.setPagination(100000);
                datatableproperties.setPage(1);

                ResultSet resultSetTables = dmd.getTables(null, null, null, new String[]{"TABLE"});
                ArrayList<HashMap> resultlist = new ArrayList<>();
                while(resultSetTables.next())
                {
                    String tablename = resultSetTables.getString("TABLE_NAME");
                    if (0 == datatableproperties.getTablename().compareToIgnoreCase(tablename)) {
                        jdbcutil.manageTableRead(con, dmd, tablename, datatableproperties, searchmap, null, resultlist);
                        TableFieldStructure tfs = jdbcutil.getTableFieldsList(dmd, tablename, null, null);
                        for (HashMap hm : resultlist) {
                            Entity entity = entityUtil.makeEntity(tablename, hm, tfs);
                            genericList.add(entity);
                        }
                    }
                }
                con.close();
            }
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage());
        }
        finally {
            try {
                if (null != con) {
                    con.close();
                }
            }
            catch (SQLException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    @Override
    public void createEntity(ODataRequest odr, ODataResponse odr1, UriInfo uriInfo, ContentType ct, ContentType ct1) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();
        
        String entityname;
        if (edmEntitySet.getName().endsWith("Set")) {
            entityname = edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-3);
        } else {
            if (edmEntitySet.getName().endsWith("List")) {
                entityname = edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-4);
            } else {
                entityname = edmEntitySet.getName();
            }
        }
        InputStream requestInputStream = odr.getBody();
        ODataDeserializer deserializer = this.odata.createDeserializer(ct);
        DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
        Entity requestEntity = result.getEntity();
        Entity createdEntity = entityUtil.createEntity(edmEntitySet, requestEntity, edmEntityType, entityUtil.getEntitysourcelist().get(new FullQualifiedName(NAMESPACE_ENTITY, entityname)));

        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
        EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

        ODataSerializer serializer = this.odata.createSerializer(ct1);
        if (null != createdEntity) {
            SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity, options);

            odr1.setContent(serializedResponse.getContent());
            odr1.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
            odr1.setHeader(HttpHeader.CONTENT_TYPE, ct1.toContentTypeString());
        } else {
            odr1.setStatusCode(HttpStatusCode.NOT_ACCEPTABLE.getStatusCode());
        }
    }

    @Override
    public void updateEntity(ODataRequest odr, ODataResponse odr1, UriInfo uriInfo, ContentType ct, ContentType ct1) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();
        
        String entityname;
        if (edmEntitySet.getName().endsWith("Set")) {
            entityname = edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-3);
        } else {
            entityname = edmEntitySet.getName();
        }
        InputStream requestInputStream = odr.getBody();
        ODataDeserializer deserializer = this.odata.createDeserializer(ct);
        Entity requestEntity = null;
        try {
            DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
            requestEntity = result.getEntity();
        } catch (DeserializerException ex) {
            LOGGER.error(ex.getMessage());
        }
        
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        HttpMethod httpMethod = odr.getMethod();
        boolean modified = entityUtil.updateEntity(edmEntitySet, keyPredicates, requestEntity, httpMethod, entityUtil.getEntitysourcelist().get(new FullQualifiedName(NAMESPACE_ENTITY, entityname)));
        if (modified) {
            odr1.setStatusCode(HttpStatusCode.OK.getStatusCode());
        } else {
            odr1.setStatusCode(HttpStatusCode.NOT_MODIFIED.getStatusCode());
        }
    }

    @Override
    public void deleteEntity(ODataRequest odr, ODataResponse odr1, UriInfo ui) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourcePaths = ui.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        
        String entityname;
        if (edmEntitySet.getName().endsWith("Set")) {
            entityname = edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-3);
        } else {
            if (edmEntitySet.getName().endsWith("List")) {
                entityname = edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-4);
            } else {
                entityname = edmEntitySet.getName();
            }
        }

        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        boolean deleted = entityUtil.deleteEntity(edmEntitySet, keyPredicates, entityUtil.getEntitysourcelist().get(new FullQualifiedName(NAMESPACE_ENTITY, entityname)));
        if (deleted) {
            odr1.setStatusCode(HttpStatusCode.OK.getStatusCode());
        } else {
            odr1.setStatusCode(HttpStatusCode.NOT_MODIFIED.getStatusCode());
        }
    }

    @Override
    public void init(OData odata, ServiceMetadata sm) {
        this.odata = odata;
        this.serviceMetadata = sm;
    }
    
    private HashMap<String, String> getOrderbyMap(OrderByOption orderbyoption) {
        if (null != orderbyoption) {
            HashMap<String, String> orderbymap = new HashMap<>();
            for (OrderByItem obi : orderbyoption.getOrders()) {
                orderbymap.put(obi.getExpression().toString().replace("[", "").replace("]", ""), getBoolean(obi.isDescending()));
            }
            return orderbymap;
        } else {
            return null;
        }
    }
    
    private String getBoolean(boolean descending) {
        if (descending) {
            return "DESC";
        } else {
            return "ASC";
        }
    }
    
    private Long getContentId(List<ContentDataOutput> genericPropListSet, long classcontentref) {
        for (ContentDataOutput cdo : genericPropListSet) {
            if (classcontentref == (long) cdo.getKeyval().get("cf_contentref")) {
                return (long) cdo.getKeyval().get("id");
            }
        }
        return -1L;
    }
    
    private SearchValues getSearchValues(String searchvalue) {
        String comparator = "eq";
        String searchvalue1 = "";
        String searchvalue2 = "";
        if (searchvalue.startsWith(":")) {
            String[] values = searchvalue.split(":");
            if (values.length < 2) {
                searchvalue1 = values[0];
            } else {
                comparator = values[1];
                searchvalue1 = values[2];
                searchvalue2 = "";
                if (values.length > 3) {
                    searchvalue2 = values[3];
                }
            }
            return new SearchValues(comparator, searchvalue1.toLowerCase(), searchvalue2.toLowerCase());
        } else {
            searchvalue1 = searchvalue;
            return new SearchValues(comparator, searchvalue1.toLowerCase(), searchvalue2.toLowerCase());
        }
        
    }
}
