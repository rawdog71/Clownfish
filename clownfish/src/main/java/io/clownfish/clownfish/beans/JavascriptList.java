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

import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfJavascriptversion;
import io.clownfish.clownfish.dbentities.CfJavascriptversionPK;
import io.clownfish.clownfish.serviceinterface.CfJavascriptService;
import io.clownfish.clownfish.serviceinterface.CfJavascriptversionService;
import io.clownfish.clownfish.utils.CheckoutUtil;
import io.clownfish.clownfish.utils.CompressionUtils;
import io.clownfish.clownfish.utils.JavascriptUtil;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author sulzbachr
 */
@Named("javascriptList")
@Scope("singleton")
@Component
public class JavascriptList {
    @Inject
    LoginBean loginbean;
    @Autowired CfJavascriptService cfjavascriptService;
    @Autowired CfJavascriptversionService cfjavascriptversionService;
    
    private @Getter @Setter List<CfJavascript> javascriptListe;
    private @Getter @Setter CfJavascript selectedJavascript = null;
    private @Getter @Setter String javascriptName = "";
    private @Getter @Setter boolean newButtonDisabled = false;
    private @Getter @Setter CfJavascriptversion version = null;
    private @Getter @Setter List<CfJavascriptversion> versionlist;
    private @Getter @Setter boolean difference;
    private @Getter @Setter long checkedoutby = 0;
    private @Getter @Setter boolean checkedout;
    private @Getter @Setter boolean access;
    @Autowired private @Getter @Setter JavascriptUtil javascriptUtility;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(JavascriptList.class);

    public JavascriptList() {
    }

    public String getJavascriptContent() {
        if (null != selectedJavascript) {
            javascriptUtility.setJavascriptContent(selectedJavascript.getContent());
            return javascriptUtility.getJavascriptContent();
        } else {
            return "";
        }
    }

    public void setJavascriptContent(String content) {
        if (null != selectedJavascript) {
            selectedJavascript.setContent(content);
        }
    }

    @PostConstruct
    public void init() {
        javascriptName = "";
        javascriptListe = cfjavascriptService.findAll();
        javascriptUtility.setJavascriptContent("");
        checkedout = false;
        access = false;
    }
    
    public void refresh() {
        javascriptListe = cfjavascriptService.findAll();
    }
    
    public void onSelect(AjaxBehaviorEvent event) {
        difference = false;
        if (null != selectedJavascript) {
            javascriptName = selectedJavascript.getName();
            javascriptUtility.setJavascriptContent(selectedJavascript.getContent());
            versionlist = cfjavascriptversionService.findByJavascriptref(selectedJavascript.getId());
            difference = javascriptUtility.hasDifference(selectedJavascript);
            BigInteger co = selectedJavascript.getCheckedoutby();
            CheckoutUtil checkoutUtil = new CheckoutUtil();
            checkoutUtil.getCheckoutAccess(co, loginbean);
            checkedout = checkoutUtil.isCheckedout();
            access = checkoutUtil.isAccess();
        } else {
            checkedout = false;
            access = false;
        }
    }
    
