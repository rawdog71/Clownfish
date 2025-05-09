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

import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.dbentities.CfTemplateversion;
import io.clownfish.clownfish.dbentities.CfTemplateversionPK;
import io.clownfish.clownfish.lucene.IndexService;
import io.clownfish.clownfish.lucene.SourceIndexer;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
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
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SlideEndEvent;
import org.primefaces.extensions.model.monacoeditor.DiffEditorOptions;
import org.primefaces.extensions.model.monacoeditor.EScrollbarHorizontal;
import org.primefaces.extensions.model.monacoeditor.EScrollbarVertical;
import org.primefaces.extensions.model.monacoeditor.ETheme;
import org.primefaces.extensions.model.monacoeditor.EditorOptions;
import org.primefaces.extensions.model.monacoeditor.EditorScrollbarOptions;
import org.primefaces.extensions.model.monaco.MonacoDiffEditorModel;
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
public class TemplateList implements ISourceContentInterface {
    @Inject
    LoginBean loginbean;
    @Autowired CfTemplateService cftemplateService;
    @Autowired CfTemplateversionService cftemplateversionService;
    @Autowired transient CfSiteService cfsiteService;
    
    private @Getter @Setter List<CfTemplate> templateListe;
    private @Getter @Setter List<CfTemplate> invisibleTemplateList;
    private @Getter @Setter List<CfTemplate> notpreviewtemplateListe;
    private @Getter @Setter List<CfTemplate> previewtemplateListe;
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
    private @Getter @Setter int type;
    private @Getter @Setter boolean showDiff;
    private @Getter @Setter EditorOptions editorOptions;
    private @Getter @Setter DiffEditorOptions editorOptionsDiff;
    private @Getter @Setter MonacoDiffEditorModel contentDiff;
    @Autowired private @Getter @Setter TemplateUtil templateUtility;
    @Autowired @Getter @Setter IndexService indexService;
    @Autowired @Getter @Setter SourceIndexer sourceindexer;
    @Autowired private @Getter @Setter ClassList classlist;
    private @Getter @Setter SiteTreeBean sitetree;
    private @Getter @Setter boolean invisible;
    
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
        difference = false;
        showDiff = false;
        templateName = "";
        templateListe = cftemplateService.findAll();
        invisibleTemplateList = cftemplateService.findAll().stream()
                .filter((cfTemplate -> !cfTemplate.getInvisible())).collect(Collectors.toList());
        notpreviewtemplateListe = cftemplateService.findNotPreview();
        previewtemplateListe = cftemplateService.findPreview();
        templateUtility.setTemplateContent("");
        checkedout = false;
        access = false;
        type = 0;
        editorOptions = new EditorOptions();
        editorOptions.setLanguage("");
        editorOptions.setTheme(ETheme.VS_DARK);
        editorOptions.setScrollbar(new EditorScrollbarOptions().setVertical(EScrollbarVertical.VISIBLE).setHorizontal(EScrollbarHorizontal.VISIBLE));
        editorOptionsDiff = new DiffEditorOptions();
        editorOptionsDiff.setTheme(ETheme.VS_DARK);
        editorOptionsDiff.setScrollbar(new EditorScrollbarOptions().setVertical(EScrollbarVertical.VISIBLE).setHorizontal(EScrollbarHorizontal.VISIBLE));
        LOGGER.info("INIT TEMPLATE END");
    }
    
    @Override
    public void refresh() {
        templateListe = cftemplateService.findAll();
        invisibleTemplateList = cftemplateService.findAll().stream()
                .filter((cfTemplate -> !cfTemplate.getInvisible())).collect(Collectors.toList());
        notpreviewtemplateListe = cftemplateService.findNotPreview();
        previewtemplateListe = cftemplateService.findPreview();
        if (null != classlist) {
            classlist.onRefreshSelection();
        }
        if (null != sitetree) {
            sitetree.onRefreshSelection();
        }
    }
    
    public List<CfTemplate> completeTextNoPreview(String query) {
        String queryLowerCase = query.toLowerCase();

        return notpreviewtemplateListe.stream().filter(t -> t.getName().toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }
    
    public List<CfTemplate> completeTextPreview(String query) {
        String queryLowerCase = query.toLowerCase();

        return previewtemplateListe.stream().filter(t -> t.getName().toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }
    
    public List<CfTemplate> completeText(String query) {
        String queryLowerCase = query.toLowerCase();

        return templateListe.stream().filter(t -> t.getName().toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }

    public List<CfTemplate> completeInvisTextNoPreview(String query) {
        String queryLowerCase = query.toLowerCase();

        return notpreviewtemplateListe.stream().filter(t -> {
            return t.getName().toLowerCase().startsWith(queryLowerCase) && t.getInvisible();
        }).collect(Collectors.toList());
    }

    public List<CfTemplate> completeInvisTextPreview(String query) {
        String queryLowerCase = query.toLowerCase();

        return previewtemplateListe.stream().filter(t -> {
            return t.getName().toLowerCase().startsWith(queryLowerCase) && t.getInvisible();
        }).collect(Collectors.toList());
    }

    public List<CfTemplate> completeInvisText(String query) {
        String queryLowerCase = query.toLowerCase();

        return invisibleTemplateList.stream().filter(t -> t.getName().toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }
    
    @Override
    public void onSelect(AjaxBehaviorEvent event) {
        selectTemplate(selectedTemplate);
        setInvisible(selectedTemplate.getInvisible());
    }
    
    @Override
    public void onSave(ActionEvent actionEvent) {
        if (null != selectedTemplate) {
            selectedTemplate.setScriptlanguage(templateScriptLanguage);
            selectedTemplate.setContent(getContent());
            selectedTemplate.setType(type);
            selectedTemplate.setInvisible(invisible);
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
                showDiff = false;

                FacesMessage message = new FacesMessage("Checked Out " + selectedTemplate.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else {
                access = false;
                FacesMessage message = new FacesMessage("Could not Checked Out " + selectedTemplate.getName());
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
            
            FacesMessage message = new FacesMessage("Checked In " + selectedTemplate.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    @Override
    public void onChangeName(ValueChangeEvent changeEvent) {
        if (!templateName.isBlank()) {
            CfTemplate newtemplate = cftemplateService.findByName(templateName);
            if (null == newtemplate) {
                newButtonDisabled = false;
            } else {
                newButtonDisabled = !templateName.isEmpty();
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
                newtemplate.setName(templateName.replaceAll("[^a-zA-Z0-9]", "_"));
                newtemplate.setContent("//"+templateName);
                newtemplate.setScriptlanguage(templateScriptLanguage);
                newtemplate.setType(type);
                if (loginbean.getCfuser().getSuperadmin()) {
                    newtemplate.setInvisible(invisible);
                } else {
                    newtemplate.setInvisible(false);
                }
                cftemplateService.create(newtemplate);
                templateListe = cftemplateService.findAll();
                invisibleTemplateList = cftemplateService.findAll().stream()
                        .filter((cfTemplate -> !cfTemplate.getInvisible())).collect(Collectors.toList());
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
            List<CfSite> sites = cfsiteService.findByTemplateref(selectedTemplate);
            for (CfSite site : sites) {
                site.setTemplateref(null);
                cfsiteService.edit(site);
            }
            sitetree.loadTree();
            cftemplateService.delete(selectedTemplate);
            templateListe = cftemplateService.findAll();
            invisibleTemplateList = cftemplateService.findAll().stream()
                    .filter((cfTemplate -> !cfTemplate.getInvisible())).collect(Collectors.toList());
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
        showDiff = (selectedtemplateversion < templateversionMax);
        if (showDiff) {
            contentDiff = new MonacoDiffEditorModel(templateUtility.getVersion(selectedTemplate.getId(), selectedtemplateversion), templateUtility.getVersion(selectedTemplate.getId(), templateversionMax));
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
            selectedTemplate.setType(type);
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
    
    public void selectDivTemplate(String template) {
        selectTemplate(cftemplateService.findByName(template));
    }
    
    public void selectTemplate(CfTemplate template) {
        selectedTemplate = template;
        difference = false;
        showDiff = false;
        if (null != selectedTemplate) {
            templateName = selectedTemplate.getName();
            templateUtility.setTemplateContent(selectedTemplate.getContent());
            templateScriptLanguage = selectedTemplate.getScriptlanguage();
            switch (selectedTemplate.getScriptlanguage()) {
                case 0:
                    selectedScriptlanguage = "freemarker";
                    editorOptions.setLanguage("freemarker2");
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
            type = selectedTemplate.getType();
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
    public void onCopy(ActionEvent actionEvent) {
        if (null != selectedTemplate) {
            CfTemplate newtemplate = new CfTemplate();
            String newname = templateUtility.getUniqueName(selectedTemplate.getName());
            newtemplate.setName(newname);
            newtemplate.setScriptlanguage(selectedTemplate.getScriptlanguage());
            newtemplate.setType(selectedTemplate.getType());
            newtemplate.setContent(selectedTemplate.getContent());
            cftemplateService.create(newtemplate);
            templateListe = cftemplateService.findAll();
            templateName = newname;
            selectedTemplate = newtemplate;
            onCommit(null);
            refresh();
            onSelect(null);
            onCheckOut(null);
        }
    }
}