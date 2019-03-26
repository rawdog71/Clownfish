package io.clownfish.clownfish.converter;

import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
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
@Named("classcontentConverterBean")
@FacesConverter(value = "classcontentConverter")
@Component
public class ClasscontentConverter implements Converter, Serializable {
    @Autowired CfClasscontentService cfclasscontentservice;
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value.compareToIgnoreCase("-1") == 0) {
            return null;
        } else {
            //Object o = em.createNamedQuery("Knclasscontent.findById").setParameter("id", new Long(value)).getSingleResult();
            Object o = cfclasscontentservice.findById(new Long(value));
            return o;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "-1";
        } else {
            String returnname = ((CfClasscontent) value).getId().toString();
            return  returnname;
        }
    }
}
