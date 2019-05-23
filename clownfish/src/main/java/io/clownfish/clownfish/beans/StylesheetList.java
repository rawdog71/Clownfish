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

import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfStylesheetversion;
import io.clownfish.clownfish.dbentities.CfStylesheetversionPK;
import io.clownfish.clownfish.serviceinterface.CfStylesheetService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetversionService;
import io.clownfish.clownfish.utils.CompressionUtils;
import io.clownfish.clownfish.utils.StylesheetUtil;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Named("stylesheetList")
@Scope("session")
@Component
public class StylesheetList {
    @Inject
    LoginBean loginbean;
    @Autowired CfStylesheetService cfstylesheetService;
    @Autowired CfStylesheetversionService cfstylesheetversionService;
    
    private @Getter @Setter List<CfStylesheet> stylesheetListe;
    private @Getter @Setter CfStylesheet selectedStylesheet = null;
    private @Getter @Setter String stylesheetName;
    private @Getter @Setter boolean newButtonDisabled = false;
    private @Getter @Setter CfStylesheetversion version = null;
    private @Getter @Setter List<CfStylesheetversion> versionlist;
    private @Getter @Setter boolean difference;
    private @Getter @Setter long checkedoutby = 0;
    private @Getter @Setter boolean checkedout;
    private @Getter @Setter boolean access;
    @Autowired private @Getter @Setter StylesheetUtil stylesheetUtility;
    
    final Logger logger = LoggerFactory.getLogger(StylesheetList.class);

    public StylesheetList() {
    }
   
    public String getStylesheetContent() {
        if (null != selectedStylesheet) {
            stylesheetUtility.setStyelsheetContent(selectedStylesheet.getContent());
            return stylesheetUtility.getStyelsheetContent();
        } else {
            return "";
        }
    }
    
    public void setStylesheetContent(String content) {
        if (null != selectedStylesheet) {
            selectedStylesheet.setContent(content);
        }
    }

    @PostConstruct
    public void init() {
        stylesheetName = "";
        stylesheetListe = cfstylesheetService.findAll();
        stylesheetUtility.setStyelsheetContent("");
        checkedout = false;
        access = false;
    }
    
    public void onSelect(AjaxBehaviorEvent event) {
        difference = false;
        if (null != selectedStylesheet) {
            stylesheetName = selectedStylesheet.getName();
            stylesheetUtility.setStyelsheetContent(selectedStylesheet.getContent());
            versionlist = cfstylesheetversionService.findByStylesheetref(selectedStylesheet.getId());
            difference = stylesheetUtility.hasDifference(selectedStylesheet);
            BigInteger co = selectedStylesheet.getCheckedoutby();
            if (null != co) {
                if (co.longValue() > 0) {
                    if (co.longValue() == loginbean.getCfuser().getId()) {
                        checkedout = true;
                        access = true;
                    } else {
                        checkedout = false;
                        access = false;
                    }
                } else {
                    checkedout = false;
                    access = true;
                }
            } else {
                checkedout = false;
                access = true;
            }
        } else {
            checkedout = false;
            access = false;
        }
    }
    
