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
import io.clownfish.clownfish.utils.CheckoutUtil;
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
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SlideEndEvent;
import org.primefaces.extensions.model.monacoeditor.EScrollbarHorizontal;
import org.primefaces.extensions.model.monacoeditor.EScrollbarVertical;
import org.primefaces.extensions.model.monacoeditor.ETheme;
import org.primefaces.extensions.model.monacoeditor.EditorOptions;
import org.primefaces.extensions.model.monacoeditor.EditorScrollbarOptions;
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
@Scope("singleton")
@Component
public class StylesheetList implements ISourceContentInterface {
    @Inject
    LoginBean loginbean;
    @Autowired CfStylesheetService cfstylesheetService;
    @Autowired CfStylesheetversionService cfstylesheetversionService;
    
    private @Getter @Setter List<CfStylesheet> stylesheetListe;
    private @Getter @Setter CfStylesheet selectedStylesheet = null;
    private @Getter @Setter String stylesheetName = "";
    private @Getter @Setter boolean newButtonDisabled = true;
    private @Getter @Setter CfStylesheetversion version = null;
    private @Getter @Setter List<CfStylesheetversion> versionlist;
    private @Getter @Setter long stylesheetversion = 0;
    private @Getter @Setter long stylesheetversionMin = 0;
    private @Getter @Setter long stylesheetversionMax = 0;
    private @Getter @Setter long selectedstylesheetversion = 0;
    private @Getter @Setter boolean difference;
    private @Getter @Setter long checkedoutby = 0;
    private @Getter @Setter boolean checkedout;
    private @Getter @Setter boolean access;
    private @Getter @Setter EditorOptions editorOptions;
    @Autowired private @Getter @Setter StylesheetUtil stylesheetUtility;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(StylesheetList.class);

    public StylesheetList() {
    }
   
    @Override
    public String getContent() {
        if (null != selectedStylesheet) {
            if (selectedstylesheetversion != stylesheetversionMax) {
                return stylesheetUtility.getVersion(selectedStylesheet.getId(), selectedstylesheetversion);
            } else {
                stylesheetUtility.setStyelsheetContent(selectedStylesheet.getContent());
                return stylesheetUtility.getStyelsheetContent();
            }
        } else {
            return "";
        }
    }
    
    @Override
    public void setContent(String content) {
        if (null != selectedStylesheet) {
            selectedStylesheet.setContent(content);
        }
    }

    @PostConstruct
    @Override
    public void init() {
        LOGGER.info("INIT STYLESHEET START");
        stylesheetName = "";
        stylesheetListe = cfstylesheetService.findAll();
        stylesheetUtility.setStyelsheetContent("");
        checkedout = false;
        access = false;
        editorOptions = new EditorOptions();
        editorOptions.setLanguage("css");
        editorOptions.setTheme(ETheme.VS_DARK);
        editorOptions.setScrollbar(new EditorScrollbarOptions().setVertical(EScrollbarVertical.VISIBLE).setHorizontal(EScrollbarHorizontal.VISIBLE));
        LOGGER.info("INIT STYLESHEET END");
    }
    
    @Override
    public void refresh() {
        stylesheetListe = cfstylesheetService.findAll();
    }
    
    @Override
    public void onSelect(AjaxBehaviorEvent event) {
        difference = false;
        if (null != selectedStylesheet) {
            stylesheetName = selectedStylesheet.getName();
            stylesheetUtility.setStyelsheetContent(selectedStylesheet.getContent());
            versionlist = cfstylesheetversionService.findByStylesheetref(selectedStylesheet.getId());
            difference = stylesheetUtility.hasDifference(selectedStylesheet);
            BigInteger co = selectedStylesheet.getCheckedoutby();
            CheckoutUtil checkoutUtil = new CheckoutUtil();
            checkoutUtil.getCheckoutAccess(co, loginbean);
            checkedout = checkoutUtil.isCheckedout();
            access = checkoutUtil.isAccess();
            stylesheetversionMin = 1;
            stylesheetversionMax = versionlist.size();
            selectedstylesheetversion = stylesheetversionMax;
        } else {
            stylesheetName = "";
            checkedout = false;
            access = false;
        }
    }
    
