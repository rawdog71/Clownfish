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

import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.dbentities.CfTemplateversion;
import io.clownfish.clownfish.dbentities.CfTemplateversionPK;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
import io.clownfish.clownfish.utils.CheckoutUtil;
import io.clownfish.clownfish.utils.CompressionUtils;
import io.clownfish.clownfish.utils.TemplateUtil;
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
@Named("templateList")
@Scope("session")
@Component
public class TemplateList {
    @Inject
    LoginBean loginbean;
    @Autowired CfTemplateService cftemplateService;
    @Autowired CfTemplateversionService cftemplateversionService;
    
    private @Getter @Setter List<CfTemplate> templateListe;
    private @Getter @Setter CfTemplate selectedTemplate = null;
    private @Getter @Setter String templateName = "";
    private @Getter @Setter boolean newButtonDisabled = false;
    private @Getter @Setter int templateScriptLanguage = 0;
    private @Getter @Setter String selectedScriptlanguage = "";
    private @Getter @Setter CfTemplateversion version = null;
    private @Getter @Setter List<CfTemplateversion> versionlist;
    private @Getter @Setter boolean difference;
    private @Getter @Setter long checkedoutby = 0;
    private @Getter @Setter boolean checkedout;
    private @Getter @Setter boolean access;
    @Autowired private @Getter @Setter TemplateUtil templateUtility;
    
    final transient Logger logger = LoggerFactory.getLogger(TemplateList.class);

    public TemplateList() {
    }
    
    public String getTemplateContent() {
        if (null != selectedTemplate) {
            templateUtility.setTemplateContent(selectedTemplate.getContent());
            return templateUtility.getTemplateContent();
        } else {
            return "";
        }
    }
    
    public void setTemplateContent(String content) {
        if (null != selectedTemplate) {
            selectedTemplate.setContent(content);
        }
    }

    @PostConstruct
    public void init() {
        templateName = "";
        templateListe = cftemplateService.findAll();
        templateUtility.setTemplateContent("");
        checkedout = false;
        access = false;
    }
    
    public void onSelect(AjaxBehaviorEvent event) {
        difference = false;
        if (null != selectedTemplate) {
            templateName = selectedTemplate.getName();
            templateUtility.setTemplateContent(selectedTemplate.getContent());
            templateScriptLanguage = selectedTemplate.getScriptlanguage();
            if (0 == selectedTemplate.getScriptlanguage()) {
                selectedScriptlanguage = "freemarker";
            } else {
                selectedScriptlanguage = "velocity";
            }
            versionlist = cftemplateversionService.findByTemplateref(selectedTemplate.getId());
            difference = templateUtility.hasDifference(selectedTemplate);
            BigInteger co = selectedTemplate.getCheckedoutby();
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
        if (null != selectedTemplate) {
            selectedTemplate.setContent(getTemplateContent());
            cftemplateService.edit(selectedTemplate);
            difference = templateUtility.hasDifference(selectedTemplate);
            
            FacesMessage message = new FacesMessage("Saved " + selectedTemplate.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onCommit(ActionEvent actionEvent) {
        if (null != selectedTemplate) {
            boolean canCommit = false;
            if (templateUtility.hasDifference(selectedTemplate)) {
                canCommit = true;
            }
            if (canCommit) {
                try {
                    String content = getTemplateContent();
                    byte[] output = CompressionUtils.compress(content.getBytes("UTF-8"));
                    try {
                        long maxversion = cftemplateversionService.findMaxVersion(selectedTemplate.getId());
                        templateUtility.setCurrentVersion(maxversion + 1);
                        writeVersion(selectedTemplate.getId(), templateUtility.getCurrentVersion(), output);
                        difference = templateUtility.hasDifference(selectedTemplate);

                        FacesMessage message = new FacesMessage("Commited " + selectedTemplate.getName() + " Version: " + (maxversion + 1));
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    } catch (NullPointerException npe) {
                        writeVersion(selectedTemplate.getId(), 1, output);
                        templateUtility.setCurrentVersion(1);
                        difference = templateUtility.hasDifference(selectedTemplate);

                        FacesMessage message = new FacesMessage("Commited " + selectedTemplate.getName() + " Version: " + 1);
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    }
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                }
            } else {
                difference = templateUtility.hasDifference(selectedTemplate);
                access = true;

                FacesMessage message = new FacesMessage("Could not commit " + selectedTemplate.getName() + " Version: " + 1);
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }
    
    public void onCheckOut(ActionEvent actionEvent) {
        if (null != selectedTemplate) {
            boolean canCheckout = false;
            CfTemplate checktemplate = cftemplateService.findById(selectedTemplate.getId());
            BigInteger co = checktemplate.getCheckedoutby();
            if (null != co) {
                if (co.longValue() == 0) {
                    canCheckout = true;
                } 
            } else {
                canCheckout = true;
            }
                    
            if (canCheckout) {
                selectedTemplate.setCheckedoutby(BigInteger.valueOf(loginbean.getCfuser().getId()));
                selectedTemplate.setContent(getTemplateContent());
                cftemplateService.edit(selectedTemplate);
                difference = templateUtility.hasDifference(selectedTemplate);
                checkedout = true;

                FacesMessage message = new FacesMessage("Checked Out " + selectedTemplate.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else {
                access = false;
                FacesMessage message = new FacesMessage("could not Checked Out " + selectedTemplate.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }
    
    public void onCheckIn(ActionEvent actionEvent) {
        if (null != selectedTemplate) {
            selectedTemplate.setCheckedoutby(BigInteger.valueOf(0));
            selectedTemplate.setContent(getTemplateContent());
            cftemplateService.edit(selectedTemplate);
            difference = templateUtility.hasDifference(selectedTemplate);
            checkedout = false;
            
            FacesMessage message = new FacesMessage("Checked Out " + selectedTemplate.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            cftemplateService.findByName(templateName);
            newButtonDisabled = true;
        } catch (NoResultException ex) {
            newButtonDisabled = templateName.isEmpty();
        }
    }
    
    public void onCreate(ActionEvent actionEvent) {
        try {
            CfTemplate newtemplate = new CfTemplate();
            newtemplate.setName(templateName);
            newtemplate.setContent("//"+templateName);
            newtemplate.setScriptlanguage(templateScriptLanguage);
            cftemplateService.create(newtemplate);
            templateListe = cftemplateService.findAll();
            templateName = "";
        } catch (ConstraintViolationException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void onDelete(ActionEvent actionEvent) {
        if (null != selectedTemplate) {
            cftemplateService.delete(selectedTemplate);
            templateListe = cftemplateService.findAll();
            
            FacesMessage message = new FacesMessage("Deleted " + selectedTemplate.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    private void writeVersion(long templateref, long version, byte[] content) {
        CfTemplateversionPK templateversionpk = new CfTemplateversionPK();
        templateversionpk.setTemplateref(templateref);
        templateversionpk.setVersion(version);

        CfTemplateversion cftemplateversion = new CfTemplateversion();
        cftemplateversion.setCfTemplateversionPK(templateversionpk);
        cftemplateversion.setContent(content);
        cftemplateversion.setTstamp(new Date());
        cftemplateversion.setCommitedby(BigInteger.valueOf(loginbean.getCfuser().getId()));
        cftemplateversionService.create(cftemplateversion);
    }
    
    public void onVersionSelect(ActionEvent actionEvent) {
        if (null != selectedTemplate) {
            templateUtility.getVersion(version.getCfTemplateversionPK().getTemplateref(), version.getCfTemplateversionPK().getVersion());
        }
    }
}