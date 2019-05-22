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
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Named("classList")
@Scope("request")
@Component
public class ClassList implements Serializable {
    @Autowired CfClassService cfclassService;
    @Autowired CfAttributService cfattributService;
    @Autowired CfAttributetypeService cfattributetypeService;
    @Autowired CfClasscontentService cfclascontentService;
    @Autowired CfAttributcontentService cfattributcontentService;
    @Autowired DataList datalist;
    @Autowired ContentList contentlist;
    
    private @Getter @Setter List<CfClass> classListe;
    private @Getter @Setter CfClass selectedClass = null;
    private @Getter @Setter List<CfAttribut> selectedAttributList = null;
    private @Getter @Setter CfAttribut selectedAttribut = null;
    private @Getter @Setter CfAttributetype selectedAttributeType = null;
    private @Getter @Setter List<CfAttributetype> attributetypelist = null;
    private @Getter @Setter String className;
    private @Getter @Setter String attributName;
    private @Getter @Setter boolean identity;
    private @Getter @Setter boolean autoinc;
    private @Getter @Setter boolean newButtonDisabled;
    private @Getter @Setter boolean newAttributButtonDisabled;
    
    @Autowired private @Getter @Setter AttributList attributlist;

    @PostConstruct
    public void init() {
        classListe = cfclassService.findAll();
        attributetypelist = cfattributetypeService.findAll();
    }
    
    public void onSelect(SelectEvent event) {
        selectedClass = (CfClass) event.getObject();
        selectedAttributList = attributlist.init(selectedClass);
        className = selectedClass.getName();
        attributName = "";
        selectedAttributeType = null;
        newButtonDisabled = true;
    }
    
    public void onSelectAttribute(SelectEvent event) {
        selectedAttribut = (CfAttribut) event.getObject();
        attributName = selectedAttribut.getName();
        selectedAttributeType = selectedAttribut.getAttributetype();
        identity = selectedAttribut.getIdentity();
        autoinc = selectedAttribut.getAutoincrementor();
        newAttributButtonDisabled = true;
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            CfClass validateClass = cfclassService.findByName(className);
            newButtonDisabled = true;
        } catch (NoResultException ex) {
            newButtonDisabled = className.isEmpty();
        }
    }
    
    public void onChangeAttributName(ValueChangeEvent changeEvent) {
        try {
            CfAttribut validateClass = cfattributService.findByNameAndClassref(attributName, selectedClass);
            newAttributButtonDisabled = true;
        } catch (NoResultException ex) {
            newAttributButtonDisabled = attributName.isEmpty();
        }
    }
    
    public void onCreate(ActionEvent actionEvent) {
        try {
            CfClass newclass = new CfClass();
            newclass.setName(className);
            cfclassService.create(newclass);
            classListe = cfclassService.findAll();
            className = "";
            contentlist.init();
            datalist.init();
        } catch (ConstraintViolationException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void onCreateAttribut(ActionEvent actionEvent) {
        try {
            CfAttribut newattribut = new CfAttribut();
            newattribut.setClassref(selectedClass);
            newattribut.setName(attributName);
            newattribut.setIdentity(identity);
            newattribut.setAutoincrementor(autoinc);
            newattribut.setAttributetype(selectedAttributeType);
            
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
            
        } catch (ConstraintViolationException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void onChangeAttribut(ActionEvent actionEvent) {
        if (selectedAttribut != null) {
            selectedAttribut.setName(attributName);
            selectedAttribut.setAttributetype(selectedAttributeType);
            selectedAttribut.setIdentity(identity);
            selectedAttribut.setAutoincrementor(autoinc);
            cfattributService.edit(selectedAttribut);
        }
    }
}
