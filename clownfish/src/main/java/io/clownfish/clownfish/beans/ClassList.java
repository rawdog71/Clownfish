package io.clownfish.clownfish.beans;

import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributetype;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributetypeService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.bean.ViewScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
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
@Named("classList")
@Transactional
@ViewScoped
@Component
public class ClassList implements Serializable {
    @Autowired CfClassService cfclassService;
    @Autowired CfAttributService cfattributService;
    @Autowired CfAttributetypeService cfattributetypeService;
    
    private @Getter @Setter List<CfClass> classListe;
    private @Getter @Setter CfClass selectedClass = null;
    private @Getter @Setter List<CfAttribut> selectedAttributList = null;
    //private @Getter @Setter AttributList attributlist = null;
    private @Getter @Setter CfAttribut selectedAttribut = null;
    private @Getter @Setter CfAttributetype selectedAttributeType = null;
    private @Getter @Setter List<CfAttributetype> attributetypelist = null;
    private @Getter @Setter String className;
    private @Getter @Setter String attributName;
    private @Getter @Setter boolean identity;
    private @Getter @Setter boolean autoinc;
    private @Getter @Setter boolean newButtonDisabled = false;
    private @Getter @Setter boolean newAttributButtonDisabled = false;
    
    @Autowired private @Getter @Setter AttributList attributlist;

    @PostConstruct
    public void init() {
        
        /*
        classListe = em.createNamedQuery("Knclass.findAll").getResultList();
        attributlist = new AttributList();
        attributetypelist = em.createNamedQuery("Knattributetype.findAll").getResultList();
        */
        attributetypelist = cfattributetypeService.findAll();
    }
    
    public void onSelect(SelectEvent event) {
        selectedClass = (CfClass) event.getObject();
        //selectedAttributList = attributlist.init(em, selectedClass);
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
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            //Knclass validateClass = (Knclass) em.createNamedQuery("Knclass.findByName").setParameter("name", className).getSingleResult();
            CfClass validateClass = cfclassService.findByName(className);
            newButtonDisabled = true;
        } catch (NoResultException ex) {
            newButtonDisabled = className.isEmpty();
        }
    }
    
    public void onChangeAttributName(ValueChangeEvent changeEvent) {
        try {
            //Knattribut validateClass = (Knattribut) em.createNamedQuery("Knattribut.findByNameAndClassref").setParameter("name", attributName).setParameter("classref", selectedClass).getSingleResult();
            CfAttribut validateClass = cfattributService.findByNameAndClassref(attributName, selectedClass);
            newAttributButtonDisabled = true;
        } catch (NoResultException ex) {
            newAttributButtonDisabled = className.isEmpty();
        }
    }
    
    public void onCreate(ActionEvent actionEvent) {
        try {
            CfClass newclass = new CfClass();
            newclass.setName(className);
            //knclassFacadeREST.create(newclass);
            cfclassService.create(newclass);
            //classListe = em.createNamedQuery("Knclass.findAll").getResultList();
            classListe = cfclassService.findAll();
            className = "";
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
            
            //knattributFacadeREST.create(newattribut);
            cfattributService.create(newattribut);
            selectedAttributList = attributlist.init(selectedClass);
            attributName = "";
            
            // Fill attributcontent with new attribut value
            /*
            List<Knclasscontent> modifyList = em.createNamedQuery("Knclasscontent.findByClassref").setParameter("classref", newattribut.getClassref()).getResultList();
            for (Knclasscontent classcontent : modifyList) {
                Knattributcontent newattributcontent = new Knattributcontent();
                newattributcontent.setAttributref(newattribut);
                newattributcontent.setClasscontentref(classcontent);
                knattributcontentFacadeREST.create(newattributcontent);
            }
            */
            
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
            //knattributFacadeREST.edit(selectedAttribut);
            cfattributService.edit(selectedAttribut);
        }
    }
}
