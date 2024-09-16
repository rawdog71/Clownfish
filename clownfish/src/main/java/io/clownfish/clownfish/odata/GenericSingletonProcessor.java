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


import static io.clownfish.clownfish.odata.GenericEntityCollectionProcessor.NAMESPACE_ENTITY;
import java.io.InputStream;
import java.util.List;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
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
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 *
 * @author raine
 */
@Component
public class GenericSingletonProcessor implements EntityProcessor {
    @Autowired OdataUtil odatautil;
    @Autowired EntityUtil entityUtil;
    
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
        EntityCollection entitySet = odatautil.getData(edmEntitySet, uriResourceEntitySet.getKeyPredicates(), orderbyoption, entityUtil.getEntitysourcelist().get(new FullQualifiedName(NAMESPACE_ENTITY, entityname)));
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
}
