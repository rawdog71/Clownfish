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

import com.google.gson.Gson;
import io.clownfish.clownfish.datamodels.InsertContentParameter;
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
import io.clownfish.clownfish.dbentities.CfKeyword;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.lucene.ContentIndexer;
import io.clownfish.clownfish.lucene.IndexService;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentKeywordService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.serviceinterface.CfSitecontentService;
import io.clownfish.clownfish.utils.FolderUtil;
import io.clownfish.clownfish.utils.PasswordUtil;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
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
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Named("classcontentList")
@Scope("singleton")
@Component
public class ContentList implements Serializable {
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired transient CfAssetService cfassetService;
    @Autowired transient CfAssetlistService cfassetlistService;
    @Autowired transient CfAttributService cfattributService;
    @Autowired transient CfListService cflistService;
    @Autowired transient CfListcontentService cflistcontentService;
    @Autowired transient CfSitecontentService cfsitecontentService;
    @Autowired CfClasscontentKeywordService cfclasscontentkeywordService;
    @Autowired CfKeywordService cfkeywordService;
    @Autowired IndexService indexService;
    @Autowired ContentIndexer contentIndexer;
    @Autowired FolderUtil folderUtil;
    
    private @Getter @Setter List<CfClasscontent> classcontentlist;
    private @Getter @Setter CfClasscontent selectedContent = null;
    private transient @Getter @Setter List<CfAttributcontent> attributcontentlist = null;
    private @Getter @Setter CfAttributcontent selectedAttributContent = null;
    private @Getter @Setter List<CfClasscontent> filteredContent;
    private @Getter @Setter String contentName;
    private @Getter @Setter CfClass selectedClass;
    private transient @Getter @Setter List<CfClass> classlist = null;
    private transient @Getter @Setter List<CfAssetlist> assetlibrarylist = null;
    private @Getter @Setter boolean newContentButtonDisabled = false;
    private @Getter @Setter boolean contentValueBoolean = false;
    private @Getter @Setter Date contentValueDatetime;
    private @Getter @Setter CfAttributcontent selectedAttribut = null;
    private @Getter @Setter long selectedAttributId;
    private @Getter @Setter CfAsset selectedMedia;
    private @Getter @Setter List<CfList> selectedList;
    private @Getter @Setter List<CfAssetlist> selectedAssetList;
    private @Getter @Setter String editContent;
    private @Getter @Setter Date editCalendar;
    private @Getter @Setter CfList editDatalist;
    private @Getter @Setter CfAssetlist editAssetlist;
    private @Getter @Setter boolean isBooleanType;
    private @Getter @Setter boolean isStringType;
    private @Getter @Setter boolean isHashStringType;
    private @Getter @Setter boolean isDatetimeType;
    private @Getter @Setter boolean isIntegerType;
    private @Getter @Setter boolean isRealType;
    private @Getter @Setter boolean isHTMLTextType;
    private @Getter @Setter boolean isTextType;
    private @Getter @Setter boolean isMarkdownType;
    private @Getter @Setter boolean isMediaType;
    private @Getter @Setter boolean isClassrefType;
    private @Getter @Setter boolean isAssetrefType;
    private @Getter @Setter boolean valueBooleanRendered = false;
    private @Getter @Setter boolean valueDatetimeRendered = false;
    private @Getter @Setter DualListModel<CfKeyword> keywords;
    private List<CfKeyword> keywordSource;
    private List<CfKeyword> keywordTarget;
    private List<CfClasscontentkeyword> contentkeywordlist;
    private @Getter @Setter List<CfAsset> assetlist;
    private @Getter @Setter String contentJson;
    
    final transient Logger logger = LoggerFactory.getLogger(ContentList.class);

    public boolean renderSelected(CfAttributcontent attribut) {
        if (selectedAttribut != null) {
            if (selectedAttribut.getAttributref().getAutoincrementor()) {
                return false;
            } else {
                return attribut.getId() == selectedAttribut.getId();
            }
        } else {
            return false;
        }
    }
    
    @PostConstruct
    public void init() {
        classcontentlist = cfclasscontentService.findAll();
        classlist = cfclassService.findAll();
        assetlist = cfassetService.findAll();
        selectedAssetList = cfassetlistService.findAll();
        editContent = "";
        
        keywordSource = cfkeywordService.findAll();
        keywordTarget = new ArrayList<>();
        
        keywords = new DualListModel<>(keywordSource, keywordTarget);
    }
    
