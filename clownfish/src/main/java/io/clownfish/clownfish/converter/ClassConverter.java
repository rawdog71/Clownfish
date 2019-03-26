/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.converter;

import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import java.io.Serializable;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@ViewScoped
@Named("classConverterBean")
@FacesConverter(value = "classConverter")
@Component
public class ClassConverter implements Converter, Serializable {
    @Autowired CfClassService cfclassservice;
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value.compareToIgnoreCase("-1") == 0) {
            return null;
        } else {
            //Object o = em.createNamedQuery("Knclass.findById").setParameter("id", new Long(value)).getSingleResult();
            //Object o = cfclassservice.findByName(value);
            Object o = cfclassservice.findById(new Long(value));
            return o;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "-1";
        } else {
            String returnname = ((CfClass) value).getId().toString();
            return  returnname;
        }
    }
    
}
