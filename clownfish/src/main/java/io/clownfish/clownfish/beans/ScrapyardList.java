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

import com.hazelcast.spring.cache.HazelcastCacheManager;
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
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
import io.clownfish.clownfish.utils.HibernateUtil;
import java.io.Serializable;
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
    @Autowired transient CfAttributService cfattributService;
    @Autowired transient CfListService cflistService;
    @Autowired transient CfListcontentService cflistcontentService;
    @Autowired transient CfSitecontentService cfsitecontentService;
    @Autowired CfClasscontentKeywordService cfclasscontentkeywordService;
    @Autowired CfKeywordService cfkeywordService;
    @Autowired IndexService indexService;
    @Autowired ContentIndexer contentIndexer;
    @Autowired HibernateUtil hibernateUtil;
    
    @Autowired private HazelcastCacheManager cacheManager;
    
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
        memoryeditDatalist = null;
        classcontentlist = cfclasscontentService.findByScrapped(true);
        classlist = cfclassService.findAll();
        assetlist = cfassetService.findAll();
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
        assetlist = cfassetService.findAll();
        assetlist = cfassetService.findAll();
    }
    
    public void onRefreshContent() {
        classcontentlist.clear();
        classcontentlist = cfclasscontentService.findByScrapped(true);
    }
}
