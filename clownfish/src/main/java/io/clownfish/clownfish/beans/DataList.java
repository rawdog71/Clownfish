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

import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.dbentities.CfListcontentPK;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.utils.HibernateUtil;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Named("datacontentList")
@Scope("singleton")
@Component
public class DataList implements Serializable {
    @Autowired transient CfListService cflistService;
    @Autowired transient CfListcontentService cflistcontentService;
    @Autowired transient CfClassService cfclassService;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfAttributcontentService cfattributcontentService;
    @Autowired HibernateUtil hibernateUtil;
    
    private transient @Getter @Setter List<CfList> datacontentlist = null;
    private @Getter @Setter CfList selectedList = null;
    private @Getter @Setter String contentName;
    private @Getter @Setter CfClass selectedClass;
    private transient @Getter @Setter List<CfClass> classlist = null;
    private @Getter @Setter boolean newContentButtonDisabled = false;
    private transient @Getter @Setter List<CfClasscontent> selectedListcontent = null;
    private transient @Getter @Setter List<CfClasscontent> filteredclasscontentlist = null;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(DataList.class);

    @PostConstruct
    public void init() {
        datacontentlist = cflistService.findAll();
        classlist = cfclassService.findAll();
        
        selectedListcontent = new ArrayList<>();
    }
    
    public void onRefreshAll() {
        datacontentlist = cflistService.findAll();
        classlist = cfclassService.findAll();
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
                if (!selectedContent.isScrapped()) {
                    selectedListcontent.add(selectedContent);
                }
            }
        }
    }
    
    public void onCreateContent(ActionEvent actionEvent) {
        try {
            CfList newlistcontent = new CfList();
            newlistcontent.setName(contentName);
            newlistcontent.setClassref(selectedClass);
            
            cflistService.create(newlistcontent);
            onRefreshAll();
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    public void onDeleteContent(ActionEvent actionEvent) {
        if (selectedList != null) {
            
            // Lösche die Verknüpfungen aus den Attributswerten
            List<CfAttributcontent> attributcontentlist = cfattributcontentService.findByContentclassRef(selectedList);
            for (CfAttributcontent attributcontent : attributcontentlist) {
                attributcontent.setClasscontentlistref(null);
                cfattributcontentService.edit(attributcontent);
            }
            cflistService.delete(selectedList);
            onRefreshAll();
        }
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            cflistService.findByName(contentName);
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
        hibernateUtil.updateRelation(selectedList);
    }
}
