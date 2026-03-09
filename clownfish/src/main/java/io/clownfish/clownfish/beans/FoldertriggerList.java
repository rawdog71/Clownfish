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

import io.clownfish.clownfish.Clownfish;
import io.clownfish.clownfish.dbentities.CfFoldertrigger;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.serviceinterface.CfFoldertriggerService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
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
@Named("foldertriggerlist")
@Scope("singleton")
@Component
public class FoldertriggerList {
    @Autowired CfFoldertriggerService cffoldertriggerService;
    @Autowired CfSiteService cfsiteService;
    
    private @Getter @Setter List<CfFoldertrigger> foldertriggerlist;
    private @Getter @Setter CfFoldertrigger selectedFoldertrigger;
    private @Getter @Setter List<CfFoldertrigger> filteredFoldertrigger;
    private @Getter @Setter boolean newFoldertriggerButtonDisabled;
    private @Getter @Setter String foldertriggername;
    private @Getter @Setter String foldertriggerfolder;
    private @Getter @Setter boolean foldertriggerrecursive;
    private @Getter @Setter boolean foldertriggeractive;
    private @Getter @Setter String foldertriggerparameter;
    private @Getter @Setter CfSite siteref;
    private Clownfish clownfish;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(PropertyList.class);

    public FoldertriggerList() {
    }
    
    @PostConstruct
    public void init() {
        LOGGER.info("INIT FOLDERTRIGGERLIST START");
        foldertriggerlist = cffoldertriggerService.findAll();
        newFoldertriggerButtonDisabled = false;
        LOGGER.info("INIT FOLDERTRIGGERLIST END");
    }
    
    public void setClownfish(Clownfish clownfish) {
        this.clownfish = clownfish;
    }
    
    public void onSelect(SelectEvent event) {
        selectedFoldertrigger = (CfFoldertrigger) event.getObject();
        foldertriggername = selectedFoldertrigger.getName();
        foldertriggerfolder = selectedFoldertrigger.getFolder();
        foldertriggerrecursive = selectedFoldertrigger.getRecursive();
        foldertriggeractive = selectedFoldertrigger.getActive();
        foldertriggerparameter = selectedFoldertrigger.getParameter();
        siteref = cfsiteService.findById(selectedFoldertrigger.getSiteref().longValue());
        newFoldertriggerButtonDisabled = true;
    }
    
    public void onCreateFoldertrigger(ActionEvent actionEvent) {
        try {
            CfFoldertrigger newfoldertrigger = new CfFoldertrigger();
            newfoldertrigger.setName(foldertriggername);
            newfoldertrigger.setFolder(foldertriggerfolder);
            newfoldertrigger.setRecursive(foldertriggerrecursive);
            newfoldertrigger.setActive(foldertriggeractive);
            newfoldertrigger.setParameter(foldertriggerparameter);
            cffoldertriggerService.create(newfoldertrigger);
            clownfish.setInitmessage(false);
            clownfish.init();
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    public void onEditFoldertrigger(ActionEvent actionEvent) {
        try {
            if (null != selectedFoldertrigger) {
                cffoldertriggerService.edit(selectedFoldertrigger);
                clownfish.setInitmessage(false);
                clownfish.init();
            }
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    public void onDeleteFoldertrigger(ActionEvent actionEvent) {
        if (null != selectedFoldertrigger) {
            cffoldertriggerService.delete(selectedFoldertrigger);
            clownfish.setInitmessage(false);
            clownfish.init();
        }
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        CfFoldertrigger newproperty = cffoldertriggerService.findByName(foldertriggername);
        if (null == newproperty) {
            newFoldertriggerButtonDisabled = false;
        } else {
            newFoldertriggerButtonDisabled = !selectedFoldertrigger.getName().isEmpty();
        }
    }
}
