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

import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.commons.api.ex.ODataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author raine
 */
@Component
public class GenericEdmProvider extends CsdlAbstractEdmProvider {
    public static final String NAMESPACE_ENTITY = "OData.Entity";
    public static final String NAMESPACE_COMPLEX = "OData.Complex";
    public static final String CONTAINER_NAME = "Container";
    public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE_ENTITY, CONTAINER_NAME);

    @Autowired private CfClassService cfclassservice;
    @Autowired private CfAttributService cfattributservice;
    
    private static final HashMap<String, CsdlSingleton> singletonlist = new HashMap<>();
    private static final HashMap<String, CsdlEntitySet> entitysetlist = new HashMap<>();
    private static final HashMap<FullQualifiedName, CsdlEntityType> entitytypelist = new HashMap<>();
    private static final HashMap<FullQualifiedName, CsdlComplexType> complextypelist = new HashMap<>();
    private static CsdlEntityContainer entityContainer = null;
    private static List<CsdlSchema> schemas = null;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericEdmProvider.class);
    
    public void init() {
        singletonlist.clear();
        entitysetlist.clear();
        entitytypelist.clear();
        complextypelist.clear();
        entityContainer = null;
        schemas = null;
    }

    @Override
    public List<CsdlSchema> getSchemas() throws ODataException {
        if (null == schemas) {
            CsdlSchema entity_schema = new CsdlSchema();
            entity_schema.setNamespace(NAMESPACE_ENTITY);
            // add EntityTypes
            List<CsdlEntityType> entityTypes = new ArrayList<>();
            List<CsdlComplexType> complexTypes = new ArrayList<>();
            for (CfClass clazz : cfclassservice.findAll()) {
                CsdlEntityType et = getEntityType(new FullQualifiedName(NAMESPACE_ENTITY, clazz.getName()));
                CsdlComplexType ct = getComplexType(new FullQualifiedName(NAMESPACE_COMPLEX, clazz.getName()));
                if (null != et) {
                    entityTypes.add(et);
                }
                if (ct != null) {
                    complexTypes.add(ct);
                }
            }
            entity_schema.setEntityTypes(entityTypes);
            entity_schema.setComplexTypes(complexTypes);
            // add EntityContainer
            entity_schema.setEntityContainer(getEntityContainer());
            schemas = new ArrayList<>();
            schemas.add(entity_schema);

            return schemas;
        } else {
            return schemas;
        }
    }

    @Override
    public CsdlEntityContainer getEntityContainer() throws ODataException {
        if (null == entityContainer) {
            // create EntitySets
            List<CsdlEntitySet> entitySets = new ArrayList<>();
            List<CsdlSingleton> singletons = new ArrayList<>();
            for (CfClass clazz : cfclassservice.findAll()) {
                if (!getKeys(clazz).isEmpty()) {
                    CsdlSingleton si = getSingleton(CONTAINER, clazz.getName());
                    CsdlEntitySet es = getEntitySet(CONTAINER, clazz.getName()+"Set");
                    if (null != es) {
                        singletons.add(si);
                        entitySets.add(es);
                    }
                }
            }
            // create EntityContainer
            entityContainer = new CsdlEntityContainer();
            entityContainer.setName(CONTAINER_NAME);
            entityContainer.setSingletons(singletons);
            entityContainer.setEntitySets(entitySets);

            return entityContainer;
        } else {
            return entityContainer;
        }
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
        if (entitytypelist.containsKey(entityTypeName)) {
            return entitytypelist.get(entityTypeName);
        } else {
            System.out.println(entityTypeName.getFullQualifiedNameAsString());
            CfClass classref = cfclassservice.findByName(entityTypeName.getName());
            List propsList = new ArrayList();
            List keysList = new ArrayList();
            for (CfAttribut attribut : cfattributservice.findByClassref(classref)) {
                CsdlProperty prop = new CsdlProperty().setName(attribut.getName()).setType(getODataType(attribut)).setCollection(getODataCollection(attribut));
                propsList.add(prop);
                if (attribut.getIdentity()) {
                    CsdlPropertyRef propertyRef = new CsdlPropertyRef();
                    propertyRef.setName(attribut.getName());
                    keysList.add(propertyRef);
                }
            }
            CsdlEntityType entityType = new CsdlEntityType();
            entityType.setName(entityTypeName.getName());
            entityType.setProperties(propsList);
            entityType.setKey(keysList);

            if (!keysList.isEmpty()) {
                entitytypelist.put(entityTypeName, entityType);
                return entityType;
            } else {
                LOGGER.warn("OData - Missing identifier for " + entityTypeName.getName());
                return null;
            }
        }
    }

    @Override
    public CsdlComplexType getComplexType(FullQualifiedName complexTypeName) {
        if (complexTypeName.getFullQualifiedNameAsString().startsWith("Edm")) {
            return null;
        }
        //System.out.println(complexTypeName.getFullQualifiedNameAsString());
        if (complextypelist.containsKey(complexTypeName)) {
            return complextypelist.get(complexTypeName);
        } else {
            CfClass classref = cfclassservice.findByName(complexTypeName.getName());
            List propsList = new ArrayList();
            List keysList = new ArrayList();
            for (CfAttribut attribut : cfattributservice.findByClassref(classref)) {
                CsdlProperty prop = new CsdlProperty().setName(attribut.getName()).setType(getODataType(attribut)).setCollection(getODataCollection(attribut));
                propsList.add(prop);
                if (attribut.getIdentity()) {
                    CsdlPropertyRef propertyRef = new CsdlPropertyRef();
                    propertyRef.setName(attribut.getName());
                    keysList.add(propertyRef);
                }
            }
            CsdlComplexType complexType = new CsdlComplexType();
            complexType.setName(complexTypeName.getName());
            complexType.setProperties(propsList);

            if (!keysList.isEmpty()) {
                complextypelist.put(complexTypeName, complexType);
                return complexType;
            } else {
                LOGGER.warn("OData - Missing identifier for " + complexTypeName.getName());
                return null;
            }
        }
    }

    @Override
    public CsdlSingleton getSingleton(FullQualifiedName entityContainer, String singletonName) throws ODataException {
        if (entityContainer.equals(CONTAINER)) {
            if (singletonlist.containsKey(singletonName)) {
                return singletonlist.get(singletonName);
            } else {
                CsdlSingleton singleton = new CsdlSingleton();
                singleton.setName(singletonName);
                singleton.setType(new FullQualifiedName(NAMESPACE_ENTITY, singletonName));
                //singleton.setNavigationPropertyBindings(navigationPropertyBindings);
                singletonlist.put(singletonName, singleton);
                return singleton;
            }
        }
        return null;
    }
    
    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
        if (entityContainer.equals(CONTAINER)) {
            if (entitysetlist.containsKey(entitySetName)) {
                return entitysetlist.get(entitySetName);
            } else {
                CsdlEntitySet entitySet = new CsdlEntitySet();
                entitySet.setName(entitySetName);
                entitySet.setType(new FullQualifiedName(NAMESPACE_ENTITY, entitySetName.substring(0, entitySetName.length()-3)));
                entitysetlist.put(entitySetName, entitySet);
                return entitySet;
            }
        }

        return null;
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) throws ODataException {
        if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
            CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
            entityContainerInfo.setContainerName(CONTAINER);
            return entityContainerInfo;
        }
        return null;
    }
    
    public static FullQualifiedName getODataType(CfAttribut attribut) {
        switch (attribut.getAttributetype().getName()) {
            case "string":
            case "hashstring":
            case "htmltext":
            case "markdown":
            case "text":
                return EdmPrimitiveTypeKind.String.getFullQualifiedName();
            case "datetime":
                return EdmPrimitiveTypeKind.Date.getFullQualifiedName();
            case "integer":
            case "media":
            case "assetref":
                return EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
            case "real":
                return EdmPrimitiveTypeKind.Double.getFullQualifiedName();
            case "boolean":
                return EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
            case "classref":
                if (0 == attribut.getRelationtype()) {                          // n:m
                    return new FullQualifiedName(NAMESPACE_COMPLEX, attribut.getRelationref().getName());
                } else {                                                        // 1:n
                    return new FullQualifiedName(NAMESPACE_COMPLEX, attribut.getRelationref().getName());
                }
            default:
                return null;
        }
    }
    
    private List getKeys(CfClass classref) {
        List keysList = new ArrayList();
        for (CfAttribut attribut : cfattributservice.findByClassref(classref)) {
            if (attribut.getIdentity()) {
                CsdlPropertyRef propertyRef = new CsdlPropertyRef();
                propertyRef.setName(attribut.getName());
                keysList.add(propertyRef);
            }
        }
        return keysList;
    }

    private boolean getODataCollection(CfAttribut attribut) {
        return (0 == attribut.getAttributetype().getName().compareToIgnoreCase("classref")) && (0 == attribut.getRelationtype());
    }
}
