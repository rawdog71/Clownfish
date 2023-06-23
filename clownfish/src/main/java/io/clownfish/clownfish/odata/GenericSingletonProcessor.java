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
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfContentversionService;
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.NoResultException;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.core.uri.UriParameterImpl;
import org.hibernate.Session;
import org.hibernate.query.Query;
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
    @Autowired private CfContentversionService cfcontentversionservice;
    @Autowired ContentUtil contentUtil;
    @Autowired HibernateUtil hibernateUtil;
    @Autowired EntityUtil entityUtil;
    
    @Value("${hibernate.use:0}") int useHibernate;

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        TopOption topoption = uriInfo.getTopOption();
        SkipOption skipoption = uriInfo.getSkipOption();
        SelectOption selectoption = uriInfo.getSelectOption();
        OrderByOption orderbyoption = uriInfo.getOrderByOption();
        //UriResourceSingleton uriResourceSingleton = (UriResourceSingleton) resourcePaths.get(0);
        //System.out.println(uriResourceSingleton.getEntityType().getName());
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        System.out.println(uriResourceEntitySet.getKeyPredicates());

        EntityCollection entitySet = getData(edmEntitySet, uriResourceEntitySet.getKeyPredicates(), orderbyoption);
        
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
    
    private EntityCollection getData(EdmEntitySet edmEntitySet, List keypredicates, OrderByOption orderbyoption) {
        HashMap searchmap = new HashMap<>();
        for (Object entry : keypredicates) {
            String attributname = ((UriParameterImpl) entry).getName();
            String attributvalue = ((UriParameterImpl) entry).getText().replaceAll("^'", "")
                    .replaceAll("'$", "");
            searchmap.put(attributname+"_1", ":eq:" + attributvalue);
        }
        String classname = "";
        if (edmEntitySet.getName().endsWith("Set")) {
            classname = edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-3);
        } else {
            classname = edmEntitySet.getName();
        }
        EntityCollection genericCollection = new EntityCollection();
        getList(cfclassservice.findByName(classname), searchmap, genericCollection, orderbyoption);
       
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

    @Override
    public void createEntity(ODataRequest odr, ODataResponse odr1, UriInfo ui, ContentType ct, ContentType ct1) throws ODataApplicationException, ODataLibraryException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void updateEntity(ODataRequest odr, ODataResponse odr1, UriInfo ui, ContentType ct, ContentType ct1) throws ODataApplicationException, ODataLibraryException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void deleteEntity(ODataRequest odr, ODataResponse odr1, UriInfo ui) throws ODataApplicationException, ODataLibraryException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
}
