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
import io.clownfish.clownfish.lucene.IndexService;
import io.clownfish.clownfish.lucene.SourceIndexer;
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
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
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
@Named("templateList")
@Scope("singleton")
@Component
public class TemplateList implements ISourceContentInterface {
    @Inject
    LoginBean loginbean;
    @Autowired CfTemplateService cftemplateService;
    @Autowired CfTemplateversionService cftemplateversionService;
    
    private @Getter @Setter List<CfTemplate> templateListe;
    private @Getter @Setter CfTemplate selectedTemplate = null;
    private @Getter @Setter String templateName = "";
    private @Getter @Setter boolean newButtonDisabled = true;
    private @Getter @Setter int templateScriptLanguage = 0;
    private @Getter @Setter long templateversion = 0;
    private @Getter @Setter long templateversionMin = 0;
    private @Getter @Setter long templateversionMax = 0;
    private @Getter @Setter long selectedtemplateversion = 0;
    private @Getter @Setter String selectedScriptlanguage = "";
    private @Getter @Setter CfTemplateversion version = null;
    private @Getter @Setter List<CfTemplateversion> versionlist;
    private @Getter @Setter boolean difference;
    private @Getter @Setter long checkedoutby = 0;
    private @Getter @Setter boolean checkedout;
    private @Getter @Setter boolean access;
    private @Getter @Setter boolean layout;
    private @Getter @Setter EditorOptions editorOptions;
    @Autowired private @Getter @Setter TemplateUtil templateUtility;
    @Autowired @Getter @Setter IndexService indexService;
    @Autowired @Getter @Setter SourceIndexer sourceindexer;
    @Autowired private @Getter @Setter ClassList classlist;
    private @Getter @Setter SiteTreeBean sitetree;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(TemplateList.class);

    public TemplateList() {
    }
    
    @Override
    public String getContent() {
        if (null != selectedTemplate) {
            if (selectedtemplateversion != templateversionMax) {
                return templateUtility.getVersion(selectedTemplate.getId(), selectedtemplateversion);
            } else {
                templateUtility.setTemplateContent(selectedTemplate.getContent());
                return templateUtility.getTemplateContent();
            }
        } else {
            return "";
        }
    }
    
    @Override
    public void setContent(String content) {
        if (null != selectedTemplate) {
            selectedTemplate.setContent(content);
        }
    }

    @PostConstruct
    @Override
    public void init() {
        LOGGER.info("INIT TEMPLATE START");
        try {
            sourceindexer.initTemplate(cftemplateService, indexService);
        } catch (IOException ex) {
            
        }
        templateName = "";
        templateListe = cftemplateService.findAll();
        templateUtility.setTemplateContent("");
        checkedout = false;
        access = false;
        layout = false;
        editorOptions = new EditorOptions();
        editorOptions.setLanguage("");
        editorOptions.setTheme(ETheme.VS_DARK);
        editorOptions.setScrollbar(new EditorScrollbarOptions().setVertical(EScrollbarVertical.VISIBLE).setHorizontal(EScrollbarHorizontal.VISIBLE));
        LOGGER.info("INIT TEMPLATE END");
    }
    
    @Override
    public void refresh() {
        templateListe = cftemplateService.findAll();
        if (null != classlist) {
            classlist.onRefreshSelection();
        }
        if (null != sitetree) {
            sitetree.onRefreshSelection();
        }
    }
    
