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

import io.clownfish.clownfish.constants.ClownfishConst.JavascriptTypes;
import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfJavascriptversion;
import io.clownfish.clownfish.dbentities.CfJavascriptversionPK;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.lucene.IndexService;
import io.clownfish.clownfish.lucene.SourceIndexer;
import io.clownfish.clownfish.serviceinterface.CfJavascriptService;
import io.clownfish.clownfish.serviceinterface.CfJavascriptversionService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.utils.CheckoutUtil;
import io.clownfish.clownfish.utils.ClownfishUtil;
import io.clownfish.clownfish.utils.CompressionUtils;
import io.clownfish.clownfish.utils.FolderUtil;
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
import jakarta.validation.ConstraintViolationException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.primefaces.event.SlideEndEvent;
import org.primefaces.extensions.model.monacoeditor.DiffEditorOptions;
import org.primefaces.extensions.model.monacoeditor.EScrollbarHorizontal;
import org.primefaces.extensions.model.monacoeditor.EScrollbarVertical;
import org.primefaces.extensions.model.monacoeditor.ETheme;
import org.primefaces.extensions.model.monacoeditor.EditorOptions;
import org.primefaces.extensions.model.monacoeditor.EditorScrollbarOptions;
import org.primefaces.extensions.model.monaco.MonacoDiffEditorModel;
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
@Scope("session")
@Component
public class JavascriptList implements ISourceContentInterface {
    @Inject
    LoginBean loginbean;
    @Autowired CfJavascriptService cfjavascriptService;
    @Autowired CfJavascriptversionService cfjavascriptversionService;
    @Autowired transient CfSiteService cfsiteService;
    
    private @Getter @Setter List<CfJavascript> javascriptListe;
    private @Getter @Setter List<CfJavascript> invisJavascriptList;
    private @Getter @Setter CfJavascript selectedJavascript = null;
    private @Getter @Setter String javascriptName = "";
    private @Getter @Setter boolean newButtonDisabled = true;
    private @Getter @Setter CfJavascriptversion version = null;
    private @Getter @Setter long javascriptversion = 0;
    private @Getter @Setter long javascriptversionMin = 0;
    private @Getter @Setter long javascriptversionMax = 0;
    private @Getter @Setter long selectedjavascriptversion = 0;
    private @Getter @Setter List<CfJavascriptversion> versionlist;
    private @Getter @Setter boolean difference;
    private @Getter @Setter long checkedoutby = 0;
    private @Getter @Setter boolean checkedout;
    private @Getter @Setter boolean access;
    private @Getter @Setter EditorOptions editorOptions;
    private @Getter @Setter boolean showDiff;
    private @Getter @Setter DiffEditorOptions editorOptionsDiff;
    private @Getter @Setter MonacoDiffEditorModel contentDiff;
    @Autowired private @Getter @Setter JavascriptUtil javascriptUtility;
    @Autowired @Getter @Setter IndexService indexService;
    @Autowired @Getter @Setter SourceIndexer sourceindexer;
    @Autowired private FolderUtil folderUtil;
    private @Getter @Setter SiteTreeBean sitetree;
    private @Getter @Setter boolean invisible;
    private @Getter @Setter int javascripttype;
    private @Getter @Setter HashMap<Integer, String> availableLanguages = null;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(JavascriptList.class);

    public JavascriptList() {
    }

    public HashMap<Integer, String> getJavascriptTypes() {
        return availableLanguages;
    }
    
    @Override
    public String getContent() {
        if (null != selectedJavascript) {
            if (selectedjavascriptversion != javascriptversionMax) {
                return javascriptUtility.getVersion(selectedJavascript.getId(), selectedjavascriptversion);
            } else {
                javascriptUtility.setJavascriptContent(selectedJavascript.getContent());
                return javascriptUtility.getJavascriptContent();
            }
        } else {
            return "";
        }
    }

    @Override
    public void setContent(String content) {
        if (null != selectedJavascript) {
            selectedJavascript.setContent(content);
        }
    }

