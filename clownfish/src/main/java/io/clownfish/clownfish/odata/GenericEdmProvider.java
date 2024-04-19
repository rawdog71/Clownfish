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

import io.clownfish.clownfish.datamodels.ColumnData;
import io.clownfish.clownfish.datamodels.TableData;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfDatasource;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.jdbc.JDBCUtil;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import lombok.Getter;
import lombok.Setter;
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
public class GenericEdmProvider extends CsdlAbstractEdmProvider implements Runnable {
    public static final String NAMESPACE_ENTITY = "OData.Entity";
    public static final String NAMESPACE_COMPLEX = "OData.Complex";
    public static final String CONTAINER_NAME = "Container";
    public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE_ENTITY, CONTAINER_NAME);

    @Autowired private CfClassService cfclassservice;
    @Autowired private CfAttributService cfattributservice;
    @Autowired transient CfDatasourceService cfdatasourceService;
    @Autowired private CfListService cflistservice;
    @Autowired EntityUtil entityUtil;
    
    private static CsdlEntityContainer entityContainer = null;
    private List<CsdlSchema> schemas = null;
    private @Getter @Setter List<CsdlSchema> schemas1 = null;
    private @Getter @Setter List<CsdlSchema> schemas2 = null;
    private int schemabuffer = 1;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericEdmProvider.class);
    
    public void init() {
        entityUtil.getSingletonlist().clear();
        entityUtil.getEntitysetlist().clear();
        entityUtil.getEntitytypelist().clear();
        entityUtil.getComplextypelist().clear();
        entityUtil.getEntitysourcelist().clear();
        entityUtil.getEntitystructurelist().clear();
        entityUtil.init(this);
        entityContainer = null;
    }
    
    @Override
    public List<CsdlSchema> getSchemas() throws ODataException {
        return schemas;
    }

    public void getNewSchemas() throws ODataException {
        if (1 == schemabuffer) {
            if (null == schemas1) {
                schemas1 = new ArrayList<CsdlSchema>();
            }
            schemas1.clear();
            schemas1.add(generateSchemas());
            schemas = schemas1;
            schemabuffer = 2;
        } else {
            if (null == schemas2) {
                schemas2 = new ArrayList<CsdlSchema>();
            }
            schemas2.clear();
            schemas2.add(generateSchemas());
            schemas = schemas2;
            schemabuffer = 1;
        }
    }
    
    private CsdlSchema generateSchemas() throws ODataException {
        LOGGER.info("START GENERATE ODATA SCHEMA");
        CsdlSchema entity_schema = new CsdlSchema();
        entity_schema.setNamespace(NAMESPACE_ENTITY);
        // add EntityTypes
        List<CsdlEntityType> entityTypes = new ArrayList<>();
        List<CsdlComplexType> complexTypes = new ArrayList<>();

        entityUtil.getEntitysourcelist().put(new FullQualifiedName(NAMESPACE_ENTITY, "CFListEntry"), new SourceStructure(0, 3, null, null, null, null, null));
        CsdlEntityType listentryet = getEntityType(new FullQualifiedName(NAMESPACE_ENTITY, "CFListEntry"));
        entityTypes.add(listentryet);

        for (CfClass clazz : cfclassservice.findAll()) {
            entityUtil.getEntitysourcelist().put(new FullQualifiedName(NAMESPACE_ENTITY, clazz.getName()), new SourceStructure(0, 0, null, null, null, null, null));
            CsdlEntityType et = getEntityType(new FullQualifiedName(NAMESPACE_ENTITY, clazz.getName()));
            CsdlComplexType ct = getComplexType(new FullQualifiedName(NAMESPACE_COMPLEX, clazz.getName()));
            if (null != et) {
                entityTypes.add(et);
            }
            if (ct != null) {
                complexTypes.add(ct);
            }
        }

        for (CfClass clazz : cfclassservice.findByMaintenance(true)) {
            if (!getKeys(clazz).isEmpty()) {
                CsdlEntitySet es = getEntitySet(CONTAINER, clazz.getName()+"Lists");
                if (null != es) {
                    entityUtil.getEntitysourcelist().put(new FullQualifiedName(NAMESPACE_ENTITY, clazz.getName()+"Lists"), new SourceStructure(0, 2, null, null, null, null, null));
                }
            }
        }

        for (CfList list : cflistservice.findByMaintenance(true)) {
            CfClass clazz = list.getClassref();
            if (!getKeys(clazz).isEmpty()) {
                CsdlEntitySet es = getEntitySet(CONTAINER, list.getName()+"List");
                if ((null != es) && (list.getClassref().isMaintenance())) {
                    entityUtil.getEntitysourcelist().put(new FullQualifiedName(NAMESPACE_ENTITY, list.getName()), new SourceStructure(0, 1, null, null, null, null, null));
                }
            }
        }

        for (CfDatasource datasource : cfdatasourceService.findByRestservice(true)) {
            JDBCUtil selectedJdbcutil = new JDBCUtil(datasource.getDriverclass(), datasource.getUrl(), datasource.getUser(), datasource.getPassword());
            Connection con = selectedJdbcutil.getConnection();
            if (null != con) {
                try {
                    DatabaseMetaData selectedDdbmd = selectedJdbcutil.getMetadata();
                    //System.out.println(selectedDdbmd.getDatabaseMajorVersion());
                    ResultSet rs = selectedDdbmd.getCatalogs();
                    while (rs.next()) {
                        String dbname = rs.getString("TABLE_CAT");
                        if (0 == dbname.compareToIgnoreCase(datasource.getDatabasename())) {
                            //System.out.println(dbname);
                            ResultSet tables = selectedDdbmd.getTables(dbname, null, null, null);
                            while (tables.next()) {
                                if (null != tables.getString("TABLE_TYPE")) {
                                    if (0 == tables.getString("TABLE_TYPE").compareToIgnoreCase("TABLE")) {
                                        boolean hasIdentity = false;
                                        TableData td = new TableData();
                                        td.setName(tables.getString("TABLE_NAME"));
                                        td.setType(tables.getString("TABLE_TYPE"));

                                        ResultSet crs = selectedDdbmd.getColumns(datasource.getDatabasename(), null, tables.getString("TABLE_NAME"), null);
                                        while (crs.next()) {
                                            ColumnData cd = new ColumnData();
                                            try {
                                                cd.setName(crs.getString("COLUMN_NAME"));
                                                cd.setType(crs.getInt("DATA_TYPE"));
                                                cd.setTypename(crs.getString("TYPE_NAME"));
                                                cd.setSize(crs.getInt("COLUMN_SIZE"));
                                                cd.setDigits(crs.getInt("DECIMAL_DIGITS"));
                                                cd.setRadix(crs.getInt("NUM_PREC_RADIX"));
                                                cd.setNullable(crs.getInt("NULLABLE"));
                                                cd.setDefaultvalue(crs.getString("COLUMN_DEF"));
                                                cd.setAutoinc(crs.getString("IS_AUTOINCREMENT"));
                                                cd.setPrimarykey(false);
                                                //cd.setGenerated(crs.getString("IS_GENERATEDCOLUMN"));
                                            } catch (Exception e) {
                                                LOGGER.error(e.getMessage());
                                            }
                                            ResultSet pkrs = selectedDdbmd.getPrimaryKeys(datasource.getDatabasename(), null, tables.getString("TABLE_NAME"));
                                            while (pkrs.next()) {
                                                if (0 == cd.getName().compareToIgnoreCase(pkrs.getString("COLUMN_NAME"))) {
                                                    cd.setPrimarykey(true);
                                                    hasIdentity = true;
                                                }
                                            }
                                            td.getColumns().add(cd);
                                        }
                                        if (hasIdentity) {
                                            entityUtil.getEntitystructurelist().put(new FullQualifiedName(NAMESPACE_ENTITY, datasource.getName() + "_" + tables.getString("TABLE_NAME")), td);
                                            entityUtil.getEntitysourcelist().put(new FullQualifiedName(NAMESPACE_ENTITY, datasource.getName() + "_" + tables.getString("TABLE_NAME")), new SourceStructure(1, 0, datasource.getDriverclass(), datasource.getUrl(), datasource.getUser(), datasource.getPassword(), td.getName()));

                                            CsdlEntityType et = getEntityType(new FullQualifiedName(NAMESPACE_ENTITY, datasource.getName() + "_" + tables.getString("TABLE_NAME")));
                                            if (null != et) {
                                                entityTypes.add(et);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    con.close();
                } catch (SQLException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
        }

        entity_schema.setEntityTypes(entityTypes);
        entity_schema.setComplexTypes(complexTypes);
        // add EntityContainer
        entity_schema.setEntityContainer(getEntityContainer());
        LOGGER.info("STOP GENERATE ODATA SCHEMA");
        return entity_schema;    
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
            for (CfClass clazz : cfclassservice.findByMaintenance(true)) {
                if (!getKeys(clazz).isEmpty()) {
                    CsdlEntitySet es = getEntitySet(CONTAINER, clazz.getName()+"Lists");
                    if (null != es) {
                        entitySets.add(es);
                    }
                }
            }
            for (CfList list : cflistservice.findAll()) {
                CfClass clazz = list.getClassref();
                if (!getKeys(clazz).isEmpty()) {
                    CsdlEntitySet es = getEntitySet(CONTAINER, list.getName()+"List");
                    if ((null != es) && (list.getClassref().isMaintenance())) {
                        entitySets.add(es);
                    }
                }
            }
            
            for (CfDatasource datasource : cfdatasourceService.findByRestservice(true)) {
                JDBCUtil selectedJdbcutil = new JDBCUtil(datasource.getDriverclass(), datasource.getUrl(), datasource.getUser(), datasource.getPassword());
                Connection con = selectedJdbcutil.getConnection();
                if (null != con) {
                    try {
                        DatabaseMetaData selectedDdbmd = selectedJdbcutil.getMetadata();
                        //System.out.println(selectedDdbmd.getDatabaseMajorVersion());
                        ResultSet rs = selectedDdbmd.getCatalogs();
                        while (rs.next()) {
                            String dbname = rs.getString("TABLE_CAT");
                            if (0 == dbname.compareToIgnoreCase(datasource.getDatabasename())) {
                                //System.out.println(dbname);
                                ResultSet tables = selectedDdbmd.getTables(dbname, null, null, null);
                                while (tables.next()) {
                                    if (null != tables.getString("TABLE_TYPE")) {
                                        if (0 == tables.getString("TABLE_TYPE").compareToIgnoreCase("TABLE")) {
                                            if (entityUtil.getEntitysourcelist().containsKey(new FullQualifiedName(NAMESPACE_ENTITY, datasource.getName() + "_" + tables.getString("TABLE_NAME")))) {
                                                CsdlSingleton si = getSingleton(CONTAINER, datasource.getName() + "_" + tables.getString("TABLE_NAME"));
                                                CsdlEntitySet es = getEntitySet(CONTAINER, datasource.getName() + "_" + tables.getString("TABLE_NAME")+"Set");
                                                if (null != es) {
                                                    singletons.add(si);
                                                    entitySets.add(es);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        con.close();
                    } catch (SQLException ex) {
                        LOGGER.error(ex.getMessage());
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
        if (entityUtil.getEntitytypelist().containsKey(entityTypeName)) {
            return entityUtil.getEntitytypelist().get(entityTypeName);
        } else {
            if (entityUtil.getEntitysourcelist().containsKey(entityTypeName)) {
                if (0 == entityUtil.getEntitysourcelist().get(entityTypeName).getSource()) {
                    //System.out.println(entityTypeName.getFullQualifiedNameAsString());
                    if (3 != entityUtil.getEntitysourcelist().get(entityTypeName).getList()) {
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
                            entityUtil.getEntitytypelist().put(entityTypeName, entityType);
                            return entityType;
                        } else {
                            LOGGER.warn("OData - Missing identifier for " + entityTypeName.getName());
                            return null;
                        }
                    } else {
                        CsdlEntityType entityType = new CsdlEntityType();
                        entityType.setName(entityTypeName.getName());
                        List propsList = new ArrayList();
                        List keysList = new ArrayList();
                        
                        CsdlProperty prop_id = new CsdlProperty().setName("id").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName()).setCollection(false);
                        propsList.add(prop_id);
                        CsdlPropertyRef propertyRef_id = new CsdlPropertyRef();
                        propertyRef_id.setName("id");
                        keysList.add(propertyRef_id);
                        
                        CsdlProperty prop_name = new CsdlProperty().setName("name").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()).setCollection(false);
                        propsList.add(prop_name);
                        
                        CsdlProperty prop_listset = new CsdlProperty().setName("listset").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName()).setCollection(true);
                        propsList.add(prop_listset);
                        
                        entityType.setProperties(propsList);
                        entityType.setKey(keysList);
                        return entityType;
                    }
                } else {
                    //System.out.println(entityTypeName.getFullQualifiedNameAsString());
                    TableData td = entityUtil.getEntitystructurelist().get(entityTypeName);
                    List propsList = new ArrayList();
                    List keysList = new ArrayList();
                    for (ColumnData cd : td.getColumns()) {
                        CsdlProperty prop = new CsdlProperty().setName(cd.getName()).setType(getODataDBType(cd)).setCollection(getODataDBCollection(cd));
                        propsList.add(prop);
                        if (cd.isPrimarykey()) {
                            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
                            propertyRef.setName(cd.getName());
                            keysList.add(propertyRef);
                        }
                    }
                    CsdlEntityType entityType = new CsdlEntityType();
                    entityType.setName(entityTypeName.getName());
                    entityType.setProperties(propsList);
                    entityType.setKey(keysList);

                    if (!keysList.isEmpty()) {
                        entityUtil.getEntitytypelist().put(entityTypeName, entityType);
                        return entityType;
                    } else {
                        LOGGER.warn("OData - Missing identifier for " + entityTypeName.getName());
                        return null;
                    }
                }
            } else {
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
        if (entityUtil.getComplextypelist().containsKey(complexTypeName)) {
            return entityUtil.getComplextypelist().get(complexTypeName);
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
                entityUtil.getComplextypelist().put(complexTypeName, complexType);
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
            if (entityUtil.getSingletonlist().containsKey(singletonName)) {
                return entityUtil.getSingletonlist().get(singletonName);
            } else {
                CsdlSingleton singleton = new CsdlSingleton();
                singleton.setName(singletonName);
                singleton.setType(new FullQualifiedName(NAMESPACE_ENTITY, singletonName));
                //singleton.setNavigationPropertyBindings(navigationPropertyBindings);
                entityUtil.getSingletonlist().put(singletonName, singleton);
                return singleton;
            }
        }
        return null;
    }
    
    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
        if (entityContainer.equals(CONTAINER)) {
            if (entityUtil.getEntitysetlist().containsKey(entitySetName)) {
                return entityUtil.getEntitysetlist().get(entitySetName);
            } else {
                CsdlEntitySet entitySet = new CsdlEntitySet();
                entitySet.setName(entitySetName);
                if ((entitySetName.endsWith("Set")) || (entitySetName.endsWith("Lists"))) {
                    int postlength = 3;
                    if (entitySetName.endsWith("Lists")) postlength = 5;
                    if (null != cfclassservice.findByName(entitySetName.substring(0, entitySetName.length()-postlength))) {
                        if (3 == postlength) {
                            entitySet.setType(new FullQualifiedName(NAMESPACE_ENTITY, entitySetName.substring(0, entitySetName.length()-postlength)));
                        } else {
                            entitySet.setType(new FullQualifiedName(NAMESPACE_ENTITY, "CFListEntry"));
                        }   
                    } else {
                        return null;
                    }
                } else {
                    if (entitySetName.endsWith("List")) {
                        CfList list = cflistservice.findByName(entitySetName.substring(0, entitySetName.length()-4));
                        if (null != list) {
                            entitySet.setType(new FullQualifiedName(NAMESPACE_ENTITY, list.getClassref().getName()));
                        } else {
                            return null;
                        }
                    } else {
                        entitySet.setType(new FullQualifiedName(NAMESPACE_ENTITY, entitySetName));
                    }
                }
                entityUtil.getEntitysetlist().put(entitySetName, entitySet);
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
                return EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
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
    
    public static FullQualifiedName getODataDBType(ColumnData cd) {
        switch (cd.getType()) {
            case -1:      // TEXT, varchar, char -> String
            case 1:
            case 12:
            case -15:
            case -16:
            case -9:
            case 2005:
            case 2004:  // varbinary ?
            case -4:    // longblob ?
                return EdmPrimitiveTypeKind.String.getFullQualifiedName();
            case 93:      // Date
            case 92:
            case 91:
            case 2014:
                return EdmPrimitiveTypeKind.Date.getFullQualifiedName();
            case 2:       // int, bigint
            case 4:
            case 5:         // SMALLINT
            case -5:
                return EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
            case 7:       // real, decimal
            case 3:
            case 8:
            case 6:
                return EdmPrimitiveTypeKind.Double.getFullQualifiedName();
            case -6:        // TINYINT
            case -7:      // bit, boolean
            case 16:
                return EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
            default:
                return null;
        }
    }
    
    public static FullQualifiedName getODataDB2Type(String type) {
        switch (type) {
            case "STRING":      // TEXT, varchar, char -> String
                return EdmPrimitiveTypeKind.String.getFullQualifiedName();
            case "DATE":      // Date
                return EdmPrimitiveTypeKind.Date.getFullQualifiedName();
            case "INT":       // int, smallint, tinyint
                return EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
            case "LONG":   
                return EdmPrimitiveTypeKind.Int64.getFullQualifiedName();
            case "FLOAT":       // real, decimal
                return EdmPrimitiveTypeKind.Double.getFullQualifiedName();
            case "BOOLEAN":      // bit, boolean
                return EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
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
        return ((0 == attribut.getAttributetype().getName().compareToIgnoreCase("classref")) && (0 == attribut.getRelationtype()) || (0 == attribut.getAttributetype().getName().compareToIgnoreCase("assetref")));
    }
    
    private boolean getODataDBCollection(ColumnData cd) {
        return false;
    }

    @Override
    public void run() {
        try {
            getNewSchemas();
        } catch (ODataException ex) {
            java.util.logging.Logger.getLogger(GenericEdmProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
