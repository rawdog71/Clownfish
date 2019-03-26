/*
 * Copyright Rainer Sulzbach
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
import javax.faces.bean.ViewScoped;
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
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Named("datacontentList")
@ViewScoped
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
        //datacontentlist = em.createNamedQuery("Knlist.findAll").getResultList();
        datacontentlist = cflistService.findAll();
        //classlist = em.createNamedQuery("Knclass.findAll").getResultList();
        classlist = cfclassService.findAll();
        
        selectedListcontent = new ArrayList<>();
    }
    
    public void onSelect(SelectEvent event) {
        selectedList = (CfList) event.getObject();
        
        contentName = selectedList.getName();
        selectedClass = selectedList.getClassref();
        newContentButtonDisabled = true;
        
        //filteredclasscontentlist = em.createNamedQuery("Knclasscontent.findByClassref").setParameter("classref", selectedList.getClassref()).getResultList();
        filteredclasscontentlist = cfclasscontentService.findByClassref(selectedList.getClassref());
        
        //List<Knlistcontent> selectedcontent = em.createNamedQuery("Knlistcontent.findByListref").setParameter("listref", selectedList.getId()).getResultList();
        List<CfListcontent> selectedcontent = cflistcontentService.findByListref(selectedList.getId());
        
        selectedListcontent.clear();
        if (selectedcontent.size() > 0) {
            for (CfListcontent listcontent : selectedcontent) {
                //Knclasscontent selectedContent = (Knclasscontent) em.createNamedQuery("Knclasscontent.findById").setParameter("id", listcontent.getKnlistcontentPK().getClasscontentref()).getSingleResult();
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
            
            //knlistFacadeREST.create(newlistcontent);
            cflistService.create(newlistcontent);
            
            //datacontentlist = em.createNamedQuery("Knlist.findAll").getResultList();
            datacontentlist = cflistService.findAll();
        } catch (ConstraintViolationException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void onDeleteContent(ActionEvent actionEvent) {
        if (selectedList != null) {
            //knlistFacadeREST.remove(selectedList);
            cflistService.delete(selectedList);
            //datacontentlist = em.createNamedQuery("Knlist.findAll").getResultList();
            datacontentlist = cflistService.findAll();
        }
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            //Knlist validateList = (Knlist) em.createNamedQuery("Knlist.findByName").setParameter("name", contentName).getSingleResult();
            CfList validateList = cflistService.findByName(contentName);
            newContentButtonDisabled = true;
        } catch (NoResultException ex) {
            newContentButtonDisabled = contentName.isEmpty();
        }
    }
    
    public void onChangeContent(AjaxBehaviorEvent event) {
        // Delete listcontent first
        //List<Knlistcontent> contentList = em.createNamedQuery("Knlistcontent.findByListref").setParameter("listref", selectedList.getId()).getResultList();
        List<CfListcontent> contentList = cflistcontentService.findByListref(selectedList.getId());
        for (CfListcontent content : contentList) {
            //knlistcontentFacadeREST.remove(content);
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
                //knlistcontentFacadeREST.create(listcontent);
                cflistcontentService.create(listcontent);
            }
        }
    }
}