    public List<CfTemplate> completeText(String query) {
        String queryLowerCase = query.toLowerCase();

        return templateListe.stream().filter(t -> t.getName().toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }
    
    @Override
    public void onSelect(AjaxBehaviorEvent event) {
        difference = false;
        if (null != selectedTemplate) {
            templateName = selectedTemplate.getName();
            templateUtility.setTemplateContent(selectedTemplate.getContent());
            templateScriptLanguage = selectedTemplate.getScriptlanguage();
            switch (selectedTemplate.getScriptlanguage()) {
                case 0:
                    selectedScriptlanguage = "freemarker";
                    editorOptions.setLanguage("html");
                    break;
                case 1:
                    selectedScriptlanguage = "velocity";
                    editorOptions.setLanguage("html");
                    break;
                case 2:
                    selectedScriptlanguage = "htmlmixed";
                    editorOptions.setLanguage("html");
                    break;
                case 3:
                    selectedScriptlanguage = "jrxml";
                    editorOptions.setLanguage("xml");
                    break;
            }
            versionlist = cftemplateversionService.findByTemplateref(selectedTemplate.getId());
            difference = templateUtility.hasDifference(selectedTemplate);
            BigInteger co = selectedTemplate.getCheckedoutby();
            CheckoutUtil checkoutUtil = new CheckoutUtil();
            checkoutUtil.getCheckoutAccess(co, loginbean);
            checkedout = checkoutUtil.isCheckedout();
            access = checkoutUtil.isAccess();
            layout = selectedTemplate.isLayout();
            templateversionMin = 1;
            templateversionMax = versionlist.size();
            selectedtemplateversion = templateversionMax;
        } else {
            templateName = "";
            checkedout = false;
            access = false;
        }
    }
    
    @Override
    public void onSave(ActionEvent actionEvent) {
        if (null != selectedTemplate) {
            selectedTemplate.setScriptlanguage(templateScriptLanguage);
            selectedTemplate.setContent(getContent());
            selectedTemplate.setLayout(layout);
            cftemplateService.edit(selectedTemplate);
            difference = templateUtility.hasDifference(selectedTemplate);
            
            FacesMessage message = new FacesMessage("Saved " + selectedTemplate.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    @Override
    public void onCommit(ActionEvent actionEvent) {
        if (null != selectedTemplate) {
            boolean canCommit = false;
            if (templateUtility.hasDifference(selectedTemplate)) {
                canCommit = true;
            }
            if (canCommit) {
                try {
                    String content = getContent();
                    byte[] output = CompressionUtils.compress(content.getBytes("UTF-8"));
                    try {
                        long maxversion = cftemplateversionService.findMaxVersion(selectedTemplate.getId());
                        templateUtility.setCurrentVersion(maxversion + 1);
                        writeVersion(selectedTemplate.getId(), templateUtility.getCurrentVersion(), output);
                        difference = templateUtility.hasDifference(selectedTemplate);
                        this.templateversionMax = templateUtility.getCurrentVersion();
                        this.selectedtemplateversion = this.templateversionMax;
                        refresh();
                        
                        FacesMessage message = new FacesMessage("Commited " + selectedTemplate.getName() + " Version: " + (maxversion + 1));
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    } catch (NullPointerException npe) {
                        writeVersion(selectedTemplate.getId(), 1, output);
                        templateUtility.setCurrentVersion(1);
                        difference = templateUtility.hasDifference(selectedTemplate);
                        refresh();

                        FacesMessage message = new FacesMessage("Commited " + selectedTemplate.getName() + " Version: " + 1);
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    }
                    sourceindexer.indexTemplate(selectedTemplate);
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage());
                }
            } else {
                difference = templateUtility.hasDifference(selectedTemplate);
                access = true;

                FacesMessage message = new FacesMessage("Could not commit " + selectedTemplate.getName() + " Version: " + 1);
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }
    
    @Override
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
                selectedTemplate.setContent(getContent());
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
    
    @Override
    public void onCheckIn(ActionEvent actionEvent) {
        if (null != selectedTemplate) {
            selectedTemplate.setCheckedoutby(BigInteger.valueOf(0));
            selectedTemplate.setContent(getContent());
            cftemplateService.edit(selectedTemplate);
            difference = templateUtility.hasDifference(selectedTemplate);
            checkedout = false;
            
            FacesMessage message = new FacesMessage("Checked Out " + selectedTemplate.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    @Override
    public void onChangeName(ValueChangeEvent changeEvent) {
        if (!templateName.isBlank()) {
            try {
                cftemplateService.findByName(templateName);
                newButtonDisabled = true;
            } catch (NoResultException ex) {
                newButtonDisabled = templateName.isEmpty();
            }
        } else {
            newButtonDisabled = true;
        }
    }
    
    @Override
    public void onCreate(ActionEvent actionEvent) {
        try {
            if (!templateName.isBlank()) {
                CfTemplate newtemplate = new CfTemplate();
                newtemplate.setName(templateName);
                newtemplate.setContent("//"+templateName);
                newtemplate.setScriptlanguage(templateScriptLanguage);
                newtemplate.setLayout(layout);
                cftemplateService.create(newtemplate);
                templateListe = cftemplateService.findAll();
                templateName = "";
                selectedTemplate = newtemplate;
                refresh();
                onSelect(null);
                onCheckOut(null);
            } else {
                FacesMessage message = new FacesMessage("Please enter template name");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    @Override
    public void onDelete(ActionEvent actionEvent) {
        if (null != selectedTemplate) {
            cftemplateService.delete(selectedTemplate);
            templateListe = cftemplateService.findAll();
            refresh();
            FacesMessage message = new FacesMessage("Deleted " + selectedTemplate.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    @Override
    public void writeVersion(long templateref, long version, byte[] content) {
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
    
    @Override
    public void onVersionSelect(ActionEvent actionEvent) {
        if (null != selectedTemplate) {
            templateUtility.getVersion(version.getCfTemplateversionPK().getTemplateref(), version.getCfTemplateversionPK().getVersion());
        }
    }
    
    @Override
    public void onSlideEnd(SlideEndEvent event) {
        selectedtemplateversion = (int) event.getValue();
        if (selectedtemplateversion <= templateversionMin) {
            selectedtemplateversion = templateversionMin;
        }
        if (selectedtemplateversion >= templateversionMax) {
            selectedtemplateversion = templateversionMax;
        }
    }
   
    @Override
    public void onVersionChanged() {
        if (templateversion <= templateversionMin) {
            templateversion = templateversionMin;
        }
        if (templateversion >= templateversionMax) {
            templateversion = templateversionMax;
        }
        selectedtemplateversion = templateversion;
    }

    @Override
    public void onChange(ActionEvent actionEvent) {
        if (null != selectedTemplate) {
            selectedTemplate.setScriptlanguage(templateScriptLanguage);
            selectedTemplate.setName(templateName);
            selectedTemplate.setLayout(layout);
            cftemplateService.edit(selectedTemplate);
            difference = templateUtility.hasDifference(selectedTemplate);
            refresh();
            
            FacesMessage message = new FacesMessage("Changed " + selectedTemplate.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        } else {
            FacesMessage message = new FacesMessage("No template selected. Nothing changed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
}