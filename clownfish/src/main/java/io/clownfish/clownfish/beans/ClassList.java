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

import io.clownfish.clownfish.compiler.JVMLanguages;
import io.clownfish.clownfish.datamodels.ODataWizard;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfAttributetype;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.odata.EntityUtil;
import io.clownfish.clownfish.odata.GenericEdmProvider;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributetypeService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.utils.ClassUtil;
import io.clownfish.clownfish.utils.ClownfishUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.plexus.util.StringUtils;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sulzbachr
 */
@Named("classList")
@Scope("singleton")
@Component
public class ClassList implements Serializable {
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfAttributService cfattributService;
    @Autowired transient CfAttributetypeService cfattributetypeService;
    @Autowired transient CfClasscontentService cfclascontentService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired DataList datalist;
    @Autowired ContentList contentlist;
    @Autowired ClassUtil classutil;
    @Autowired GenericEdmProvider edmprovider;
    
    private @Getter @Setter List<CfClass> classListe;
    private @Getter @Setter CfClass selectedClass = null;
    private transient @Getter @Setter List<CfAttribut> selectedAttributList = null;
    private transient @Getter @Setter List<ODataWizard> odataWizardList = null;
    private @Getter @Setter CfAttribut selectedAttribut = null;
    private @Getter @Setter CfAttributetype selectedAttributeType = null;
    private transient @Getter @Setter List<CfAttributetype> attributetypelist = null;
    private @Getter @Setter String className;
    private @Getter @Setter boolean classSearchrelevant;
    private @Getter @Setter boolean classMaintenance;
    private @Getter @Setter boolean classEncrypted;
    private @Getter @Setter boolean loginClass;
    private @Getter @Setter String attributName;
    private @Getter @Setter boolean identity;
    private @Getter @Setter boolean autoinc;
    private @Getter @Setter boolean isindex;
    private @Getter @Setter String defaultval;
    private @Getter @Setter long minval;
    private @Getter @Setter long maxval;
    private @Getter @Setter boolean mandatory;
    private @Getter @Setter boolean nodelete;
    private @Getter @Setter boolean extmutable;
    private @Getter @Setter String description;
    private @Getter @Setter List<CfClass> classListeRef;
    private @Getter @Setter CfClass selectedClassRef = null;
    private @Getter @Setter int selectedRelType = -1;
    private @Getter @Setter CfTemplate selectedTemplateRef = null;
    private @Getter @Setter boolean newButtonDisabled;
    private @Getter @Setter boolean newAttributButtonDisabled;
    private @Getter @Setter boolean editAttributButtonDisabled;
    private @Getter @Setter boolean renderClass;
    private @Getter @Setter int javaLanguage = 0;
    private @Getter @Setter String idField;
    private @Getter @Setter String passwordField;
    private @Getter @Setter String authField;
    private @Getter @Setter String adminEmailField;
    @Autowired transient private @Getter @Setter  EntityUtil entityutil;

    @Autowired transient private @Getter @Setter AttributList attributlist;
    final transient Logger LOGGER = LoggerFactory.getLogger(ClassList.class);

    @PostConstruct
    public void init() {
        LOGGER.info("INIT CLASSLIST START");
        idField = "";
        passwordField = "";
        authField = "";
        classListe = cfclassService.findAll();
        classListeRef = cfclassService.findAll();
        attributetypelist = cfattributetypeService.findAll();
        odataWizardList = new ArrayList<>();
        renderClass = false;
        LOGGER.info("INIT CLASSLIST END");
    }
    
    public void onRefreshAll() {
        classListe = cfclassService.findAll();
        classListeRef = cfclassService.findAll();
        attributetypelist = cfattributetypeService.findAll();
    }
    
    public void onRefreshSelection() {
        if (null != selectedClass) {
            selectedClass = cfclassService.findById(selectedClass.getId());
            selectedTemplateRef = selectedClass.getTemplateref();
        }
    }
    
