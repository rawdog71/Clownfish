/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.beans;

import io.clownfish.clownfish.dbentities.CfProperty;
import io.clownfish.clownfish.serviceinterface.CfPropertyService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
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
@Transactional
@Named("propertylist")
@ViewScoped
@Component
public class PropertyList {
    @Autowired CfPropertyService cfpropertyService;
    
    private @Getter @Setter List<CfProperty> propertylist;
    private @Getter @Setter HashMap<String, String> propertymap;
    private @Getter @Setter CfProperty selectedProperty;
    private @Getter @Setter List<CfProperty> filteredProperty;
    private @Getter @Setter boolean newPropertyButtonDisabled;
    private @Getter @Setter String propertykey;
    private @Getter @Setter String propertyvalue;

    @PostConstruct
    public void init() {
        propertylist = cfpropertyService.findAll();
        newPropertyButtonDisabled = false;
    }
    
    public Map<String, String> fillPropertyMap() {
        propertymap = new HashMap<>();
        propertylist = cfpropertyService.findAll();
        for (CfProperty property : propertylist) {
            propertymap.put(property.getHashkey(), property.getValue());
        }
        return propertymap;
    }
    
    public void onSelect(SelectEvent event) {
        selectedProperty = (CfProperty) event.getObject();
        propertykey = selectedProperty.getHashkey();
        propertyvalue = selectedProperty.getValue();
        newPropertyButtonDisabled = true;
    }
    
    public void onCreateProperty(ActionEvent actionEvent) {
        try {
            CfProperty newproperty = new CfProperty();
            newproperty.setHashkey(propertykey);
            newproperty.setValue(propertyvalue);
            cfpropertyService.create(newproperty);

            propertylist = cfpropertyService.findAll();
        } catch (ConstraintViolationException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void onEditProperty(ActionEvent actionEvent) {
        try {
            if (null != selectedProperty) {
                selectedProperty.setHashkey(propertykey);
                selectedProperty.setValue(propertyvalue);
                cfpropertyService.edit(selectedProperty);
                propertylist = cfpropertyService.findAll();
            }
        } catch (ConstraintViolationException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void onDeleteProperty(ActionEvent actionEvent) {
        if (null != selectedProperty) {
            cfpropertyService.delete(selectedProperty);
            propertylist = cfpropertyService.findAll();
        }
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            CfProperty validateProperty = cfpropertyService.findByHashkey(propertykey);
            newPropertyButtonDisabled = true;
        } catch (NoResultException ex) {
            newPropertyButtonDisabled = selectedProperty.getHashkey().isEmpty();
        }
    }
}