    public void initAssetlist() {
        assetlist = cfassetService.findAll();
    }
    
    public void onSelect(SelectEvent event) {
        selectedContent = (CfClasscontent) event.getObject();
        attributcontentlist = cfattributcontentService.findByClasscontentref(selectedContent);
       
        contentName = selectedContent.getName();
        selectedClass = selectedContent.getClassref();
        newContentButtonDisabled = true;
        
        keywords.getTarget().clear();
        keywords.getSource().clear();
        keywords.setSource(cfkeywordService.findAll());
        contentkeywordlist = cfclasscontentkeywordService.findByClassContentRef(selectedContent.getId());
        for (CfClasscontentkeyword contentkeyword : contentkeywordlist) {
            CfKeyword kw = cfkeywordService.findById(contentkeyword.getCfClasscontentkeywordPK().getKeywordref());
            keywords.getTarget().add(kw);
            keywords.getSource().remove(kw);
        }
    }
    
    public void onSelectAttribut(SelectEvent event) {
        selectedAttribut = (CfAttributcontent) event.getObject();
        selectedAttributId = selectedAttribut.getId();
        selectedMedia = null;
        
        isBooleanType = false;
        isStringType = false;
        isHashStringType = false;
        isIntegerType = false;
        isRealType = false;
        isHTMLTextType = false;
        isTextType = false;
        isMarkdownType = false;
        isDatetimeType = false;
        isMediaType = false;
        isClassrefType = false;
        isAssetrefType = false;
        
        switch (selectedAttribut.getAttributref().getAttributetype().getName()) {
            case "boolean":
                isBooleanType = true;
                break;
            case "string":
                isStringType = true;
                break;
            case "hashstring":
                isHashStringType = true;
                break;    
            case "integer":
                isIntegerType = true;
                break;
            case "real":
                isRealType = true;
                break;
            case "htmltext":
                isHTMLTextType = true;
                break;    
            case "text":
                isTextType = true;
                break;
            case "markdown":
                isMarkdownType = true;
                break;    
            case "datetime":
                isDatetimeType = true;
                editCalendar = selectedAttribut.getContentDate();
                break;
            case "media":
                isMediaType = true;
                if (selectedAttribut.getContentInteger() != null) {
                    selectedMedia = cfassetService.findById(selectedAttribut.getContentInteger().longValue());
                }
                break;
            case "classref":
                isClassrefType = true;
                editDatalist = null;
                CfClass ref = selectedAttribut.getAttributref().getRelationref();
                selectedList = cflistService.findByClassref(ref);
                if (selectedAttribut.getClasscontentlistref() != null) {
                    editDatalist = cflistService.findById(selectedAttribut.getClasscontentlistref().getId());
                }
                break;
            case "assetref":
                isAssetrefType = true;
                editAssetlist = null;
                if (selectedAttribut.getAssetcontentlistref() != null) {
                    editAssetlist = cfassetlistService.findById(selectedAttribut.getAssetcontentlistref().getId());
                }
                break;    
        }
        editContent = selectedAttribut.toString();
    }
    
    public void onCreateContent(ActionEvent actionEvent) {
        try {
            CfClasscontent newclasscontent = new CfClasscontent();
            contentName = contentName.replaceAll("\\s+", "_");
            newclasscontent.setName(contentName);
            newclasscontent.setClassref(selectedClass);
            cfclasscontentService.create(newclasscontent);
            
            List<CfAttribut> attributlist = cfattributService.findByClassref(newclasscontent.getClassref());
            attributlist.stream().forEach((attribut) -> {
                if (attribut.getAutoincrementor() == true) {
                    List<CfClasscontent> classcontentlist2 = cfclasscontentService.findByClassref(newclasscontent.getClassref());
                    long max = 0;
                    for (CfClasscontent classcontent : classcontentlist2) {
                        try {
                            CfAttributcontent attributcontent = cfattributcontentService.findByAttributrefAndClasscontentref(attribut, classcontent);
                            if (attributcontent.getContentInteger().longValue() > max) {
                                max = attributcontent.getContentInteger().longValue();
                            }
                        } catch (javax.persistence.NoResultException ex) {
                            logger.error(ex.getMessage());
                        }    
                    }
                    CfAttributcontent newcontent = new CfAttributcontent();
                    newcontent.setAttributref(attribut);
                    newcontent.setClasscontentref(newclasscontent);
                    newcontent.setContentInteger(BigInteger.valueOf(max+1));
                    cfattributcontentService.create(newcontent);
                } else {
                    CfAttributcontent newcontent = new CfAttributcontent();
                    newcontent.setAttributref(attribut);
                    newcontent.setClasscontentref(newclasscontent);
                    cfattributcontentService.create(newcontent);
                }
            });
            classcontentlist.clear();
            classcontentlist = cfclasscontentService.findAll();
        } catch (ConstraintViolationException ex) {
            logger.error(ex.getMessage());
        }
    }
    
