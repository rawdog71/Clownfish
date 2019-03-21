package io.clownfish.clownfish.converter;

import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.serviceinterface.CfListService;
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
@Named("sitelistConverterBean")
@FacesConverter(value = "sitelistConverter")
@Component
public class ListConverter implements Converter, Serializable {
    @Autowired CfListService cflistservice;
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value.compareToIgnoreCase("-1") == 0) {
            return null;
        } else {
            Object o = cflistservice.findById(new Long(value));
            return o;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "-1";
        } else {
            String returnname = ((CfList) value).getName();
            return returnname; 
        }
    }
}