    public void onSave(ActionEvent actionEvent) {
        if (null != selectedJavascript) {
            selectedJavascript.setContent(getJavascriptContent());
            cfjavascriptService.edit(selectedJavascript);
            difference = javascriptUtility.hasDifference(selectedJavascript);
            
            FacesMessage message = new FacesMessage("Saved " + selectedJavascript.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onCommit(ActionEvent actionEvent) {
        if (null != selectedJavascript) {
            boolean canCommit = false;
            if (javascriptUtility.hasDifference(selectedJavascript)) {
                canCommit = true;
            }
            if (canCommit) {
                try {
                    String content = getJavascriptContent();
                    byte[] output = CompressionUtils.compress(content.getBytes("UTF-8"));
                    try {
                        long maxversion = cfjavascriptversionService.findMaxVersion(selectedJavascript.getId());
                        javascriptUtility.setCurrentVersion(maxversion + 1);
                        writeVersion(selectedJavascript.getId(), javascriptUtility.getCurrentVersion(), output);
                        difference = javascriptUtility.hasDifference(selectedJavascript);

                        FacesMessage message = new FacesMessage("Commited " + selectedJavascript.getName() + " Version: " + (maxversion + 1));
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    } catch (NullPointerException npe) {
                        writeVersion(selectedJavascript.getId(), 1, output);
                        javascriptUtility.setCurrentVersion(1);
                        difference = javascriptUtility.hasDifference(selectedJavascript);

                        FacesMessage message = new FacesMessage("Commited " + selectedJavascript.getName() + " Version: " + 1);
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    }
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage());
                }
            } else {
                difference = javascriptUtility.hasDifference(selectedJavascript);
                access = true;

                FacesMessage message = new FacesMessage("Could not commit " + selectedJavascript.getName() + " Version: " + 1);
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }
    
    public void onCheckIn(ActionEvent actionEvent) {
        if (null != selectedJavascript) {
            selectedJavascript.setCheckedoutby(BigInteger.valueOf(0));
            selectedJavascript.setContent(getJavascriptContent());
            cfjavascriptService.edit(selectedJavascript);
            
            difference = javascriptUtility.hasDifference(selectedJavascript);
            checkedout = false;
            
            FacesMessage message = new FacesMessage("Checked In " + selectedJavascript.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onCheckOut(ActionEvent actionEvent) {
        if (null != selectedJavascript) {
            boolean canCheckout = false;
            CfJavascript checkjavascript = cfjavascriptService.findById(selectedJavascript.getId());
            BigInteger co = checkjavascript.getCheckedoutby();
            if (null != co) {
                if (co.longValue() == 0) {
                    canCheckout = true;
                } 
            } else {
                canCheckout = true;
            }
                    
            if (canCheckout) {
                selectedJavascript.setCheckedoutby(BigInteger.valueOf(loginbean.getCfuser().getId()));
                selectedJavascript.setContent(getJavascriptContent());
                cfjavascriptService.edit(selectedJavascript);
                difference = javascriptUtility.hasDifference(selectedJavascript);
                checkedout = true;

                FacesMessage message = new FacesMessage("Checked Out " + selectedJavascript.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else {
                access = false;
                FacesMessage message = new FacesMessage("could not Checked Out " + selectedJavascript.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            cfjavascriptService.findByName(javascriptName);
            newButtonDisabled = true;
        } catch (NoResultException ex) {
            newButtonDisabled = javascriptName.isEmpty();
        }
    }
    
    public void onCreate(ActionEvent actionEvent) {
        try {
            CfJavascript newjavascript = new CfJavascript();
            newjavascript.setName(javascriptName);
            newjavascript.setContent("//"+javascriptName);
            cfjavascriptService.create(newjavascript);
            javascriptListe = cfjavascriptService.findAll();
            
            javascriptName = "";
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    public void onDelete(ActionEvent actionEvent) {
        if (null != selectedJavascript) {
            cfjavascriptService.delete(selectedJavascript);
            javascriptListe = cfjavascriptService.findAll();
            
            FacesMessage message = new FacesMessage("Deleted " + selectedJavascript.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    private void writeVersion(long javascriptref, long version, byte[] content) {
        CfJavascriptversionPK javascriptversionpk = new CfJavascriptversionPK();
        javascriptversionpk.setJavascriptref(javascriptref);
        javascriptversionpk.setVersion(version);

        CfJavascriptversion cfjavascriptversion = new CfJavascriptversion();
        cfjavascriptversion.setCfJavascriptversionPK(javascriptversionpk);
        cfjavascriptversion.setContent(content);
        cfjavascriptversion.setTstamp(new Date());
        cfjavascriptversionService.create(cfjavascriptversion);
    }
    
    public void onVersionSelect(ActionEvent actionEvent) {
        if (null != selectedJavascript) {
            javascriptUtility.getVersion(version.getCfJavascriptversionPK().getJavascriptref(), version.getCfJavascriptversionPK().getVersion());
        }
    }
}