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

import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfAttributetype;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributetypeService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.utils.HibernateUtil;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import javax.persistence.NoResultException;
import jakarta.validation.ConstraintViolationException;
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
    
    private @Getter @Setter List<CfClass> classListe;
    private @Getter @Setter CfClass selectedClass = null;
    private transient @Getter @Setter List<CfAttribut> selectedAttributList = null;
    private @Getter @Setter CfAttribut selectedAttribut = null;
    private @Getter @Setter CfAttributetype selectedAttributeType = null;
    private transient @Getter @Setter List<CfAttributetype> attributetypelist = null;
    private @Getter @Setter String className;
    private @Getter @Setter boolean classSearchrelevant;
    private @Getter @Setter boolean classMaintenance;
    private @Getter @Setter String attributName;
    private @Getter @Setter boolean identity;
    private @Getter @Setter boolean autoinc;
    private @Getter @Setter boolean isindex;
    private @Getter @Setter List<CfClass> classListeRef;
    private @Getter @Setter CfClass selectedClassRef = null;
    private @Getter @Setter boolean newButtonDisabled;
    private @Getter @Setter boolean newAttributButtonDisabled;
    private @Getter @Setter boolean renderClass;
    @Autowired HibernateUtil hibernateUtil;
    
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
    
    public void onSelect(SelectEvent event) {
        selectedClass = (CfClass) event.getObject();
        selectedAttributList = attributlist.init(selectedClass);
        className = selectedClass.getName();
        classSearchrelevant = selectedClass.isSearchrelevant();
        classMaintenance = selectedClass.isMaintenance();
        attributName = "";
        selectedAttributeType = null;
        newButtonDisabled = true;
    }
    
    public void onSelectAttribute(SelectEvent event) {
        selectedAttribut = (CfAttribut) event.getObject();
        attributName = selectedAttribut.getName();
        selectedAttributeType = selectedAttribut.getAttributetype();
        if (selectedAttributeType.getName().compareToIgnoreCase("classref") == 0) {
            renderClass = true;
            selectedClassRef = selectedAttribut.getRelationref();
        } else {
            selectedClassRef = null;
            renderClass = false;
        }
        identity = selectedAttribut.getIdentity();
        autoinc = selectedAttribut.getAutoincrementor();
        isindex = selectedAttribut.getIsindex();
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
            CfClass newclass = new CfClass();
            newclass.setName(className);
            newclass.setSearchrelevant(classSearchrelevant);
            newclass.setMaintenance(classMaintenance);
            cfclassService.create(newclass);
            classListe = cfclassService.findAll();
            classListeRef = cfclassService.findAll();
            className = "";
            classSearchrelevant = false;
            classMaintenance = true;
            contentlist.init();
            datalist.init();
            //hibernateUtil.generateTablesDatamodel(selectedClass.getName(), 1);
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    public void onEdit(ActionEvent actionEvent) {
        try {
            selectedClass.setName(className);
            selectedClass.setSearchrelevant(classSearchrelevant);
            selectedClass.setMaintenance(classMaintenance);
            cfclassService.edit(selectedClass);
            classListe = cfclassService.findAll();
            contentlist.init();
            datalist.init();
            //hibernateUtil.generateTablesDatamodel(selectedClass.getName(), 1);
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
            //hibernateUtil.generateTablesDatamodel(selectedClass.getName(), 1);
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
            selectedAttribut.setRelationref(selectedClassRef);
            cfattributService.edit(selectedAttribut);
            //hibernateUtil.generateTablesDatamodel(selectedClass.getName(), 1);
        }
    }
    
    public void onRecreateDatamodel(ActionEvent actionEvent) {
        if (null != selectedClass) {
            hibernateUtil.generateTablesDatamodel(selectedClass.getName(), 1);
        }
    }
}
