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

import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.dbentities.CfListcontentPK;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
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
@Named("datacontentList")
@Scope("session")
@Component
public class DataList implements Serializable {
    @Autowired CfListService cflistService;
    @Autowired CfListcontentService cflistcontentService;
    @Autowired CfClassService cfclassService;
    @Autowired CfClasscontentService cfclasscontentService;
    
    
    private @Getter @Setter List<CfList> datacontentlist = null;
    private @Getter @Setter CfList selectedList = null;
    private @Getter @Setter String contentName;
    private @Getter @Setter CfClass selectedClass;
    private @Getter @Setter List<CfClass> classlist = null;
    private @Getter @Setter boolean newContentButtonDisabled = false;
    private @Getter @Setter List<CfClasscontent> selectedListcontent = null;
    private @Getter @Setter List<CfClasscontent> filteredclasscontentlist = null;

    @PostConstruct
    public void init() {
        datacontentlist = cflistService.findAll();
        classlist = cfclassService.findAll();
        
        selectedListcontent = new ArrayList<>();
    }
    
    public void onSelect(SelectEvent event) {
        selectedList = (CfList) event.getObject();
        contentName = selectedList.getName();
        selectedClass = selectedList.getClassref();
        newContentButtonDisabled = true;
        
        filteredclasscontentlist = cfclasscontentService.findByClassref(selectedList.getClassref());
        List<CfListcontent> selectedcontent = cflistcontentService.findByListref(selectedList.getId());
        
        selectedListcontent.clear();
        if (selectedcontent.size() > 0) {
            for (CfListcontent listcontent : selectedcontent) {
                CfClasscontent selectedContent = cfclasscontentService.findById(listcontent.getCfListcontentPK().getClasscontentref());
                selectedListcontent.add(selectedContent);
            }
        }
    }
    
    public void onCreateContent(ActionEvent actionEvent) {
        try {
            CfList newlistcontent = new CfList();
            newlistcontent.setName(contentName);
            newlistcontent.setClassref(selectedClass);
            
            cflistService.create(newlistcontent);
            datacontentlist = cflistService.findAll();
        } catch (ConstraintViolationException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void onDeleteContent(ActionEvent actionEvent) {
        if (selectedList != null) {
            cflistService.delete(selectedList);
            datacontentlist = cflistService.findAll();
        }
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            CfList validateList = cflistService.findByName(contentName);
            newContentButtonDisabled = true;
        } catch (NoResultException ex) {
            newContentButtonDisabled = contentName.isEmpty();
        }
    }
    
    public void onChangeContent(AjaxBehaviorEvent event) {
        // Delete listcontent first
        List<CfListcontent> contentList = cflistcontentService.findByListref(selectedList.getId());
        for (CfListcontent content : contentList) {
            cflistcontentService.delete(content);
        }
        // Add selected listcontent
        if (selectedListcontent.size() > 0) {
            for (CfClasscontent selected : selectedListcontent) {
                CfListcontent listcontent = new CfListcontent();
                CfListcontentPK cflistcontentPK = new CfListcontentPK();
                cflistcontentPK.setListref(selectedList.getId());
                cflistcontentPK.setClasscontentref(selected.getId());
                listcontent.setCfListcontentPK(cflistcontentPK);
                cflistcontentService.create(listcontent);
            }
        }
    }
}
