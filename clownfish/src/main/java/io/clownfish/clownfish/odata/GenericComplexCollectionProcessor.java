package io.clownfish.clownfish.odata;

import org.apache.olingo.commons.api.data.*;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.ComplexCollectionProcessor;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.*;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static io.clownfish.clownfish.odata.GenericEntityCollectionProcessor.NAMESPACE_ENTITY;
import static org.apache.olingo.commons.api.data.ValueType.COLLECTION_COMPLEX;

@Component
public class GenericComplexCollectionProcessor implements ComplexCollectionProcessor {
    @Autowired OdataUtil odatautil;
    @Autowired EntityUtil entityUtil;
    final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GenericComplexCollectionProcessor.class);

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void readComplexCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        UriResourceComplexProperty property = (UriResourceComplexProperty) resourcePaths.get(1);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        OrderByOption orderbyoption = uriInfo.getOrderByOption();
        FilterOption filterOption = uriInfo.getFilterOption();

        String entityname;
        if (edmEntitySet.getName().endsWith("Set")) {
            entityname = edmEntitySet.getName().substring(0, edmEntitySet.getName().length()-3);
        } else {
            entityname = edmEntitySet.getName();
        }
        EntityCollection entitySet = odatautil.getData(edmEntitySet, uriResourceEntitySet.getKeyPredicates(), orderbyoption, entityUtil.getEntitysourcelist().get(new FullQualifiedName(NAMESPACE_ENTITY, entityname)));
        Property prop = entitySet.getEntities().get(0).getProperty(property.getProperty().getName());

        if(null != prop) {

            // handle $filter
            ArrayList propList = (ArrayList) prop.getValue();
            Iterator<ComplexValue> entityIterator = propList.iterator();

            if (filterOption != null) {
                // Apply $filter system query option
                try {
                    // Evaluate the expression for each entity
                    // If the expression is evaluated to "true", keep the entity, otherwise remove it from the entityList
                    while (entityIterator.hasNext()) {
                        // To evaluate the expression, create an instance of the Filter Expression Visitor and pass
                        // the current entity to the constructor
                        ComplexValue currentProp = entityIterator.next();
                        Entity currentEntity = entityUtil.createEntity(currentProp);
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

            prop.setValue(COLLECTION_COMPLEX, propList);

            ODataSerializer serializer = odata.createSerializer(responseFormat);
            EdmComplexType edmComplexType = (EdmComplexType) property.getProperty().getType();
            ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();

            ComplexSerializerOptions opts = ComplexSerializerOptions.with().contextURL(contextUrl).build();
            SerializerResult serializerResult = serializer.complexCollection(serviceMetadata, edmComplexType, prop, opts);
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
    public void updateComplexCollection(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void deleteComplexCollection(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void init(OData odata, ServiceMetadata sm) {
        this.odata = odata;
        this.serviceMetadata = sm;
    }
}