    public void onSelect(SelectEvent event) {
        selectedClass = (CfClass) event.getObject();
        idField = "";
        passwordField = "";
        authField = "";
        selectedAttributList = attributlist.init(selectedClass);
        odataWizardList.clear();
        for (CfAttribut attr : selectedAttributList) {
            ODataWizard odw = new ODataWizard();
            switch (attr.getAttributetype().getName()) {
                case "string":
                case "integer":
                case "real":
                case "datetime":
                case "boolean":
                    odw.setHeaderenabled(true);
                    odw.setRelationenabled(false);
                    odw.setTableheader(true);
                    break;
                case "classref":
                    odw.setRelationenabled(true);
                    odw.setHeaderenabled(true);
                    odw.setTableheader(true);
                    break;
                default:
                    odw.setTableheader(false);
                    odw.setHeaderenabled(false);
                    odw.setRelationenabled(false);
                    break;
            }
            odw.setRelationattribut1(null);
            odw.setRelationattribut2(null);
            odw.setRelationattribut3(null);
            odw.setAttribut(attr);
            odataWizardList.add(odw);
        }
        className = selectedClass.getName();
        classSearchrelevant = selectedClass.isSearchrelevant();
        classMaintenance = selectedClass.isMaintenance();
        classEncrypted = selectedClass.isEncrypted();
        loginClass = selectedClass.isLoginclass();
        selectedTemplateRef = selectedClass.getTemplateref();
        attributName = "";
        selectedAttributeType = null;
        newButtonDisabled = true;
    }
    
    public void onSelectAttribute(SelectEvent event) {
        selectedAttribut = (CfAttribut) event.getObject();
        attributName = selectedAttribut.getName();
        selectedAttributeType = selectedAttribut.getAttributetype();
        if (0 == selectedAttributeType.getName().compareToIgnoreCase("classref")) {
            renderClass = true;
            selectedClassRef = selectedAttribut.getRelationref();
            selectedRelType = selectedAttribut.getRelationtype();
        } else {
            selectedClassRef = null;
            renderClass = false;
            selectedRelType = -1;
        }
        identity = selectedAttribut.getIdentity();
        autoinc = selectedAttribut.getAutoincrementor();
        isindex = selectedAttribut.getIsindex();
        defaultval = selectedAttribut.getDefault_val();
        minval = selectedAttribut.getMin_val();
        maxval = selectedAttribut.getMax_val();
        mandatory = selectedAttribut.getMandatory();
        description = selectedAttribut.getDescription();
        nodelete = selectedAttribut.getNodelete();
        extmutable = selectedAttribut.getExt_mutable();
        newAttributButtonDisabled = true;
        editAttributButtonDisabled = nodelete;
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        CfClass newclass = cfclassService.findByName(className);
        if (null == newclass) {
            newButtonDisabled = false;
        } else {
            newButtonDisabled = !className.isEmpty();
        }
    }
    
    public void onChangeAttributName(ValueChangeEvent changeEvent) {
        CfAttribut dummyattribut = cfattributService.findByNameAndClassref(attributName, selectedClass);
        if (null != dummyattribut) {
            newAttributButtonDisabled = true;
        } else {
            newAttributButtonDisabled = attributName.isEmpty();
        }
    }
    
