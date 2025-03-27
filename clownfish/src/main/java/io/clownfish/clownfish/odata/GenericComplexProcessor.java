package io.clownfish.clownfish.odata;

import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.ComplexProcessor;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.*;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.core.uri.UriResourceComplexPropertyImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

import static io.clownfish.clownfish.odata.GenericEntityCollectionProcessor.NAMESPACE_ENTITY;

@Component
public class GenericComplexProcessor implements ComplexProcessor {
    @Autowired OdataUtil odatautil;
    @Autowired private CfAttributcontentService cfattributcontentService;
    @Autowired private CfClasscontentService cfclasscontentService;
    @Autowired
    HibernateUtil hibernateUtil;
    @Autowired EntityUtil entityUtil;
    @Autowired private ContentUtil contentUtil;
    final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GenericComplexProcessor.class);

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void readComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        UriResourceComplexProperty property = (UriResourceComplexProperty) resourcePaths.get(1);
        OrderByOption orderbyoption = uriInfo.getOrderByOption();
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        String entityname;
        if (edmEntitySet.getName().endsWith("Set")) {
            entityname = edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-3);
        } else {
            entityname = edmEntitySet.getName();
        }
        EntityCollection entitySet = odatautil.getData(edmEntitySet, uriResourceEntitySet.getKeyPredicates(), orderbyoption, entityUtil.getEntitysourcelist().get(new FullQualifiedName(NAMESPACE_ENTITY, entityname)));
        Property prop = entitySet.getEntities().get(0).getProperty(property.getProperty().getName());

        if(null != prop) {
            ODataSerializer serializer = odata.createSerializer(responseFormat);
            EdmComplexType edmComplexType = (EdmComplexType) property.getProperty().getType();
            ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();

            ComplexSerializerOptions opts = ComplexSerializerOptions.with().contextURL(contextUrl).build();
            SerializerResult serializerResult = serializer.complex(serviceMetadata, edmComplexType, prop, opts);
            InputStream serializedContent = serializerResult.getContent();

            response.setContent(serializedContent);
            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        } else {
            response.setContent(null);
            response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        }
    }

    @Override
    public void updateComplex(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        UriResourceComplexPropertyImpl uriResourceComplexProperty = (UriResourceComplexPropertyImpl) resourcePaths.get(1);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        String entityname;
        String propertyName = uriResourceComplexProperty.getProperty().getName();
        if (edmEntitySet.getName().endsWith("Set")) {
            entityname = edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-3);
        } else {
            if (edmEntitySet.getName().endsWith("List")) {
                entityname = edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-4);
            } else {
                entityname = edmEntitySet.getName();
            }
        }

        InputStream requestInputStream = oDataRequest.getBody();
        ODataDeserializer deserializer = this.odata.createDeserializer(contentType);
        Entity requestEntity = null;
        try {
            DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
            requestEntity = result.getEntity();
        } catch (DeserializerException ex) {
            LOGGER.error(ex.getMessage());
        }

        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        CfClasscontent cfclasscontent = null;
        List<CfAttributcontent> attributContentList = null;

        try {
            for (UriParameter param : keyPredicates) {
                if (0 == param.getName().compareToIgnoreCase("id")) {
                    cfclasscontent = cfclasscontentService.findById(hibernateUtil.getContentRef(entityname, "id", Long.parseLong(param.getText())));
                    attributContentList = cfattributcontentService.findByClasscontentref(cfclasscontent);
                }
            }

            assert attributContentList != null;
            for (CfAttributcontent attributcontent : attributContentList) {
                if (!attributcontent.getAttributref().getIdentity()) {
                    CfAttribut attribut = attributcontent.getAttributref();
                    if (attribut.getExt_mutable()) {
                        if(attribut.getAttributetype().getName().compareToIgnoreCase("classref") == 0 && attribut.getName().equals(propertyName)) {
                            long contentRefId = hibernateUtil.getContentRef(attribut.getRelationref().getName(),"id", new Long(String.valueOf(requestEntity.getProperty("id").getValue())));
                            contentUtil.setAttributValue(attributcontent, String.valueOf(contentRefId));
                            cfattributcontentService.edit(attributcontent);
                            contentUtil.indexContent();
                        }
                    }
                }
            }
            hibernateUtil.updateContent(cfclasscontent);
            contentUtil.commit(cfclasscontent);
            oDataResponse.setStatusCode(HttpStatusCode.OK.getStatusCode());
        } catch (Error e) {
            oDataResponse.setStatusCode(HttpStatusCode.NOT_MODIFIED.getStatusCode());
        }
    }

    @Override
    public void deleteComplex(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        UriResourceComplexPropertyImpl uriResourceComplexProperty = (UriResourceComplexPropertyImpl) resourcePaths.get(1);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        String entityname;
        String propertyName = uriResourceComplexProperty.getProperty().getName();
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
        CfClasscontent cfclasscontent = null;
        List<CfAttributcontent> attributContentList = null;

        try {
            for (UriParameter param : keyPredicates) {
                if (0 == param.getName().compareToIgnoreCase("id")) {
                    cfclasscontent = cfclasscontentService.findById(hibernateUtil.getContentRef(entityname, "id", Long.parseLong(param.getText())));
                    attributContentList = cfattributcontentService.findByClasscontentref(cfclasscontent);
                }
            }

            assert attributContentList != null;
            for (CfAttributcontent attributcontent : attributContentList) {
                if (!attributcontent.getAttributref().getIdentity()) {
                    CfAttribut attribut = attributcontent.getAttributref();
                    if (attribut.getExt_mutable()) {
                        if(attribut.getName().equals(propertyName)) {
                            contentUtil.setAttributValue(attributcontent, null);
                            cfattributcontentService.edit(attributcontent);
                            contentUtil.indexContent();
                        }
                    }
                }
            }

            hibernateUtil.updateContent(cfclasscontent);
            contentUtil.commit(cfclasscontent);
            oDataResponse.setStatusCode(HttpStatusCode.OK.getStatusCode());
        } catch (Error e) {
            oDataResponse.setStatusCode(HttpStatusCode.NOT_MODIFIED.getStatusCode());
        }
    }

    @Override
    public void init(OData odata, ServiceMetadata sm) {
        this.odata = odata;
        this.serviceMetadata = sm;
    }
}