    public void onSave(ActionEvent actionEvent) {
        if (null != selectedStylesheet) {
            selectedStylesheet.setContent(getStylesheetContent());
            cfstylesheetService.edit(selectedStylesheet);
            difference = stylesheetUtility.hasDifference(selectedStylesheet);
            
            FacesMessage message = new FacesMessage("Saved " + selectedStylesheet.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onCommit(ActionEvent actionEvent) {
        if (null != selectedStylesheet) {
            boolean canCommit = false;
            if (stylesheetUtility.hasDifference(selectedStylesheet)) {
                canCommit = true;
            }
            if (canCommit) {
                try {
                    String content = getStylesheetContent();
                    byte[] output = CompressionUtils.compress(content.getBytes("UTF-8"));
                    try {
                        long maxversion = cfstylesheetversionService.findMaxVersion(selectedStylesheet.getId());
                        stylesheetUtility.setCurrentVersion(maxversion + 1);
                        writeVersion(selectedStylesheet.getId(), stylesheetUtility.getCurrentVersion(), output);
                        difference = stylesheetUtility.hasDifference(selectedStylesheet);

                        FacesMessage message = new FacesMessage("Commited " + selectedStylesheet.getName() + " Version: " + (maxversion + 1));
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    } catch (NullPointerException npe) {
                        writeVersion(selectedStylesheet.getId(), 1, output);
                        stylesheetUtility.setCurrentVersion(1);
                        difference = stylesheetUtility.hasDifference(selectedStylesheet);

                        FacesMessage message = new FacesMessage("Commited " + selectedStylesheet.getName() + " Version: " + 1);
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    }
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                }
            } else {
                difference = stylesheetUtility.hasDifference(selectedStylesheet);
                access = true;

                FacesMessage message = new FacesMessage("Could not commit " + selectedStylesheet.getName() + " Version: " + 1);
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }
    
    public void onCheckIn(ActionEvent actionEvent) {
        if (null != selectedStylesheet) {
            selectedStylesheet.setCheckedoutby(BigInteger.valueOf(0));
            selectedStylesheet.setContent(getStylesheetContent());
            cfstylesheetService.edit(selectedStylesheet);
            
            difference = stylesheetUtility.hasDifference(selectedStylesheet);
            checkedout = false;
            
            FacesMessage message = new FacesMessage("Checked In " + selectedStylesheet.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onCheckOut(ActionEvent actionEvent) {
        if (null != selectedStylesheet) {
            boolean canCheckout = false;
            CfStylesheet checkstylesheet = cfstylesheetService.findById(selectedStylesheet.getId());
            BigInteger co = checkstylesheet.getCheckedoutby();
            if (null != co) {
                if (co.longValue() == 0) {
                    canCheckout = true;
                } 
            } else {
                canCheckout = true;
            }
                    
            if (canCheckout) {
                selectedStylesheet.setCheckedoutby(BigInteger.valueOf(loginbean.getCfuser().getId()));
                selectedStylesheet.setContent(getStylesheetContent());
                cfstylesheetService.edit(checkstylesheet);
                
                difference = stylesheetUtility.hasDifference(selectedStylesheet);
                checkedout = true;

                FacesMessage message = new FacesMessage("Checked Out " + selectedStylesheet.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else {
                access = false;
                FacesMessage message = new FacesMessage("could not Checked Out " + selectedStylesheet.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            CfStylesheet validateStylesheet = cfstylesheetService.findByName(stylesheetName);
            newButtonDisabled = true;
        } catch (NoResultException ex) {
            newButtonDisabled = stylesheetName.isEmpty();
        }
    }
    
    public void onCreate(ActionEvent actionEvent) {
        try {
            CfStylesheet newstylesheet = new CfStylesheet();
            newstylesheet.setName(stylesheetName);
            newstylesheet.setContent("//"+stylesheetName);
            cfstylesheetService.create(newstylesheet);
            stylesheetListe = cfstylesheetService.findAll();
            stylesheetName = "";
        } catch (ConstraintViolationException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void onDelete(ActionEvent actionEvent) {
        if (null != selectedStylesheet) {
            cfstylesheetService.delete(selectedStylesheet);
            stylesheetListe = cfstylesheetService.findAll();
            FacesMessage message = new FacesMessage("Deleted " + selectedStylesheet.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    private void writeVersion(long stylesheetref, long version, byte[] content) {
        CfStylesheetversionPK stylesheetversionpk = new CfStylesheetversionPK();
        stylesheetversionpk.setStylesheetref(stylesheetref);
        stylesheetversionpk.setVersion(version);

        CfStylesheetversion cfstylesheetversion = new CfStylesheetversion();
        cfstylesheetversion.setCfStylesheetversionPK(stylesheetversionpk);
        cfstylesheetversion.setContent(content);
        cfstylesheetversion.setTstamp(new Date());
        cfstylesheetversionService.create(cfstylesheetversion);
    }
    
    public void onVersionSelect(ActionEvent actionEvent) {
        if (null != selectedStylesheet) {
            String versioncontent = stylesheetUtility.getVersion(version.getCfStylesheetversionPK().getStylesheetref(), version.getCfStylesheetversionPK().getVersion());
        }
    }
}