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
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.utils.PasswordUtil;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
@Named("classcontentList")
@Scope("session")
@Component
public class ContentList implements Serializable {
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired transient CfAssetService cfassetService;
    @Autowired transient CfAttributService cfattributService;
    
    private @Getter @Setter List<CfClasscontent> classcontentlist;
    private @Getter @Setter CfClasscontent selectedContent = null;
    private transient @Getter @Setter List<CfAttributcontent> attributcontentlist = null;
    private @Getter @Setter CfAttributcontent selectedAttributContent = null;
    private @Getter @Setter List<CfClasscontent> filteredContent;
    private @Getter @Setter String contentName;
    private @Getter @Setter CfClass selectedClass;
    private transient @Getter @Setter List<CfClass> classlist = null;
    private @Getter @Setter boolean newContentButtonDisabled = false;
    private @Getter @Setter boolean contentValueBoolean = false;
    private @Getter @Setter Date contentValueDatetime;
    private @Getter @Setter CfAttributcontent selectedAttribut = null;
    private @Getter @Setter long selectedAttributId;
    private @Getter @Setter CfAsset selectedMedia;
    private @Getter @Setter String editContent;
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
    private @Getter @Setter boolean valueBooleanRendered = false;
    private @Getter @Setter boolean valueDatetimeRendered = false;
    
    final transient Logger logger = LoggerFactory.getLogger(ContentList.class);

    public boolean renderSelected(CfAttributcontent attribut) {
        if (selectedAttribut != null) {
            return attribut.getId() == selectedAttribut.getId();
        } else {
            return false;
        }
    }
    
    @PostConstruct
    public void init() {
        classcontentlist = cfclasscontentService.findAll();
        classlist = cfclassService.findAll();
        editContent = "";
    }
    
    public void onSelect(SelectEvent event) {
        selectedContent = (CfClasscontent) event.getObject();
        attributcontentlist = cfattributcontentService.findByClasscontentref(selectedContent);
       
        contentName = selectedContent.getName();
        selectedClass = selectedContent.getClassref();
        newContentButtonDisabled = true;
    }
    
    public void onSelectAttribut(SelectEvent event) {
        selectedAttribut = (CfAttributcontent) event.getObject();
        selectedAttributId = selectedAttribut.getId();
        
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
                break;
            case "media":
                isMediaType = true;
                if (selectedAttribut.getContentInteger() != null) {
                    selectedMedia = cfassetService.findById(selectedAttribut.getContentInteger().longValue());
                }
                break;    
        }
        editContent = selectedAttribut.toString();
    }
    
    public void onCreateContent(ActionEvent actionEvent) {
        try {
            CfClasscontent newclasscontent = new CfClasscontent();
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
            classcontentlist = cfclasscontentService.findAll();
        } catch (ConstraintViolationException ex) {
            logger.error(ex.getMessage());
        }
    }
    
    public void onDeleteContent(ActionEvent actionEvent) {
        if (selectedContent != null) {
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
                DateTimeFormatter dtf = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss ZZZ yyyy").withLocale(Locale.US);
                DateTime dt = DateTime.parse(editContent, dtf);
                
                selectedAttribut.setContentDate(dt.toDate());
                break;
            case "media":
                selectedAttribut.setContentInteger(BigInteger.valueOf(selectedMedia.getId()));
                break;    
        }
        cfattributcontentService.edit(selectedAttribut);
    }
}