    public void onCreate(ActionEvent actionEvent) {
        try {
            className = className.trim().replaceAll("[^a-zA-Z0-9]", "_");
            className = className.toLowerCase();
            className = StringUtils.capitalise(className);
            CfClass newclass = new CfClass();
            newclass.setName(className);
            newclass.setSearchrelevant(classSearchrelevant);
            newclass.setMaintenance(classMaintenance);
            newclass.setEncrypted(classEncrypted);
            newclass.setLoginclass(loginClass);
            newclass.setTemplateref(selectedTemplateRef);
            CfClass createdclass = cfclassService.create(newclass);
            
            CfAttribut newattribut = new CfAttribut();
            newattribut.setClassref(createdclass);
            newattribut.setName("id");
            newattribut.setIdentity(true);
            newattribut.setAutoincrementor(true);
            newattribut.setIsindex(false);
            newattribut.setAttributetype(cfattributetypeService.findByName("integer"));
            //newattribut.setRelationref(selectedClassRef);
            newattribut.setRelationtype(-1);
            newattribut.setDefault_val("");
            //newattribut.setMin_val(0);
            //newattribut.setMax_val(0);
            newattribut.setMandatory(false);
            newattribut.setDescription("Identifikation");
            newattribut.setNodelete(true);
            newattribut.setExt_mutable(true);
            
            cfattributService.create(newattribut);
            
            if (loginClass) {
                // Create valid field
                CfAttribut newattributvalid = new CfAttribut();
                newattributvalid.setClassref(createdclass);
                newattributvalid.setName("valid");
                newattributvalid.setIdentity(false);
                newattributvalid.setAutoincrementor(false);
                newattributvalid.setIsindex(false);
                newattributvalid.setAttributetype(cfattributetypeService.findByName("boolean"));
                newattributvalid.setRelationtype(-1);
                newattributvalid.setDefault_val("false");
                newattributvalid.setMandatory(false);
                newattributvalid.setDescription("Valid by admin");
                newattributvalid.setNodelete(true);
                newattributvalid.setExt_mutable(false);
                cfattributService.create(newattributvalid);
                // Create confirmed field
                CfAttribut newattributconfirmed = new CfAttribut();
                newattributconfirmed.setClassref(createdclass);
                newattributconfirmed.setName("confirmed");
                newattributconfirmed.setIdentity(false);
                newattributconfirmed.setAutoincrementor(false);
                newattributconfirmed.setIsindex(false);
                newattributconfirmed.setAttributetype(cfattributetypeService.findByName("boolean"));
                newattributconfirmed.setRelationtype(-1);
                newattributconfirmed.setDefault_val("false");
                newattributconfirmed.setMandatory(false);
                newattributconfirmed.setDescription("Confirmed by user");
                newattributconfirmed.setNodelete(true);
                newattributconfirmed.setExt_mutable(false);
                cfattributService.create(newattributconfirmed);
                // Create email field
                CfAttribut newattributemail = new CfAttribut();
                newattributemail.setClassref(createdclass);
                newattributemail.setName("email");
                newattributemail.setIdentity(true);
                newattributemail.setAutoincrementor(false);
                newattributemail.setIsindex(false);
                newattributemail.setAttributetype(cfattributetypeService.findByName("string"));
                newattributemail.setRelationtype(-1);
                newattributemail.setDefault_val("");
                newattributemail.setMandatory(true);
                newattributemail.setDescription("EMail");
                newattributemail.setNodelete(true);
                newattributemail.setExt_mutable(true);
                cfattributService.create(newattributemail);
                // Create password field
                CfAttribut newattributpassword = new CfAttribut();
                newattributpassword.setClassref(createdclass);
                newattributpassword.setName("password");
                newattributpassword.setIdentity(false);
                newattributpassword.setAutoincrementor(false);
                newattributpassword.setIsindex(false);
                newattributpassword.setAttributetype(cfattributetypeService.findByName("hashstring"));
                newattributpassword.setRelationtype(-1);
                newattributpassword.setDefault_val("");
                newattributpassword.setMandatory(false);
                newattributpassword.setDescription("Passwort");
                newattributpassword.setNodelete(true);
                newattributpassword.setExt_mutable(true);
                cfattributService.create(newattributpassword);
                
                // Create password field
                CfAttribut newattributcreated = new CfAttribut();
                newattributcreated.setClassref(createdclass);
                newattributcreated.setName("created");
                newattributcreated.setIdentity(false);
                newattributcreated.setAutoincrementor(false);
                newattributcreated.setIsindex(false);
                newattributcreated.setAttributetype(cfattributetypeService.findByName("datetime"));
                newattributcreated.setRelationtype(-1);
                newattributcreated.setDefault_val("");
                newattributcreated.setMandatory(false);
                newattributcreated.setDescription("Angelegt");
                newattributcreated.setNodelete(true);
                newattributcreated.setExt_mutable(false);
                cfattributService.create(newattributcreated);
                
                // Create password field
                CfAttribut newattributupdated = new CfAttribut();
                newattributupdated.setClassref(createdclass);
                newattributupdated.setName("lastlogin");
                newattributupdated.setIdentity(false);
                newattributupdated.setAutoincrementor(false);
                newattributupdated.setIsindex(false);
                newattributupdated.setAttributetype(cfattributetypeService.findByName("datetime"));
                newattributupdated.setRelationtype(-1);
                newattributupdated.setDefault_val("");
                newattributupdated.setMandatory(false);
                newattributupdated.setDescription("Letzter Login");
                newattributupdated.setNodelete(true);
                newattributupdated.setExt_mutable(false);
                cfattributService.create(newattributupdated);
            }
            
            
            selectedAttributList = attributlist.init(selectedClass);
            attributName = "";
            
            // Fill attributcontent with new attribut value
            List<CfClasscontent> modifyList = cfclascontentService.findByClassref(newattribut.getClassref());
            for (CfClasscontent classcontent : modifyList) {
                CfAttributcontent newattributcontent = new CfAttributcontent();
                newattributcontent.setAttributref(newattribut);
                newattributcontent.setClasscontentref(classcontent);
                cfattributcontentService.create(newattributcontent);
            }
            
            
            classListe = cfclassService.findAll();
            classListeRef = cfclassService.findAll();
            className = "";
            classSearchrelevant = false;
            classMaintenance = true;
            classEncrypted = false;
            loginClass = false;
            contentlist.init();
            datalist.init();
            
            FacesMessage message = new FacesMessage("Class created");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    public void onEdit(ActionEvent actionEvent) {
        try {
            selectedClass.setName(className);
            selectedClass.setSearchrelevant(classSearchrelevant);
            selectedClass.setMaintenance(classMaintenance);
            selectedClass.setEncrypted(classEncrypted);
            selectedClass.setLoginclass(loginClass);
            selectedClass.setTemplateref(selectedTemplateRef);
            cfclassService.edit(selectedClass);
            classListe = cfclassService.findAll();
            contentlist.init();
            datalist.init();
            HibernateUtil.generateTablesDatamodel(selectedClass.getName(), 0);
            FacesMessage message = new FacesMessage("Datamodel recreated");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    public void onCreateAttribut(ActionEvent actionEvent) {
        try {
            CfAttribut newattribut = new CfAttribut();
            newattribut.setClassref(selectedClass);
            newattribut.setName(attributName);
            newattribut.setIdentity(identity);
            newattribut.setAutoincrementor(autoinc);
            newattribut.setIsindex(isindex);
            newattribut.setAttributetype(selectedAttributeType);
            newattribut.setRelationref(selectedClassRef);
            newattribut.setRelationtype(selectedRelType);
            newattribut.setDefault_val(defaultval);
            newattribut.setMin_val(minval);
            newattribut.setMax_val(maxval);
            newattribut.setMandatory(mandatory);
            newattribut.setDescription(description);
            newattribut.setNodelete(false);
            newattribut.setExt_mutable(extmutable);
            
            cfattributService.create(newattribut);
            selectedAttributList = attributlist.init(selectedClass);
            attributName = "";
            
            // Fill attributcontent with new attribut value
            List<CfClasscontent> modifyList = cfclascontentService.findByClassref(newattribut.getClassref());
            for (CfClasscontent classcontent : modifyList) {
                CfAttributcontent newattributcontent = new CfAttributcontent();
                if (!newattribut.getDefault_val().isBlank()) {
                    switch (newattribut.getAttributetype().getName()) {
                        case "boolean":
                            newattributcontent.setContentBoolean(ClownfishUtil.getBoolean(newattribut.getDefault_val(), false));
                            break;
                        case "string":
                            newattributcontent.setContentString(newattribut.getDefault_val());
                            break;
                        case "integer":
                            newattributcontent.setContentInteger(BigInteger.valueOf(Long.parseLong(newattribut.getDefault_val())));
                            break;
                        case "real":
                            newattributcontent.setContentReal(Double.parseDouble(newattribut.getDefault_val()));
                            break;
                        case "text":
                        case "htmltext":
                        case "markdown":
                            newattributcontent.setContentText(newattribut.getDefault_val());
                            break;
                    }
                }
                newattributcontent.setAttributref(newattribut);
                newattributcontent.setClasscontentref(classcontent);
                cfattributcontentService.create(newattributcontent);
            }
            
            //HibernateUtil.generateTablesDatamodel(selectedClass.getName(), 0);
            FacesMessage message = new FacesMessage("Attribute created");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    public void onChangeAttribut(ActionEvent actionEvent) {
        if (selectedAttribut != null) {
            selectedAttribut.setName(attributName);
            selectedAttribut.setAttributetype(selectedAttributeType);
            selectedAttribut.setIdentity(identity);
            selectedAttribut.setAutoincrementor(autoinc);
            selectedAttribut.setIsindex(isindex);
            if (0 == selectedAttributeType.getName().compareToIgnoreCase("classref")) {
                selectedAttribut.setRelationref(selectedClassRef);
                selectedAttribut.setRelationtype(selectedRelType);
            } else {
                selectedAttribut.setRelationref(null);
                selectedAttribut.setRelationtype(0);
            }
            selectedAttribut.setDefault_val(defaultval);
            selectedAttribut.setMin_val(minval);
            selectedAttribut.setMax_val(maxval);
            selectedAttribut.setMandatory(mandatory);
            selectedAttribut.setExt_mutable(extmutable);
            selectedAttribut.setDescription(description);
            cfattributService.edit(selectedAttribut);
            //HibernateUtil.generateTablesDatamodel(selectedClass.getName(), 1);
            
            FacesMessage message = new FacesMessage("Attribute changed");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onChangeAttributtype() {
        if (null != selectedAttributeType) {
            if (0 == selectedAttributeType.getName().compareToIgnoreCase("classref")) {
                renderClass = true;
            } else {
                renderClass = false;
            }
        }
    }
    
    public void onRecreateDatamodel(ActionEvent actionEvent) {
        if (null != selectedClass) {
            HibernateUtil.generateTablesDatamodel(selectedClass.getName(), 1);
            edmprovider.init();
            entityutil.clearAttributmap();
            Thread edmprovider_thread = new Thread(edmprovider);
            edmprovider_thread.start();
            FacesMessage message = new FacesMessage("Datamodel recreated with data init");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onGenerateJVMClass(ActionEvent actionEvent) {
        if (null != selectedClass) {
            classutil.generateJVMClass(selectedClass, JVMLanguages.valueOf(javaLanguage));
            FacesMessage message = new FacesMessage("JVM class generated");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void onGenerateHTMLForm(ActionEvent actionEvent) {
        if (selectedClass != null) {
            classutil.generateHTMLForm(selectedClass);
            FacesMessage message = new FacesMessage("HTML Form template generated");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public void onGenerateODataForm(ActionEvent actionEvent) {
        if (selectedClass != null) {
            classutil.generateODataForm(selectedClass, odataWizardList);
            FacesMessage message = new FacesMessage("OData Form template generated");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void onGenerateLogin(ActionEvent actionEvent) {
        if (selectedClass != null) {
            //classutil.generateLogin(selectedClass);
            classutil.generateLogin(selectedClass, idField, passwordField, authField, adminEmailField);
            FacesMessage message = new FacesMessage("Login Form template generated");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    public List getAttributenames(CfClass clazz) {
        return cfattributService.findByClassref(clazz);
    }
}
