package io.clownfish.clownfish.converter;

import io.clownfish.clownfish.dbentities.CfAttributetype;
import io.clownfish.clownfish.serviceinterface.CfAttributetypeService;
import java.io.Serializable;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */

@ViewScoped
@Named("attributetypeConverterBean")
@FacesConverter(value = "attributetypeConverter")
@Component
public class AttributetypeConverter implements Converter, Serializable {
    @Autowired CfAttributetypeService cfattributetypeService;
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value.compareToIgnoreCase("-1") == 0) {
            return null;
        } else {
            //Object o = em.createNamedQuery("Knattributetype.findById").setParameter("id", new Long(value)).getSingleResult();
            Object o = cfattributetypeService.findByName(value);
            return o;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "-1";
        } else {
            String returnname = ((CfAttributetype) value).getName();
            return returnname; 
        }
    }
    
}
