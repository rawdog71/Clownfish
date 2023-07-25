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
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
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
public class GenericEntityCollectionProcessor implements EntityCollectionProcessor {
    @Autowired private CfClassService cfclassservice;
    @Autowired private CfClasscontentService cfclasscontentService;
    @Autowired private CfAttributcontentService cfattributcontentservice;
    @Autowired ContentUtil contentUtil;
    @Autowired HibernateUtil hibernateUtil;
    @Autowired EntityUtil entityUtil;
    
    @Value("${hibernate.use:0}") int useHibernate;

    private OData odata;
    private ServiceMetadata serviceMetadata;
    
    @Override
    public void init(OData odata, ServiceMetadata sm) {
        this.odata = odata;
        this.serviceMetadata = sm;
    }

    /**
     *
     * @param request
     * @param response
     * @param uriInfo
     * @param responseFormat
     * @throws ODataApplicationException
     * @throws ODataLibraryException
     */
    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        CountOption countOption = uriInfo.getCountOption();
        TopOption topOption = uriInfo.getTopOption();
        SkipOption skipOption = uriInfo.getSkipOption();
        SelectOption selectoption = uriInfo.getSelectOption();
        OrderByOption orderbyoption = uriInfo.getOrderByOption();

        FilterOption filterOption = uriInfo.getFilterOption();

        EntityCollection entitySet = getData(edmEntitySet, null, orderbyoption);
        EntityCollection returnCollection = new EntityCollection();

        List<Entity> entityList = entitySet.getEntities();
        Iterator<Entity> entityIterator = entityList.iterator();

        // handle $count
        if (countOption != null) {
            if (countOption.getValue()) {
                returnCollection.setCount(entityList.size());
            }
        }

        // handle $skip
        if (skipOption != null) {
            int skipNumber = skipOption.getValue();
            if (skipNumber >= 0) {
                if (skipNumber <= entityList.size()) {
                    entityList = entityList.subList(skipNumber, entityList.size());
                } else {
                    // The client skipped all entities
                    entityList.clear();
                }
            } else {
                throw new ODataApplicationException("Invalid value for $skip", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
            }
        }

        // handle $top
        if (topOption != null) {
            int topNumber = topOption.getValue();
            if (topNumber >= 0) {
                if (topNumber <= entityList.size()) {
                    entityList = entityList.subList(0, topNumber);
                }  // else the client has requested more entities than available, just return what we have
            } else {
                throw new ODataApplicationException("Invalid value for $top", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
            }
        }

        // handle $filter
        if (filterOption != null) {
            // Apply $filter system query option
            try {
                // Evaluate the expression for each entity
                // If the expression is evaluated to "true", keep the entity, otherwise remove it from the entityList
                while (entityIterator.hasNext()) {
                    // To evaluate the expression, create an instance of the Filter Expression Visitor and pass
                    // the current entity to the constructor
                    Entity currentEntity = entityIterator.next();
                    Expression filterExpression = filterOption.getExpression();
                    GenericFilterExpressionVisitor expressionVisitor = new GenericFilterExpressionVisitor(currentEntity);

                    // Start evaluating the expression
                    Object visitorResult = filterExpression.accept(expressionVisitor);

                    // The result of the filter expression must be of type Edm.Boolean
                    if (visitorResult instanceof Boolean) {
                        if (!Boolean.TRUE.equals(visitorResult)) {
                            // The expression evaluated to false (or null), so we have to remove the currentEntity from entityList
                            entityIterator.remove();
                        }
                    } else {
                        throw new ODataApplicationException("A filter expression must evaulate to type Edm.Boolean",
                                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
                    }
                }

            } catch (ExpressionVisitException e) {
                throw new ODataApplicationException("Exception in filter evaluation",
                        HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
            }
        }

        for (Entity entity : entityList) {
            returnCollection.getEntities().add(entity);
        }
        
        ODataSerializer serializer = odata.createSerializer(responseFormat);

        EdmEntityType edmEntityType = edmEntitySet.getEntityType();
        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();

        final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
        EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with().id(id)
                .contextURL(contextUrl)
                .select(selectoption)
                .count(countOption)
                .build();
        SerializerResult serializerResult = serializer
                .entityCollection(serviceMetadata, edmEntityType, returnCollection, opts);
        InputStream serializedContent = serializerResult.getContent();

        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    private EntityCollection getData(EdmEntitySet edmEntitySet, Expression filterExpression, OrderByOption orderbyoption) {
        String classname = "";
        if (edmEntitySet.getName().endsWith("Set")) {
            classname = edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-3);
        } else {
            classname = edmEntitySet.getName();
        }
        HashMap searchMap = getSearchMap(filterExpression);
        EntityCollection genericCollection = new EntityCollection();
        getList(cfclassservice.findByName(classname), genericCollection, searchMap, orderbyoption);
       
       return genericCollection;
    }
    
    private void getList(CfClass clazz, EntityCollection genericCollection, HashMap searchmap, OrderByOption orderbyoption) {
        List<Entity> genericList = genericCollection.getEntities();
        if (0 == useHibernate) {
            List<CfClasscontent> classcontentList = cfclasscontentService.findByClassref(clazz);
            for (CfClasscontent cc : classcontentList) {
                if (!cc.isScrapped()) {
                    HashMap<String, String> attributmap = new HashMap<>();
                    List<CfAttributcontent> aclist = cfattributcontentservice.findByClasscontentref(cc);
                    List keyvals = contentUtil.getContentOutputKeyvalList(aclist);
                    Entity entity = new Entity();
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
    
    private HashMap getSearchMap(Expression filterExpression) {
        HashMap searchmap = new HashMap<>();
        // https://olingo.apache.org/doc/odata2/tutorials/Olingo_Tutorial_AdvancedRead_FilterVisitor.html


        return searchmap;
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