    /**
     * Handles the content scrapping
     * Sets the scrapped flag to indicate the content is on the scrapyard
     * @param actionEvent
     */
    public void onScrappContent(ActionEvent actionEvent) {
        if (selectedContent != null) {
            selectedContent.setScrapped(true);
            cfclasscontentService.edit(selectedContent);
            classcontentlist = cfclasscontentService.findAll();
            FacesMessage message = new FacesMessage("Succesful", selectedContent.getName() + " has been scrapped.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    /**
     * Handles the content recycling
     * Sets the scrapped flag to indicate the content is recycled from the scrapyard
     */
    public void onRecycle() {
        selectedContent.setScrapped(true);
        cfclasscontentService.edit(selectedContent);
        classcontentlist = cfclasscontentService.findAll();
        FacesMessage message = new FacesMessage("Succesful", selectedContent.getName() + " has been recycled.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    public void onDeleteContent(ActionEvent actionEvent) {
        if (selectedContent != null) {
            // Delete corresponding attributcontent entries
            List<CfAttributcontent> attributcontentlistdummy = cfattributcontentService.findByClasscontentref(selectedContent);
            for (CfAttributcontent attributcontent : attributcontentlistdummy) {
                cfattributcontentService.delete(attributcontent);
            }
            
            // Delete corresponding listcontent entries
            List<CfListcontent> selectedcontent = cflistcontentService.findByClasscontentref(selectedContent.getId());
            for (CfListcontent listcontent : selectedcontent) {
                cflistcontentService.delete(listcontent);
            }
            
            // Delete corresponding keywordcontent entries
            List<CfClasscontentkeyword> keywordcontentdummy = cfclasscontentkeywordService.findByClassContentRef(selectedContent.getId());
            for (CfClasscontentkeyword keywordcontent : keywordcontentdummy) {
                cfclasscontentkeywordService.delete(keywordcontent);
            }
            
            // Delete corresponding sitecontent entries
            List<CfSitecontent> sitecontentdummy = cfsitecontentService.findByClasscontentref(selectedContent.getId());
            for (CfSitecontent sitecontent : sitecontentdummy) {
                cfsitecontentService.delete(sitecontent);
            }
            
            cfclasscontentService.delete(selectedContent);
            classcontentlist = cfclasscontentService.findAll();
        }
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            cfclasscontentService.findByName(contentName);
            newContentButtonDisabled = true;
        } catch (NoResultException ex) {
            newContentButtonDisabled = contentName.isEmpty();
        }
    }
    
    public void onEditAttribut(ActionEvent actionEvent) {
        selectedAttribut.setSalt(null);
        switch (selectedAttribut.getAttributref().getAttributetype().getName()) {
            case "boolean":
                selectedAttribut.setContentBoolean(Boolean.valueOf(editContent));
                break;
            case "string":
                if (selectedAttribut.getAttributref().getIdentity() == true) {
                    List<CfClasscontent> classcontentlist2 = cfclasscontentService.findByClassref(selectedAttribut.getClasscontentref().getClassref());
                    boolean found = false;
                    for (CfClasscontent classcontent : classcontentlist2) {
                        try {
                            CfAttributcontent attributcontent = cfattributcontentService.findByAttributrefAndClasscontentref(selectedAttribut.getAttributref(), classcontent);
                            if (attributcontent.getContentString().compareToIgnoreCase(editContent) == 0) {
                                found = true;
                            }
                        } catch (javax.persistence.NoResultException | NullPointerException ex) {
                            logger.error(ex.getMessage());
                        }
                    }
                    if (!found) {
                        selectedAttribut.setContentString(editContent);
                    }
                } else {
                    selectedAttribut.setContentString(editContent);
                }
                break;
            case "hashstring":
                String salt = PasswordUtil.getSalt(30);
                selectedAttribut.setContentString(PasswordUtil.generateSecurePassword(editContent, salt));
                selectedAttribut.setSalt(salt);
                break;    
            case "integer":
                selectedAttribut.setContentInteger(BigInteger.valueOf(Long.parseLong(editContent)));
                break;
            case "real":
                selectedAttribut.setContentReal(Double.parseDouble(editContent));
                break;
            case "htmltext":
                selectedAttribut.setContentText(editContent);
                break;    
            case "text":
                selectedAttribut.setContentText(editContent);
                break;
            case "markdown":
                selectedAttribut.setContentText(editContent);
                break;    
            case "datetime":
                selectedAttribut.setContentDate(editCalendar);
                break;
            case "media":
                if (null != selectedMedia) {
                    selectedAttribut.setContentInteger(BigInteger.valueOf(selectedMedia.getId()));
                } else {
                    selectedAttribut.setContentInteger(null);
                }
                break;
            case "classref":
                selectedAttribut.setClasscontentlistref(editDatalist);
                break;
            case "assetref":
                selectedAttribut.setAssetcontentlistref(editAssetlist);
                break;    
        }
        selectedAttribut.setIndexed(false);
        cfattributcontentService.edit(selectedAttribut);
        // Index the changed content and merge the Index files
        if ((null != folderUtil.getIndex_folder()) && (!folderUtil.getMedia_folder().isEmpty())) {
            try {
                contentIndexer.run();
                indexService.getWriter().commit();
                indexService.getWriter().forceMerge(10);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(ContentList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void onAttach(ActionEvent actionEvent) {
        contentkeywordlist = cfclasscontentkeywordService.findByClassContentRef(selectedContent.getId());
        for (CfClasscontentkeyword assetkeyword : contentkeywordlist) {
            cfclasscontentkeywordService.delete(assetkeyword);
        }
        List<CfKeyword> selectedkeyword = keywords.getTarget();
        try {
            for (Object keyword : selectedkeyword) {
                CfClasscontentkeyword assetkeyword = new CfClasscontentkeyword(selectedContent.getId(), ((CfKeyword)keyword).getId());
                cfclasscontentkeywordService.create(assetkeyword);
            }
        } catch (ConstraintViolationException ex) {
            logger.error(ex.getMessage());
        }
    }
    
    public void onRefreshAll() {
        classcontentlist = cfclasscontentService.findAll();
        classlist = cfclassService.findAll();
        assetlist = cfassetService.findAll();
        keywordSource = cfkeywordService.findAll();
        assetlist = cfassetService.findAll();
    }
    
    public void onRefreshContent() {
        classcontentlist.clear();
        classcontentlist = cfclasscontentService.findAll();
    }
    
    public void jsonExport() {
        InsertContentParameter contentparameter = new InsertContentParameter();
        contentparameter.setClassname(selectedContent.getClassref().getName());
        contentparameter.setContentname(selectedContent.getName());
        for (CfAttributcontent attributcontent : attributcontentlist) {
            switch (attributcontent.getAttributref().getAttributetype().getName()) {
                case "boolean":
                    if (null != attributcontent.getContentBoolean()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentBoolean().toString());
                    }
                    break;
                case "string":
                    if (null != attributcontent.getContentString()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentString());
                    }
                    break;
                case "hashstring":
                    if (null != attributcontent.getContentString()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentString());
                    }
                    break;    
                case "integer":
                    if (null != attributcontent.getContentInteger()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentInteger().toString());
                    }
                    break;
                case "real":
                    if (null != attributcontent.getContentReal()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentReal().toString());
                    }
                    break;
                case "htmltext":
                    if (null != attributcontent.getContentText()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentText());
                    }
                    break;    
                case "text":
                    if (null != attributcontent.getContentText()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentText());
                    }
                    break;
                case "markdown":
                    if (null != attributcontent.getContentText()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentText());
                    }
                    break;    
                case "datetime":
                    if (null != attributcontent.getContentDate()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getContentDate().toString());
                    }
                    break;
                case "media":
                    if (null != attributcontent.getContentInteger()) {
                        CfAsset asset = cfassetService.findById(attributcontent.getContentInteger().longValue());
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), asset.getName());
                    }
                    break;
                case "classref":
                    if (null != attributcontent.getClasscontentref()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getClasscontentref().getName());
                    }
                    break;
                case "assetref":
                    if (null != attributcontent.getAssetcontentlistref()) {
                        contentparameter.getAttributmap().put(attributcontent.getAttributref().getName(), attributcontent.getAssetcontentlistref().getName());
                    }
                    break;    
            }
        }
        Gson gson = new Gson();
        contentJson = gson.toJson(contentparameter);
    }
}