    @PostConstruct
    @Override
    public void init() {
        LOGGER.info("INIT JAVASCRIPT START");
        if (null == availableLanguages) {
            availableLanguages = new HashMap<>();
            availableLanguages.put(JavascriptTypes.TYPE_JS.getValue(), "Javascript");
            if (checkTypescript()) {
                availableLanguages.put(JavascriptTypes.TYPE_TS.getValue(), "Typescript");
            }
        }
        try {
            sourceindexer.initJavascript(cfjavascriptService, indexService);
        } catch (IOException ex) {
            
        }
        difference = false;
        showDiff = false;
        javascriptName = "";
        javascriptListe = cfjavascriptService.findAll();
        invisJavascriptList = cfjavascriptService.findAll().stream()
                .filter((cfJavascript -> !cfJavascript.getInvisible())).collect(Collectors.toList());
        javascriptUtility.setJavascriptContent("");
        checkedout = false;
        access = false;
        javascripttype = JavascriptTypes.TYPE_JS.getValue();
        editorOptions = new EditorOptions();
        editorOptions.setLanguage(availableLanguages.get(JavascriptTypes.TYPE_JS.getValue()));
        editorOptions.setTheme(ETheme.VS_DARK);
        editorOptions.setScrollbar(new EditorScrollbarOptions().setVertical(EScrollbarVertical.VISIBLE).setHorizontal(EScrollbarHorizontal.VISIBLE));
        editorOptionsDiff = new DiffEditorOptions();
        editorOptionsDiff.setTheme(ETheme.VS_DARK);
        editorOptionsDiff.setScrollbar(new EditorScrollbarOptions().setVertical(EScrollbarVertical.VISIBLE).setHorizontal(EScrollbarHorizontal.VISIBLE));
        LOGGER.info("INIT JAVASCRIPT END");
    }
    
    @Override
    public void refresh() {
        javascriptListe = cfjavascriptService.findAll();
        invisJavascriptList = cfjavascriptService.findAll().stream()
                .filter((cfJavascript -> !cfJavascript.getInvisible())).collect(Collectors.toList());
        if (null != sitetree) {
            sitetree.onRefreshSelection();
        }
    }
    
