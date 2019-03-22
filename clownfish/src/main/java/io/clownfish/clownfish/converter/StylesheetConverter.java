package io.clownfish.clownfish.converter;

import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.serviceinterface.CfStylesheetService;
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
@Named("stylesheetConverterBean")
@FacesConverter(value = "stylesheetConverter")
@Component
public class StylesheetConverter implements Converter, Serializable {
    @Autowired CfStylesheetService cfstylesheetservice;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value.compareToIgnoreCase("-1") == 0) {
            return null;
        } else {
            Object o = cfstylesheetservice.findByName(value);
            return o;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "-1";
        } else {
            String returnname = ((CfStylesheet) value).getName();
            return returnname; 
        }
    }
    
}
