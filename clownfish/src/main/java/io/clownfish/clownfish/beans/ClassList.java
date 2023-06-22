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
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfAttributetype;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.odata.GenericEdmProvider;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributetypeService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.utils.ClassUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import javax.persistence.NoResultException;
import jakarta.validation.ConstraintViolationException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.plexus.util.StringUtils;
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
    private @Getter @Setter CfAttribut selectedAttribut = null;
    private @Getter @Setter CfAttributetype selectedAttributeType = null;
    private transient @Getter @Setter List<CfAttributetype> attributetypelist = null;
    private @Getter @Setter String className;
    private @Getter @Setter boolean classSearchrelevant;
    private @Getter @Setter boolean classMaintenance;
    private @Getter @Setter boolean classEncrypted;
    private @Getter @Setter String attributName;
    private @Getter @Setter boolean identity;
    private @Getter @Setter boolean autoinc;
    private @Getter @Setter boolean isindex;
    private @Getter @Setter String defaultval;
    private @Getter @Setter long minval;
    private @Getter @Setter long maxval;
    private @Getter @Setter boolean mandatory;
    private @Getter @Setter String description;
    private @Getter @Setter List<CfClass> classListeRef;
    private @Getter @Setter CfClass selectedClassRef = null;
    private @Getter @Setter int selectedRelType = -1;
    private @Getter @Setter CfTemplate selectedTemplateRef = null;
    private @Getter @Setter boolean newButtonDisabled;
    private @Getter @Setter boolean newAttributButtonDisabled;
    private @Getter @Setter boolean renderClass;
    private @Getter @Setter int javaLanguage = 0;
    
    @Autowired transient private @Getter @Setter AttributList attributlist;
    final transient Logger LOGGER = LoggerFactory.getLogger(ClassList.class);

    @PostConstruct
    public void init() {
        LOGGER.info("INIT CLASSLIST START");
        classListe = cfclassService.findAll();
        classListeRef = cfclassService.findAll();
        attributetypelist = cfattributetypeService.findAll();
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
        selectedAttributList = attributlist.init(selectedClass);
        className = selectedClass.getName();
        classSearchrelevant = selectedClass.isSearchrelevant();
        classMaintenance = selectedClass.isMaintenance();
        classEncrypted = selectedClass.isEncrypted();
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
        newAttributButtonDisabled = true;
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            cfclassService.findByName(className);
            newButtonDisabled = true;
        } catch (NoResultException ex) {
            newButtonDisabled = className.isEmpty();
        }
    }
    
    public void onChangeAttributName(ValueChangeEvent changeEvent) {
        try {
            cfattributService.findByNameAndClassref(attributName, selectedClass);
            newAttributButtonDisabled = true;
        } catch (NoResultException ex) {
            newAttributButtonDisabled = attributName.isEmpty();
        }
    }
    
    public void onCreate(ActionEvent actionEvent) {
        try {
            className = className.trim().replaceAll("\\s+", "_");
            className = className.toLowerCase();
            className = StringUtils.capitalise(className);
            CfClass newclass = new CfClass();
            newclass.setName(className);
            newclass.setSearchrelevant(classSearchrelevant);
            newclass.setMaintenance(classMaintenance);
            newclass.setEncrypted(classEncrypted);
            newclass.setTemplateref(selectedTemplateRef);
            cfclassService.create(newclass);
            classListe = cfclassService.findAll();
            classListeRef = cfclassService.findAll();
            className = "";
            classSearchrelevant = false;
            classMaintenance = true;
            classEncrypted = false;
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
            
            cfattributService.create(newattribut);
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
}
