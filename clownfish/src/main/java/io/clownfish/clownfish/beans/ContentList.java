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

import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
import io.clownfish.clownfish.dbentities.CfContentversion;
import io.clownfish.clownfish.dbentities.CfContentversionPK;
import io.clownfish.clownfish.dbentities.CfKeyword;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.lucene.ContentIndexer;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentKeywordService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfContentversionService;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.serviceinterface.CfSitecontentService;
import io.clownfish.clownfish.utils.CheckoutUtil;
import io.clownfish.clownfish.utils.ClassUtil;
import io.clownfish.clownfish.utils.ClownfishUtil;
import io.clownfish.clownfish.utils.CompressionUtils;
import io.clownfish.clownfish.utils.ContentUtil;
import io.clownfish.clownfish.utils.EncryptUtil;
import io.clownfish.clownfish.utils.FolderUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import io.clownfish.clownfish.utils.PasswordUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.SlideEndEvent;
import org.primefaces.extensions.model.monacoeditor.EScrollbarHorizontal;
import org.primefaces.extensions.model.monacoeditor.EScrollbarVertical;
import org.primefaces.extensions.model.monacoeditor.ETheme;
import org.primefaces.extensions.model.monacoeditor.EditorOptions;
import org.primefaces.extensions.model.monacoeditor.EditorScrollbarOptions;
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
    @Inject
    LoginBean loginbean;
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
    @Autowired ContentIndexer contentIndexer;
    @Autowired FolderUtil folderUtil;
    @Autowired HibernateUtil hibernateUtil;
    @Autowired private @Getter @Setter ContentUtil contentUtility;
    @Autowired private @Getter @Setter CfContentversionService cfcontentversionService;
    @Autowired ClassUtil classutil;
    @Autowired private PropertyUtil propertyUtil;
    @Autowired private ContentUtil contentUtil;
    
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
    private @Getter @Setter List<CfClasscontent> selectedContentList;
    private @Getter @Setter List<CfAssetlist> selectedAssetList;
    private @Getter @Setter String editContent;
    private @Getter @Setter Date editCalendar;
    private @Getter @Setter CfList editDatalist;
    private @Getter @Setter CfList memoryeditDatalist;
    private @Getter @Setter CfClasscontent editDataContentlist;
    private @Getter @Setter CfClasscontent memoryeditDataContentlist;
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
    private @Getter @Setter EditorOptions editorOptions;
    private @Getter @Setter boolean difference;
    private @Getter @Setter long selectedcontentversion = 0;
    private @Getter @Setter long contentversionMin = 0;
    private @Getter @Setter long contentversionMax = 0;
    private @Getter @Setter boolean checkedout;
    private @Getter @Setter boolean access;
    private @Getter @Setter CfContentversion version = null;
    private @Getter @Setter List<CfContentversion> versionlist;
    private @Getter @Setter String contentpreview;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(ContentList.class);

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
    
    public boolean isRequired(CfAttributcontent attribut) {
        if (selectedAttribut != null) {
            return selectedAttribut.getAttributref().getMandatory();
        } else {
            return false;
        }
    }
    
    public long getMaxlength(CfAttributcontent attribut) {
        if (selectedAttribut != null) {
            if (selectedAttribut.getAttributref().getMax_val()>0) {
                return selectedAttribut.getAttributref().getMax_val();
            } else {
                switch (selectedAttribut.getAttributref().getAttributetype().getName()) {
                    case "string":
                    case "hashstring":
                        return 255;
                    case "text":
                    case "htmltext":
                    case "markdown":
                        return Integer.MAX_VALUE;
                    default:
                        return 255;
                }
            }
        } else {
            return 0;
        }
    }
    
    public long getMinlength(CfAttributcontent attribut) {
        if (selectedAttribut != null) {
            if (selectedAttribut.getAttributref().getMin_val()>0) {
                return selectedAttribut.getAttributref().getMin_val();
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
    
    @PostConstruct
    public void init() {
        LOGGER.info("INIT CONTENTLIST START");
        memoryeditDatalist = null;
        classcontentlist = cfclasscontentService.findByMaintenance(true);
        classlist = cfclassService.findAll();
        classlist.sort((CfClass c1, CfClass c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
        assetlist = cfassetService.findAll();
        selectedAssetList = cfassetlistService.findAll();
        editContent = "";
        contentpreview = "";
        
        keywordSource = cfkeywordService.findAll();
        keywordTarget = new ArrayList<>();
        
        keywords = new DualListModel<>(keywordSource, keywordTarget);
        
        editorOptions = new EditorOptions();
        editorOptions.setLanguage("markdown");
        editorOptions.setTheme(ETheme.VS_DARK);
        editorOptions.setScrollbar(new EditorScrollbarOptions().setVertical(EScrollbarVertical.VISIBLE).setHorizontal(EScrollbarHorizontal.VISIBLE));
        LOGGER.info("INIT CONTENTLIST END");
    }
    
    public void initAssetlist() {
        assetlist = cfassetService.findAll();
    }
    
    public void onSelect(SelectEvent event) {
        selectedContent = (CfClasscontent) event.getObject();
        selectContent(selectedContent);
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
                editorOptions.setLanguage("html");
                break;    
            case "text":
                isTextType = true;
                break;
            case "markdown":
                isMarkdownType = true;
                editorOptions.setLanguage("markdown");
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
                if (0 == selectedAttribut.getAttributref().getRelationtype()) {     // n:m
                    selectedList = cflistService.findByClassref(ref);
                    if (selectedAttribut.getClasscontentlistref() != null) {
                        editDatalist = cflistService.findById(selectedAttribut.getClasscontentlistref().getId());
                        memoryeditDatalist = editDatalist;
                    }
                } else {                                                            // 1:n
                    selectedContentList = cfclasscontentService.findByClassref(ref);
                    if (selectedAttribut.getContentInteger() != null) {
                        editDataContentlist = cfclasscontentService.findById(selectedAttribut.getContentInteger().longValue()); //cflistService.findById(selectedAttribut.getClasscontentlistref().getId());
                        memoryeditDatalist = editDatalist;
                    }
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
        editContent = contentUtil.toString(selectedAttribut);
    }
    
    public void onCreateContent(ActionEvent actionEvent) {
        try {
            CfClasscontent newclasscontent = new CfClasscontent();
            if (contentName.startsWith(selectedClass.getName().toUpperCase() + "_")) {
                contentName = contentName.replaceAll("[^a-zA-Z0-9]", "_");
            } else {
                contentName = selectedClass.getName().toUpperCase() + "_" + contentName.replaceAll("[^a-zA-Z0-9]", "_");
            }
            newclasscontent.setName(contentName);
            newclasscontent.setClassref(selectedClass);
            cfclasscontentService.create(newclasscontent);
            
            List<CfAttribut> attributlist = cfattributService.findByClassref(newclasscontent.getClassref());
            attributlist.stream().forEach((attribut) -> {
                if (attribut.getAutoincrementor() == true) {
                    List<CfClasscontent> classcontentlist2 = cfclasscontentService.findByClassref(newclasscontent.getClassref());
                    long max = 0;
                    int last = classcontentlist2.size();
                    if (1 == last) {
                        max = 0;
                    } else {
                        CfClasscontent classcontent = classcontentlist2.get(last - 2);
                        CfAttributcontent attributcontent = cfattributcontentService.findByAttributrefAndClasscontentref(attribut, classcontent);        
                        if (attributcontent.getContentInteger().longValue() > max) {
                            max = attributcontent.getContentInteger().longValue();
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
                    if ((null != attribut.getDefault_val()) && (!attribut.getDefault_val().isEmpty())) {
                        switch (attribut.getAttributetype().getName()) {
                            case "boolean":
                                newcontent.setContentBoolean(ClownfishUtil.getBoolean(attribut.getDefault_val(), false));
                                break;
                            case "string":
                                newcontent.setContentString(attribut.getDefault_val());
                                break;
                            case "integer":
                                newcontent.setContentInteger(BigInteger.valueOf(Long.parseLong(attribut.getDefault_val())));
                                break;
                            case "real":
                                newcontent.setContentReal(Double.parseDouble(attribut.getDefault_val()));
                                break;
                            case "text":
                            case "htmltext":
                            case "markdown":
                                newcontent.setContentText(attribut.getDefault_val());
                                break;
                        }
                        
                    }
                    cfattributcontentService.create(newcontent);
                }
            });
            hibernateUtil.insertContent(newclasscontent);
            classcontentlist.clear();
            classcontentlist = cfclasscontentService.findByMaintenance(true);
            FacesMessage message = new FacesMessage("Content created");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    public void onCopyContent(ActionEvent actionEvent) {
        if (selectedContent != null) {
            try {
                CfClasscontent newclasscontent = new CfClasscontent();
                String newname = contentUtil.getUniqueName(selectedContent.getName());
                if (newname.startsWith(selectedClass.getName().toUpperCase() + "_")) {
                    newname = newname.replaceAll("[^a-zA-Z0-9]", "_");
                } else {
                    newname = selectedClass.getName().toUpperCase() + "_" + newname.replaceAll("[^a-zA-Z0-9]", "_");
                }
                newclasscontent.setName(newname);
                
                newclasscontent.setClassref(selectedContent.getClassref());
                cfclasscontentService.create(newclasscontent);

                List<CfAttribut> attributlist = cfattributService.findByClassref(newclasscontent.getClassref());
                attributlist.stream().forEach((attribut) -> {
                    if (attribut.getAutoincrementor() == true) {
                        List<CfClasscontent> classcontentlist2 = cfclasscontentService.findByClassref(newclasscontent.getClassref());
                        long max = 0;
                        int last = classcontentlist2.size();
                        if (1 == last) {
                            max = 0;
                        } else {
                            CfClasscontent classcontent = classcontentlist2.get(last - 2);
                            CfAttributcontent attributcontent = cfattributcontentService.findByAttributrefAndClasscontentref(attribut, classcontent);        
                            if (attributcontent.getContentInteger().longValue() > max) {
                                max = attributcontent.getContentInteger().longValue();
                            }
                        }
                        CfAttributcontent newcontent = new CfAttributcontent();
                        newcontent.setAttributref(attribut);
                        newcontent.setClasscontentref(newclasscontent);
                        newcontent.setContentInteger(BigInteger.valueOf(max+1));
                        cfattributcontentService.create(newcontent);
                    } else {
                        CfAttributcontent attributcontent = cfattributcontentService.findByAttributrefAndClasscontentref(attribut, selectedContent); 
                        
                        CfAttributcontent newcontent = new CfAttributcontent();
                        newcontent.setAttributref(attribut);
                        newcontent.setClasscontentref(newclasscontent);
                        newcontent.setAssetcontentlistref(attributcontent.getAssetcontentlistref());
                        newcontent.setClasscontentlistref(attributcontent.getClasscontentlistref());
                        newcontent.setContentBoolean(attributcontent.getContentBoolean());
                        newcontent.setContentDate(attributcontent.getContentDate());
                        newcontent.setContentInteger(attributcontent.getContentInteger());
                        newcontent.setContentReal(attributcontent.getContentReal());
                        newcontent.setContentString(attributcontent.getContentString());
                        newcontent.setContentText(attributcontent.getContentText());
                        newcontent.setIndexed(attributcontent.isIndexed());
                        newcontent.setSalt(attributcontent.getSalt());
                        cfattributcontentService.create(newcontent);
                    }
                });
                hibernateUtil.insertContent(newclasscontent);
                classcontentlist.clear();
                classcontentlist = cfclasscontentService.findByMaintenance(true);
                FacesMessage message = new FacesMessage("Content created");
                FacesContext.getCurrentInstance().addMessage(null, message);
            } catch (ConstraintViolationException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }
    
    public void onChangeContent(ActionEvent actionEvent) {
        if (selectedContent != null) {
            if (contentName.startsWith(selectedClass.getName().toUpperCase() + "_")) {
                contentName = contentName.replaceAll("\\s+", "_");
            } else {
                contentName = selectedClass.getName().toUpperCase() + "_" + contentName.replaceAll("\\s+", "_");
            }
            CfClasscontent findcontent = cfclasscontentService.findByName(contentName);
            if (null != findcontent) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "WARNING", "Contentname already exists!");
                FacesContext.getCurrentInstance().addMessage(null, message);
                contentName = selectedContent.getName();
            } else {
                selectedContent.setName(contentName);
                cfclasscontentService.edit(selectedContent);
                FacesMessage message = new FacesMessage("Contentname changed!");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
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
            
            // Delete from Listcontent - consistency
            List<CfListcontent> listcontent = cflistcontentService.findByClasscontentref(selectedContent.getId());
            for (CfListcontent lc : listcontent) {
                cflistcontentService.delete(lc);
                hibernateUtil.deleteRelation(cflistService.findById(lc.getCfListcontentPK().getListref()), cfclasscontentService.findById(lc.getCfListcontentPK().getClasscontentref()));
            }
            
            // Delete from Sitecontent - consistency
            List<CfSitecontent> sitecontent = cfsitecontentService.findByClasscontentref(selectedContent.getId());
            for (CfSitecontent sc : sitecontent) {
                cfsitecontentService.delete(sc);
            }
            
            try {
                hibernateUtil.updateContent(selectedContent);
            } catch (javax.persistence.NoResultException ex) {
                LOGGER.warn(ex.getMessage());
            }
            //cacheManager.getCache("classcontent").clear();                      // Hazelcast Cache clearing
            classcontentlist = cfclasscontentService.findByMaintenance(true);
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
        try {
            hibernateUtil.updateContent(selectedContent);
        } catch (javax.persistence.NoResultException ex) {
            LOGGER.warn(ex.getMessage());
        }
        classcontentlist = cfclasscontentService.findByMaintenance(true);
        FacesMessage message = new FacesMessage("Succesful", selectedContent.getName() + " has been recycled.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        CfClasscontent newclasscontent = cfclasscontentService.findByName(contentName);
        if (null == newclasscontent) {
            newContentButtonDisabled = false;
        } else {
            newContentButtonDisabled = !contentName.isEmpty();
        }
    }
    
    public void onEditAttribut(ActionEvent actionEvent) {
        selectedAttribut.setSalt(null);
        boolean updateClassref = false;
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
                            LOGGER.error(ex.getMessage());
                        }
                    }
                    if (!found) {
                        selectedAttribut.setContentString(editContent);
                    }
                } else {
                    if (selectedAttribut.getClasscontentref().getClassref().isEncrypted()) {
                        selectedAttribut.setContentString(EncryptUtil.encrypt(editContent, propertyUtil.getPropertyValue("aes_key")));
                    } else {
                        selectedAttribut.setContentString(editContent);
                    }
                }
                break;
            case "hashstring":
                String salt = PasswordUtil.getSalt(30);
                {
                    try {
                        selectedAttribut.setContentString(URLEncoder.encode(PasswordUtil.generateSecurePassword(editContent, salt), StandardCharsets.UTF_8.toString()));
                    } catch (UnsupportedEncodingException ex) {
                         LOGGER.error(ex.getMessage());
                    }
                }
                selectedAttribut.setSalt(salt);
                break;    
            case "integer":
                selectedAttribut.setContentInteger(BigInteger.valueOf(Long.parseLong(editContent)));
                break;
            case "real":
                selectedAttribut.setContentReal(Double.parseDouble(editContent));
                break;
            case "htmltext":
                if (selectedAttribut.getClasscontentref().getClassref().isEncrypted()) {
                    selectedAttribut.setContentText(EncryptUtil.encrypt(editContent, propertyUtil.getPropertyValue("aes_key")));
                } else {
                    selectedAttribut.setContentText(editContent);
                }
                break;    
            case "text":
                if (selectedAttribut.getClasscontentref().getClassref().isEncrypted()) {
                    selectedAttribut.setContentText(EncryptUtil.encrypt(editContent, propertyUtil.getPropertyValue("aes_key")));
                } else {
                    selectedAttribut.setContentText(editContent);
                }
                break;
            case "markdown":
                if (selectedAttribut.getClasscontentref().getClassref().isEncrypted()) {
                    selectedAttribut.setContentText(EncryptUtil.encrypt(editContent, propertyUtil.getPropertyValue("aes_key")));
                } else {
                    selectedAttribut.setContentText(editContent);
                }
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
                if (0 == selectedAttribut.getAttributref().getRelationtype()) {             // n:m
                    selectedAttribut.setClasscontentlistref(editDatalist);
                    updateClassref = true;
                } else {                                                                    // 1:n
                    selectedAttribut.setContentInteger(BigInteger.valueOf(editDataContentlist.getId()));
                }
                break;
            case "assetref":
                selectedAttribut.setAssetcontentlistref(editAssetlist);
                break;    
        }
        selectedAttribut.setIndexed(false);
        if ((updateClassref) && (null == editDatalist)) {
            hibernateUtil.deleteRelation(memoryeditDatalist, selectedAttribut.getClasscontentref());
        }
        cfattributcontentService.edit(selectedAttribut);
        hibernateUtil.updateContent(selectedAttribut.getClasscontentref());
        if (updateClassref) {
            hibernateUtil.updateRelation(editDatalist);
        }
        
        // Index the changed content and merge the Index files
        if ((null != folderUtil.getIndex_folder()) && (!folderUtil.getMedia_folder().isEmpty())) {
            Thread contentindexer_thread = new Thread(contentIndexer);
            contentindexer_thread.start();
            LOGGER.info("CONTENTINDEXER RUN");
        }
        FacesMessage message = new FacesMessage("Value changed");
        FacesContext.getCurrentInstance().addMessage(null, message);
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
            LOGGER.error(ex.getMessage());
        }
    }
    
    public void onRefreshAll() {
        classcontentlist = cfclasscontentService.findByMaintenance(true);
        classlist = cfclassService.findAll();
        assetlist = cfassetService.findAll();
        keywordSource = cfkeywordService.findAll();
        assetlist = cfassetService.findAll();
    }
    
    public void onRefreshContent() {
        classcontentlist.clear();
        classcontentlist = cfclasscontentService.findByMaintenance(true);
    }
    
    public void jsonExport() {
        contentJson = classutil.jsonExport(selectedContent, attributcontentlist);
    }

    public String getContent() {
        if (null != selectedContent) {
            if (selectedcontentversion != contentversionMax) {
                return contentUtility.getVersion(selectedContent.getId(), selectedcontentversion);
            } else {
                contentUtility.setContent(classutil.jsonExport(selectedContent, attributcontentlist));
                return contentUtility.getContent();
            }
        } else {
            return "";
        }
    }
    
    public void onCheckOut(ActionEvent actionEvent) {
        if (null != selectedContent) {
            boolean canCheckout = false;
            CfClasscontent checkcontent = cfclasscontentService.findById(selectedContent.getId());
            BigInteger co = checkcontent.getCheckedoutby();
            if (null != co) {
                if (co.longValue() == 0) {
                    canCheckout = true;
                } 
            } else {
                canCheckout = true;
            }
                    
            if (canCheckout) {
                selectedContent.setCheckedoutby(BigInteger.valueOf(loginbean.getCfuser().getId()));
                
                //selectedContent.setContent(getContent());
                cfclasscontentService.edit(selectedContent);
                difference = contentUtility.hasDifference(selectedContent);
                checkedout = true;
                //showDiff = false;

                FacesMessage message = new FacesMessage("Checked Out " + selectedContent.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else {
                access = false;
                FacesMessage message = new FacesMessage("could not Checked Out " + selectedContent.getName());
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }
    
    public void onCheckIn(ActionEvent actionEvent) {
        if (null != selectedContent) {
            selectedContent.setCheckedoutby(BigInteger.valueOf(0));
            //selectedContent.setContent(getContent());
            cfclasscontentService.edit(selectedContent);
            difference = contentUtility.hasDifference(selectedContent);
            checkedout = false;
            
            FacesMessage message = new FacesMessage("Checked Out " + selectedContent.getName());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onCommit(ActionEvent actionEvent) {
        if (null != selectedContent) {
            boolean canCommit = false;
            
            if (contentUtility.hasDifference(selectedContent)) {
                canCommit = true;
            }
            if (canCommit) {
                try {
                    String content = getContent();
                    byte[] output = CompressionUtils.compress(content.getBytes("UTF-8"));
                    try {
                        long maxversion = cfcontentversionService.findMaxVersion(selectedContent.getId());
                        contentUtility.setCurrentVersion(maxversion + 1);
                        writeVersion(selectedContent.getId(), contentUtility.getCurrentVersion(), output);
                        difference = contentUtility.hasDifference(selectedContent);
                        this.contentversionMax = contentUtility.getCurrentVersion();
                        this.selectedcontentversion = this.contentversionMax;
                        //refresh();
                        
                        FacesMessage message = new FacesMessage("Commited " + selectedContent.getName() + " Version: " + (maxversion + 1));
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    } catch (NullPointerException npe) {
                        writeVersion(selectedContent.getId(), 1, output);
                        contentUtility.setCurrentVersion(1);
                        difference = contentUtility.hasDifference(selectedContent);
                        //refresh();

                        FacesMessage message = new FacesMessage("Commited " + selectedContent.getName() + " Version: " + 1);
                        FacesContext.getCurrentInstance().addMessage(null, message);
                    }
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage());
                }
            } else {
                difference = contentUtility.hasDifference(selectedContent);
                access = true;

                FacesMessage message = new FacesMessage("Could not commit " + selectedContent.getName() + " Version: " + 1);
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
            
            try {
                attributcontentlist = cfattributcontentService.findByClasscontentref(selectedContent);
                String output = selectedContent.getClassref().getTemplateref().getContent();
                for (CfAttributcontent attributcontent : attributcontentlist) {
                    output = output.replaceAll("#" + attributcontent.getAttributref().getName() + "#", contentUtil.toString(attributcontent));
                }
                contentpreview = output;
            } catch (Exception ex) {
                contentpreview = "";
            }
        }
    }
    
    public void writeVersion(long ref, long version, byte[] content) {
        CfContentversionPK contentversionpk = new CfContentversionPK();
        contentversionpk.setContentref(ref);
        contentversionpk.setVersion(version);

        CfContentversion cfcontentversion = new CfContentversion();
        cfcontentversion.setCfContentversionPK(contentversionpk);
        cfcontentversion.setContent(content);
        cfcontentversion.setTstamp(new Date());
        cfcontentversion.setCommitedby(BigInteger.valueOf(loginbean.getCfuser().getId()));
        cfcontentversionService.create(cfcontentversion);
    }
    
    public void onSlideEnd(SlideEndEvent event) {
        selectedcontentversion = (int) event.getValue();
        if (selectedcontentversion <= contentversionMin) {
            selectedcontentversion = contentversionMin;
        }
        if (selectedcontentversion >= contentversionMax) {
            selectedcontentversion = contentversionMax;
            attributcontentlist = cfattributcontentService.findByClasscontentref(selectedContent);
        } else {
            String jsoncontent = getContent();
            List<CfAttributcontent> attributcontentversionlist = classutil.jsonImport(jsoncontent);
            for (CfAttributcontent attributcontent : attributcontentlist) {
                for (CfAttributcontent attributcontentversion : attributcontentversionlist) {
                    if (0 == attributcontent.getAttributref().getName().compareToIgnoreCase(attributcontentversion.getAttributref().getName())) {
                        if (null != attributcontent.getContentBoolean()) {
                            attributcontent.setContentBoolean(attributcontentversion.getContentBoolean());
                        }
                        if (null != attributcontent.getContentDate()) {
                            attributcontent.setContentDate(attributcontentversion.getContentDate());
                        }
                        if (null != attributcontent.getContentInteger()) {
                            attributcontent.setContentInteger(attributcontentversion.getContentInteger());
                        }
                        if (null != attributcontent.getContentReal()) {
                            attributcontent.setContentReal(attributcontentversion.getContentReal());
                        }
                        if (null != attributcontent.getContentString()) {
                            attributcontent.setContentString(attributcontentversion.getContentString());
                        }
                        if (null != attributcontent.getContentText()) {
                            attributcontent.setContentText(attributcontentversion.getContentText());
                        }
                        if (null != attributcontent.getAssetcontentlistref()) {
                            attributcontent.setAssetcontentlistref(attributcontentversion.getAssetcontentlistref());
                        }
                        if (null != attributcontent.getClasscontentlistref()) {
                            attributcontent.setClasscontentlistref(attributcontentversion.getClasscontentlistref());
                        }
                    }
                }
            }
        }
    }
    
    public String toString(CfAttributcontent attributcontent) {
        return contentUtil.toString(attributcontent);
    }

    public void selectDivContent(String contentname) {
        selectContent(cfclasscontentService.findByName(contentname));
    }
    
    public void selectContent(CfClasscontent content) {
        selectedContent = content;
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
        
        versionlist = cfcontentversionService.findByContentref(selectedContent.getId());
        difference = contentUtility.hasDifference(selectedContent);
        BigInteger co = selectedContent.getCheckedoutby();
        CheckoutUtil checkoutUtil = new CheckoutUtil();
        checkoutUtil.getCheckoutAccess(co, loginbean);
        checkedout = checkoutUtil.isCheckedout();
        access = checkoutUtil.isAccess();
        contentversionMin = 1;
        contentversionMax = versionlist.size();
        selectedcontentversion = contentversionMax;
        
        try {
            String output = selectedContent.getClassref().getTemplateref().getContent();
            for (CfAttributcontent attributcontent : attributcontentlist) {
                output = output.replaceAll("#" + attributcontent.getAttributref().getName() + "#", contentUtil.toString(attributcontent));
            }
            contentpreview = output;
        } catch (Exception ex) {
            contentpreview = "";
        }
    }
}
