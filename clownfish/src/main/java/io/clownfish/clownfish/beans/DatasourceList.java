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

import io.clownfish.clownfish.dbentities.CfDatasource;
import io.clownfish.clownfish.dbentities.CfSitedatasource;
import io.clownfish.clownfish.jdbc.JDBCUtil;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import io.clownfish.clownfish.serviceinterface.CfSitedatasourceService;
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
import javax.validation.ConstraintViolationException;
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
    
    final transient Logger LOGGER = LoggerFactory.getLogger(DatasourceList.class);

    @PostConstruct
    public void init() {
        datasourcelist = cfdatasourceService.findAll();
        newContentButtonDisabled = false;
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
}
