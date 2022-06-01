/*
 * Copyright 2019 sulzbachr.
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
package io.clownfish.clownfish.beans;

import io.clownfish.clownfish.datamodels.ColumnData;
import io.clownfish.clownfish.datamodels.TableData;
import io.clownfish.clownfish.dbentities.CfDatasource;
import io.clownfish.clownfish.dbentities.CfSearchdatabase;
import io.clownfish.clownfish.dbentities.CfSearchdatabasePK;
import io.clownfish.clownfish.dbentities.CfSitedatasource;
import io.clownfish.clownfish.jdbc.JDBCUtil;
import io.clownfish.clownfish.lucene.DatabasetableIndexer;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import io.clownfish.clownfish.serviceinterface.CfSearchdatabaseService;
import io.clownfish.clownfish.serviceinterface.CfSitedatasourceService;
import io.clownfish.clownfish.utils.DatabaseUtil;
import java.io.Serializable;
import java.sql.Connection;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import javax.persistence.NoResultException;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Named("datasourceList")
@Scope("singleton")
@Component
public class DatasourceList implements Serializable {
    @Autowired transient CfDatasourceService cfdatasourceService;
    @Autowired transient CfSitedatasourceService cfsitedatasourceService;
    @Autowired transient DatabaseUtil databaseUtil;
    @Autowired transient CfSearchdatabaseService cfsearchdatabaseService;
    @Autowired DatabasetableIndexer dbtableIndexer;
    
    private transient @Getter @Setter List<CfDatasource> datasourcelist = null;
    private @Getter @Setter CfDatasource selectedDatasource = null;
    private @Getter @Setter String datasourceName;
    private @Getter @Setter String datasourceServer;
    private @Getter @Setter String datasourceURL;
    private @Getter @Setter int datasourcePort;
    private @Getter @Setter String datasourceDatabasename;
    private @Getter @Setter String datasourceUser;
    private @Getter @Setter String datasourcePassword;
    private @Getter @Setter String datasourceDriverclass;
    private @Getter @Setter boolean newContentButtonDisabled = false;
    private transient @Getter @Setter List<TableData> tablelist = null;
    private @Getter @Setter TableData selectedTable = null;
    private @Getter @Setter JDBCUtil selectedJdbcutil = null;
    private @Getter @Setter DatabaseMetaData selectedDdbmd = null;
    private @Getter @Setter ColumnData selectedColumn = null;
    private @Getter @Setter boolean searchdatabseflag = false;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(DatasourceList.class);

    @PostConstruct
    public void init() {
        LOGGER.info("INIT DATASOURCELIST START");
        datasourcelist = cfdatasourceService.findAll();
        newContentButtonDisabled = false;
        tablelist = new ArrayList<>();
        LOGGER.info("INIT DATASOURCELIST END");
    }
    
    public void onRefreshAll() {
        datasourcelist = cfdatasourceService.findAll();
    }
    
    /**
     * Selects an external datasource
     * @param event
     */
    public void onSelect(SelectEvent event) {
        selectedDatasource = (CfDatasource) event.getObject();
        
        datasourceName = selectedDatasource.getName();
        datasourceDatabasename = selectedDatasource.getDatabasename();
        datasourceDriverclass = selectedDatasource.getDriverclass();
        datasourcePassword = selectedDatasource.getPassword();
        datasourcePort = selectedDatasource.getPort();
        datasourceServer = selectedDatasource.getServer();
        datasourceURL = selectedDatasource.getUrl();
        datasourceUser = selectedDatasource.getUser();
        
        newContentButtonDisabled = true;
        searchdatabseflag = false;

        tablelist.clear();
        try {
            selectedJdbcutil = new JDBCUtil(selectedDatasource.getDriverclass(), selectedDatasource.getUrl(), selectedDatasource.getUser(), selectedDatasource.getPassword());
            Connection con = selectedJdbcutil.getConnection();
            if (null != con) {
                selectedDdbmd = selectedJdbcutil.getMetadata();
                System.out.println(selectedDdbmd.getDatabaseMajorVersion());
                ResultSet rs = selectedDdbmd.getCatalogs();
                //ResultSetMetaData rmd = rs.getMetaData();
                //TableFieldStructure tfs = getTableFieldsList(rmd);
                while (rs.next()) {
                    String value = rs.getString("TABLE_CAT");
                    if (0 == value.compareToIgnoreCase(datasourceDatabasename)) {
                        System.out.println(value);
                        ResultSet tables = selectedDdbmd.getTables(value, null, null, null);
                        while (tables.next()) {
                            TableData td = new TableData();
                            td.setName(tables.getString("TABLE_NAME"));
                            td.setType(tables.getString("TABLE_TYPE"));

                            ResultSet crs = selectedDdbmd.getColumns(datasourceDatabasename, null, tables.getString("TABLE_NAME"), null);
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
                                ResultSet pkrs = selectedDdbmd.getPrimaryKeys(datasourceDatabasename, null, tables.getString("TABLE_NAME"));
                                while (pkrs.next()) {
                                    if (0 == cd.getName().compareToIgnoreCase(pkrs.getString("COLUMN_NAME"))) {
                                        cd.setPrimarykey(true);
                                    }
                                }
                                td.getColumns().add(cd);
                            }
                            tablelist.add(td);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    /**
     * Selects an external datasource
     * @param event
     */
    public void onTableSelect(SelectEvent event) {
        selectedTable = (TableData) event.getObject();
        //System.out.println(selectedDatasource.getId() + " " + selectedTable.getName());
        try {
            CfSearchdatabase searchdatabase = cfsearchdatabaseService.findByDatasourceRefAndTable(selectedDatasource.getId(), selectedTable.getName());
            searchdatabseflag = true;
        } catch (Exception ex) {
            searchdatabseflag = false;
            System.out.println("NOT FOUND " + selectedDatasource.getId() + " " + selectedTable.getName());
        }
    }
     
    public void onGenerateHTML() {
        if (null != selectedTable) {
            databaseUtil.generateHTMLForm(selectedDatasource, selectedTable);
        }
    }
    
    public void onSetSearchdatabse() {
        if (null != selectedTable) {
            CfSearchdatabase sdb = new CfSearchdatabase();
            CfSearchdatabasePK sdbpk = new CfSearchdatabasePK();
            sdbpk.setDatasourceRef(selectedDatasource.getId());
            sdbpk.setTablename(selectedTable.getName());
            sdb.setCfSearchdatabsePK(sdbpk);
            if (searchdatabseflag) {
                try {
                    cfsearchdatabaseService.create(sdb);
                    dbtableIndexer.createIndex(sdb);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(DatasourceList.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                cfsearchdatabaseService.delete(sdb);
            }
        }
    }
    
    /**
     * Creates an external datasource
     * @param actionEvent
     */
    public void onCreateContent(ActionEvent actionEvent) {
        try {
            CfDatasource newdatasourcecontent = new CfDatasource();
            newdatasourcecontent.setName(datasourceName);
            newdatasourcecontent.setDatabasename(datasourceDatabasename);
            newdatasourcecontent.setDriverclass(datasourceDriverclass);
            newdatasourcecontent.setPassword(datasourcePassword);
            newdatasourcecontent.setPort(datasourcePort);
            newdatasourcecontent.setServer(datasourceServer);
            newdatasourcecontent.setUrl(datasourceURL);
            newdatasourcecontent.setUser(datasourceUser);
            
            cfdatasourceService.create(newdatasourcecontent);
            datasourcelist = cfdatasourceService.findAll();
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    /**
     * Edits an external datasource
     * @param actionEvent
     */
    public void onEditContent(ActionEvent actionEvent) {
        try {
            if (selectedDatasource != null) {
                selectedDatasource.setName(datasourceName);
                selectedDatasource.setDatabasename(datasourceDatabasename);
                selectedDatasource.setDriverclass(datasourceDriverclass);
                selectedDatasource.setPassword(datasourcePassword);
                selectedDatasource.setPort(datasourcePort);
                selectedDatasource.setServer(datasourceServer);
                selectedDatasource.setUrl(datasourceURL);
                selectedDatasource.setUser(datasourceUser);
                cfdatasourceService.edit(selectedDatasource);
                datasourcelist = cfdatasourceService.findAll();
            }
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    /**
     * Deletes an external datasource
     * @param actionEvent
     */
    public void onDeleteContent(ActionEvent actionEvent) {
        if (selectedDatasource != null) {
            List<CfSitedatasource> sitedatasourcelist = cfsitedatasourceService.findByDatasourceref(selectedDatasource.getId());
            sitedatasourcelist.stream().forEach((sitedatasource) -> {
                cfsitedatasourceService.delete(sitedatasource);
            });
            cfdatasourceService.delete(selectedDatasource);
            datasourcelist = cfdatasourceService.findAll();
        }
    }
    
    /**
     * Changes the name of an external datasource
     * @param changeEvent
     */
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            cfdatasourceService.findByName(datasourceName);
            newContentButtonDisabled = true;
        } catch (NoResultException ex) {
            newContentButtonDisabled = datasourceName.isEmpty();
        }
    }
    
    /**
     * Checks the connection to an external datasource
     * @param actionEvent
     */
    public void onConnectionCheck(ActionEvent actionEvent) {
        if (selectedDatasource != null) {
            JDBCUtil jdbcutil = new JDBCUtil(selectedDatasource.getDriverclass(), selectedDatasource.getUrl(), selectedDatasource.getUser(), selectedDatasource.getPassword());
            Connection con = jdbcutil.getConnection();
            if (null != con) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Connection check", "Connection check successfully");
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Connection check", "Connection check error");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }
    
    public String showPrimaryKey(boolean pk) {
        if (pk) {
            return " *";
        } else {
            return "";
        }
    }
}