    @Override
    public void onSave(ActionEvent actionEvent) {
        if (null != selectedStylesheet) {
            selectedStylesheet.setContent(getContent());
            cfstylesheetService.edit(selectedStylesheet);
            difference = stylesheetUtility.hasDifference(selectedStylesheet);
            
            FacesMessage message = new FacesMessage("Saved " + selectedStylesheet.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    @Override
    public void onCommit(ActionEvent actionEvent) {
        if (null != selectedStylesheet) {
            boolean canCommit = false;
            if (stylesheetUtility.hasDifference(selectedStylesheet)) {
                canCommit = true;
            }
            if (canCommit) {
                try {
                    String content = getContent();
                    byte[] output = CompressionUtils.compress(content.getBytes("UTF-8"));
                    try {
                        long maxversion = cfstylesheetversionService.findMaxVersion(selectedStylesheet.getId());
                        stylesheetUtility.setCurrentVersion(maxversion + 1);
                        writeVersion(selectedStylesheet.getId(), stylesheetUtility.getCurrentVersion(), output);
                        difference = stylesheetUtility.hasDifference(selectedStylesheet);
                        this.stylesheetversionMax = stylesheetUtility.getCurrentVersion();
                        this.selectedstylesheetversion = this.stylesheetversionMax;

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
                    LOGGER.error(ex.getMessage());
                }
            } else {
                difference = stylesheetUtility.hasDifference(selectedStylesheet);
                access = true;

                FacesMessage message = new FacesMessage("Could not commit " + selectedStylesheet.getName() + " Version: " + 1);
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }
    
    @Override
    public void onCheckIn(ActionEvent actionEvent) {
        if (null != selectedStylesheet) {
            selectedStylesheet.setCheckedoutby(BigInteger.valueOf(0));
            selectedStylesheet.setContent(getContent());
            cfstylesheetService.edit(selectedStylesheet);
            
            difference = stylesheetUtility.hasDifference(selectedStylesheet);
            checkedout = false;
            
            FacesMessage message = new FacesMessage("Checked In " + selectedStylesheet.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    @Override
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
                selectedStylesheet.setContent(getContent());
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
    
    @Override
    public void onChangeName(ValueChangeEvent changeEvent) {
        if (!stylesheetName.isBlank()) {
            try {
                cfstylesheetService.findByName(stylesheetName);
                newButtonDisabled = true;
            } catch (NoResultException ex) {
                newButtonDisabled = stylesheetName.isEmpty();
            }
        } else {
            newButtonDisabled = true;
        }
    }
    
    @Override
    public void onCreate(ActionEvent actionEvent) {
        try {
            if (!stylesheetName.isBlank()) {
                CfStylesheet newstylesheet = new CfStylesheet();
                newstylesheet.setName(stylesheetName);
                newstylesheet.setContent("//"+stylesheetName);
                cfstylesheetService.create(newstylesheet);
                stylesheetListe = cfstylesheetService.findAll();
                stylesheetName = "";
            } else {
                FacesMessage message = new FacesMessage("Please enter stylesheet name");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    @Override
    public void onDelete(ActionEvent actionEvent) {
        if (null != selectedStylesheet) {
            cfstylesheetService.delete(selectedStylesheet);
            stylesheetListe = cfstylesheetService.findAll();
            FacesMessage message = new FacesMessage("Deleted " + selectedStylesheet.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    @Override
    public void writeVersion(long stylesheetref, long version, byte[] content) {
        CfStylesheetversionPK stylesheetversionpk = new CfStylesheetversionPK();
        stylesheetversionpk.setStylesheetref(stylesheetref);
        stylesheetversionpk.setVersion(version);

        CfStylesheetversion cfstylesheetversion = new CfStylesheetversion();
        cfstylesheetversion.setCfStylesheetversionPK(stylesheetversionpk);
        cfstylesheetversion.setContent(content);
        cfstylesheetversion.setTstamp(new Date());
        cfstylesheetversionService.create(cfstylesheetversion);
    }
    
    @Override
    public void onVersionSelect(ActionEvent actionEvent) {
        if (null != selectedStylesheet) {
            stylesheetUtility.getVersion(version.getCfStylesheetversionPK().getStylesheetref(), version.getCfStylesheetversionPK().getVersion());
        }
    }
    
    @Override
    public void onSlideEnd(SlideEndEvent event) {
        selectedstylesheetversion = (int) event.getValue();
        if (selectedstylesheetversion <= stylesheetversionMin) {
            selectedstylesheetversion = stylesheetversionMin;
        }
        if (selectedstylesheetversion >= stylesheetversionMax) {
            selectedstylesheetversion = stylesheetversionMax;
        }
    }
   
    @Override
    public void onVersionChanged() {
        if (stylesheetversion <= stylesheetversionMin) {
            stylesheetversion = stylesheetversionMin;
        }
        if (stylesheetversion >= stylesheetversionMax) {
            stylesheetversion = stylesheetversionMax;
        }
        selectedstylesheetversion = stylesheetversion;
    }

    @Override
    public void onChange(ActionEvent actionEvent) {
        if (null != selectedStylesheet) {
            selectedStylesheet.setName(stylesheetName);
            cfstylesheetService.edit(selectedStylesheet);
            difference = stylesheetUtility.hasDifference(selectedStylesheet);
            refresh();
            
            FacesMessage message = new FacesMessage("Changed " + selectedStylesheet.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }  else {
            FacesMessage message = new FacesMessage("No stylesheet selected. Nothing changed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
}