    public List<CfJavascript> completeText(String query) {
        String queryLowerCase = query.toLowerCase();

        return javascriptListe.stream().filter(t -> t.getName().toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }

    public List<CfJavascript> completeInvisText(String query) {
        String queryLowerCase = query.toLowerCase();

        return invisJavascriptList.stream().filter(t -> t.getName().toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }
    
    @Override
    public void onSelect(AjaxBehaviorEvent event) {
        selectJavascript(selectedJavascript);
        setInvisible(selectedJavascript.getInvisible());
    }
    
    @Override
    public void onSave(ActionEvent actionEvent) {
        if (null != selectedJavascript) {
            selectedJavascript.setContent(getContent());
            selectedJavascript.setInvisible(invisible);
            selectedJavascript.setType(javascripttype);
            cfjavascriptService.edit(selectedJavascript);
            difference = javascriptUtility.hasDifference(selectedJavascript);
            
            FacesMessage message = new FacesMessage("Saved " + selectedJavascript.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    @Override
    public void onCommit(ActionEvent actionEvent) {
        if (null != selectedJavascript) {
            boolean canCommit = false;
            if (javascriptUtility.hasDifference(selectedJavascript)) {
                canCommit = true;
            }
            if (canCommit) {
                try {
                    String content = getContent();
                    byte[] output = CompressionUtils.compress(content.getBytes("UTF-8"));
                    try {
                        long maxversion = cfjavascriptversionService.findMaxVersion(selectedJavascript.getId());
                        javascriptUtility.setCurrentVersion(maxversion + 1);
                        writeVersion(selectedJavascript.getId(), javascriptUtility.getCurrentVersion(), output);
                        difference = javascriptUtility.hasDifference(selectedJavascript);
                        this.javascriptversionMax = javascriptUtility.getCurrentVersion();
                        this.selectedjavascriptversion = this.javascriptversionMax;
                        if (0 == selectedJavascript.getType()) {
                            writeStaticJS(selectedJavascript.getName(), content);
                        }
                        new File(folderUtil.getJs_folder() + File.separator + selectedJavascript.getName() + "_ts.js").delete();
                        FacesMessage message = new FacesMessage("Commited " + selectedJavascript.getName() + " Version: " + (maxversion + 1));
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    } catch (NullPointerException npe) {
                        writeVersion(selectedJavascript.getId(), 1, output);
                        javascriptUtility.setCurrentVersion(1);
                        difference = javascriptUtility.hasDifference(selectedJavascript);
                        if (0 == selectedJavascript.getType()) {
                            writeStaticJS(selectedJavascript.getName(), content);
                        }
                        new File(folderUtil.getJs_folder() + File.separator + selectedJavascript.getName() + "_ts.js").delete();
                        FacesMessage message = new FacesMessage("Commited " + selectedJavascript.getName() + " Version: " + 1);
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    }
                    sourceindexer.indexJavascript(selectedJavascript);
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
    
    @Override
    public void onCheckIn(ActionEvent actionEvent) {
        if (null != selectedJavascript) {
            selectedJavascript.setCheckedoutby(BigInteger.valueOf(0));
            selectedJavascript.setContent(getContent());
            cfjavascriptService.edit(selectedJavascript);
            
            difference = javascriptUtility.hasDifference(selectedJavascript);
            checkedout = false;
            
            FacesMessage message = new FacesMessage("Checked In " + selectedJavascript.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    @Override
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
                selectedJavascript.setContent(getContent());
                cfjavascriptService.edit(selectedJavascript);
                difference = javascriptUtility.hasDifference(selectedJavascript);
                checkedout = true;
                showDiff = false;

                FacesMessage message = new FacesMessage("Checked Out " + selectedJavascript.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else {
                access = false;
                FacesMessage message = new FacesMessage("could not Checked Out " + selectedJavascript.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }
    
    @Override
    public void onChangeName(ValueChangeEvent changeEvent) {
        if (!javascriptName.isBlank()) {
            CfJavascript newjavascript = cfjavascriptService.findByName(javascriptName);
            if (null == newjavascript) {
                newButtonDisabled = false;
            } else {
                newButtonDisabled = !javascriptName.isEmpty();
            }
        } else {
            newButtonDisabled = true;
        }
    }
    
    @Override
    public void onCreate(ActionEvent actionEvent) {
        try {
            if (!javascriptName.isBlank()) {
                CfJavascript newjavascript = new CfJavascript();
                newjavascript.setName(javascriptName.replaceAll("[^a-zA-Z0-9]", "_"));
                newjavascript.setContent("//"+javascriptName);
                if (loginbean.getCfuser().getSuperadmin()) {
                    newjavascript.setInvisible(invisible);
                } else {
                    newjavascript.setInvisible(false);
                }
                newjavascript.setType(javascripttype);
                cfjavascriptService.create(newjavascript);
                javascriptListe = cfjavascriptService.findAll();
                invisJavascriptList = cfjavascriptService.findAll().stream()
                        .filter((cfJavascript -> !cfJavascript.getInvisible())).collect(Collectors.toList());
                javascriptName = "";
                selectedJavascript = newjavascript;
                onSelect(null);
                onCheckOut(null);
            } else {
                FacesMessage message = new FacesMessage("Please enter javascript name");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    @Override
    public void onDelete(ActionEvent actionEvent) {
        if (null != selectedJavascript) {
            List<CfSite> sites = cfsiteService.findByJavascriptref(selectedJavascript);
            for (CfSite site : sites) {
                site.setJavascriptref(null);
                cfsiteService.edit(site);
            }
            sitetree.loadTree();
            cfjavascriptService.delete(selectedJavascript);
            javascriptListe = cfjavascriptService.findAll();
            invisJavascriptList = cfjavascriptService.findAll().stream()
                    .filter((cfJavascript -> !cfJavascript.getInvisible())).collect(Collectors.toList());
            
            FacesMessage message = new FacesMessage("Deleted " + selectedJavascript.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    @Override
    public void writeVersion(long javascriptref, long version, byte[] content) {
        CfJavascriptversionPK javascriptversionpk = new CfJavascriptversionPK();
        javascriptversionpk.setJavascriptref(javascriptref);
        javascriptversionpk.setVersion(version);

        CfJavascriptversion cfjavascriptversion = new CfJavascriptversion();
        cfjavascriptversion.setCfJavascriptversionPK(javascriptversionpk);
        cfjavascriptversion.setContent(content);
        cfjavascriptversion.setTstamp(new Date());
        cfjavascriptversion.setCommitedby(BigInteger.valueOf(loginbean.getCfuser().getId()));
        cfjavascriptversionService.create(cfjavascriptversion);
    }
    
    @Override
    public void onVersionSelect(ActionEvent actionEvent) {
        if (null != selectedJavascript) {
            javascriptUtility.getVersion(version.getCfJavascriptversionPK().getJavascriptref(), version.getCfJavascriptversionPK().getVersion());
        }
    }

    @Override
    public void onSlideEnd(SlideEndEvent event) {
        selectedjavascriptversion = (int) event.getValue();
        if (selectedjavascriptversion <= javascriptversionMin) {
            selectedjavascriptversion = javascriptversionMin;
        }
        if (selectedjavascriptversion >= javascriptversionMax) {
            selectedjavascriptversion = javascriptversionMax;
        }
        showDiff = (selectedjavascriptversion < javascriptversionMax);
        if (showDiff) {
            contentDiff = new MonacoDiffEditorModel(javascriptUtility.getVersion(selectedJavascript.getId(), selectedjavascriptversion), javascriptUtility.getVersion(selectedJavascript.getId(), javascriptversionMax));
        }
    }
   
    @Override
    public void onVersionChanged() {
        if (javascriptversion <= javascriptversionMin) {
            javascriptversion = javascriptversionMin;
        }
        if (javascriptversion >= javascriptversionMax) {
            javascriptversion = javascriptversionMax;
        }
        selectedjavascriptversion = javascriptversion;
    }

    @Override
    public void onChange(ActionEvent actionEvent) {
        if (null != selectedJavascript) {
            selectedJavascript.setName(javascriptName);
            cfjavascriptService.edit(selectedJavascript);
            difference = javascriptUtility.hasDifference(selectedJavascript);
            refresh();
            
            FacesMessage message = new FacesMessage("Changed " + selectedJavascript.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }  else {
            FacesMessage message = new FacesMessage("No javascript selected. Nothing changed.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    private void writeStaticJS(String filename, String js) {
        javascriptUtility.writeStaticJS(filename, js, "js");
    }
    
    public void selectJavascript(CfJavascript javascript) {
        selectedJavascript = javascript;
        difference = false;
        showDiff = false;
        if (null != selectedJavascript) {
            javascriptName = selectedJavascript.getName();
            javascriptUtility.setJavascriptContent(selectedJavascript.getContent());
            versionlist = cfjavascriptversionService.findByJavascriptref(selectedJavascript.getId());
            difference = javascriptUtility.hasDifference(selectedJavascript);
            BigInteger co = selectedJavascript.getCheckedoutby();
            CheckoutUtil checkoutUtil = new CheckoutUtil();
            checkoutUtil.getCheckoutAccess(co, loginbean);
            javascriptversionMin = 1;
            checkedout = checkoutUtil.isCheckedout();
            access = checkoutUtil.isAccess();
            javascriptversionMax = versionlist.size();
            selectedjavascriptversion = javascriptversionMax;
            setInvisible(selectedJavascript.getInvisible());
            javascripttype = selectedJavascript.getType();
            editorOptions.setLanguage(availableLanguages.get(javascripttype).toLowerCase());
        } else {
            javascriptName = "";
            checkedout = false;
            access = false;
        }
    }

    @Override
    public void onCopy(ActionEvent actionEvent) {
        if (null != selectedJavascript) {
            CfJavascript newjavascript = new CfJavascript();
            String newname = javascriptUtility.getUniqueName(selectedJavascript.getName());
            newjavascript.setName(newname);
            newjavascript.setContent(selectedJavascript.getContent());
            newjavascript.setType(selectedJavascript.getType());
            cfjavascriptService.create(newjavascript);
            javascriptListe = cfjavascriptService.findAll();
            javascriptName = newname;
            selectedJavascript = newjavascript;
            onCommit(null);
            refresh();
            onSelect(null);
            onCheckOut(null);
        }
    }
    
    private boolean checkTypescript() {
        ProcessBuilder builder = new ProcessBuilder();
        try {
            if (ClownfishUtil.isWindows()) {
                builder.command("cmd.exe", "/c", "tsc" + " -version");
            } else {
                builder.command("sh", "-c", "tsc" + " -version");
            }
            builder.redirectErrorStream(true);
            String result = IOUtils.toString(builder.start().getInputStream(), StandardCharsets.UTF_8);

            if (result.contains("Version ")) {
                LOGGER.info("TypeScript Compiler TYPESCRIPT -> TRUE");
                return true;
            } else {
                LOGGER.info("TypeScript Compiler TYPESCRIPT -> FALSE");
                return false;
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
            return false;
        }
    }
}