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

import io.clownfish.clownfish.dbentities.*;
import io.clownfish.clownfish.serviceinterface.*;
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;

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
@Named("scrapyardList")
@Scope("singleton")
@Component
public class ScrapyardList implements Serializable {
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired transient CfAssetService cfassetService;
    @Autowired transient CfAssetlistService cfassetlistService;
    @Autowired CfAssetlistcontentService cfassetlistcontentService;
    @Autowired transient CfListcontentService cflistcontentService;
    @Autowired transient CfSitecontentService cfsitecontentService;
    @Autowired CfClasscontentKeywordService cfclasscontentkeywordService;
    @Autowired CfAssetKeywordService cfassetkeywordService;
    @Autowired CfKeywordService cfkeywordService;
    @Autowired HibernateUtil hibernateUtil;
    @Autowired private ContentUtil contentUtil;
    
    private @Getter @Setter List<CfClasscontent> classcontentlist;
    private @Getter @Setter CfClasscontent selectedContent = null;
    private @Getter @Setter CfAsset selectedAsset = null;
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
    private @Getter @Setter CfList memoryeditDatalist;
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
    private @Getter @Setter List<CfAsset> assetlist;
    private @Getter @Setter String contentJson;

    private @Getter @Setter String menuStatus;
    private @Getter @Setter boolean renderContent;
    private @Getter @Setter boolean isImage;
    private @Getter @Setter boolean isPdf;
    private @Getter @Setter String assetpublicusage;
    private @Getter @Setter List<String> keywords;
    private @Getter @Setter List<CfAssetkeyword> assetkeywordlist;
    private @Getter @Setter boolean renderDetail;
    private @Getter @Setter String description = "";
    
    final transient Logger LOGGER = LoggerFactory.getLogger(ScrapyardList.class);

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
        LOGGER.info("INIT SCRAPYARDLIST START");
        menuStatus = "Content";
        renderContent = true;
        memoryeditDatalist = null;
        classcontentlist = cfclasscontentService.findByScrapped(true);
        classlist = cfclassService.findAll();

        description = "Empty";
        assetlist = cfassetService.findByScrapped(true);
        keywords = new ArrayList<>();
        renderDetail = false;
        selectedAssetList = cfassetlistService.findAll();
        editContent = "";
        LOGGER.info("INIT SCRAPYARDLIST END");
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
    }

    public void onSelectAsset(SelectEvent<CfAsset> event) {
        selectedAsset = event.getObject();
    }

    public void onAssetDetail() {
        if (selectedAsset == null)
            return;
        isImage = selectedAsset.getMimetype().contains("jpeg");
        description = selectedAsset.getDescription() == null ? "Empty" : selectedAsset.getDescription();
        keywords.clear();
        renderDetail = true;
        assetpublicusage = selectedAsset.isPublicuse() ? "Yes" : "No";
        assetkeywordlist = cfassetkeywordService.findByAssetRef(selectedAsset.getId());
        for (CfAssetkeyword assetkeyword : assetkeywordlist) {
            CfKeyword kw = cfkeywordService.findById(assetkeyword.getCfAssetkeywordPK().getKeywordref());
            keywords.add(kw.getName());
        }
        isImage = selectedAsset.getMimetype().contains("jpeg") || selectedAsset.getMimetype().contains("png");
        isPdf = selectedAsset.getMimetype().contains("pdf") || selectedAsset.getMimetype().contains("octet-stream");
    }
        
    /**
     * Handles the content recycling
     * Sets the scrapped flag to indicate the content is recycled from the scrapyard
     */
    public void onRecycle() {
        selectedContent.setScrapped(false);
        cfclasscontentService.edit(selectedContent);
        try {
            hibernateUtil.updateContent(selectedContent);
        } catch (javax.persistence.NoResultException ex) {
            LOGGER.warn(ex.getMessage());
        }
        classcontentlist = cfclasscontentService.findByScrapped(true);
        FacesMessage message = new FacesMessage("Succesful", selectedContent.getName() + " has been recycled.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void onRecycleAsset() {
        selectedAsset.setScrapped(false);
        cfassetService.edit(selectedAsset);
        assetlist = cfassetService.findByScrapped(true);
        FacesMessage message = new FacesMessage("Succesful", selectedAsset.getName() + " has been recycled.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void onDeleteAsset() {
        if (selectedAsset != null) {
            List<CfAssetlist> assetLibraries = cfassetlistService.findAll();
            for (CfAssetlist assetLib : assetLibraries) {
                List<CfAssetlistcontent> assets = cfassetlistcontentService.findByAssetlistref(assetLib.getId());
                for (CfAssetlistcontent asset : assets) {
                    if (cfassetService.findById(asset.getCfAssetlistcontentPK().getAssetref()).equals(selectedAsset)) {
                        cfassetlistcontentService.delete(asset);
                    }
                }
            }
            String name = selectedAsset.getName();
            cfassetService.delete(selectedAsset);
            assetlist = cfassetService.findByScrapped(true);
            renderDetail = false;
            selectedAsset = null;
            FacesMessage message = new FacesMessage("Succesful", name + " has been deleted.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
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
            try {
                hibernateUtil.deleteContent(selectedContent);
            } catch (javax.persistence.NoResultException ex) {
                LOGGER.warn(ex.getMessage());
            }
            classcontentlist = cfclasscontentService.findByScrapped(true);
        }
    }
    
    public void onRefreshAll() {
        classcontentlist = cfclasscontentService.findByScrapped(true);
        classlist = cfclassService.findAll();
        assetlist = cfassetService.findByScrapped(true);
    }

    public void onRefreshAssets() {
        assetlist = cfassetService.findByScrapped(true);
    }
    
    public void onRefreshContent() {
        classcontentlist.clear();
        classcontentlist = cfclasscontentService.findByScrapped(true);
    }

    public void onSelectMenu(SelectEvent event) {
        setMenuStatus((String) event.getObject());
        setRenderContent(menuStatus.equalsIgnoreCase("Content"));
    }
    
    public String toString(CfAttributcontent attributcontent) {
        return contentUtil.toString(attributcontent);
    }
}
