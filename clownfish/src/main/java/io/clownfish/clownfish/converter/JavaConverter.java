package io.clownfish.clownfish.converter;

import io.clownfish.clownfish.dbentities.CfJava;
import io.clownfish.clownfish.serviceinterface.CfJavaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import java.io.Serializable;

@Scope("session")
@Named("javaConverterBean")
@FacesConverter(value = "javaConverter")
@Component
public class JavaConverter implements Converter, Serializable
{
    @Autowired
    transient CfJavaService cfjavaservice;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value)
    {
        if (value.compareToIgnoreCase("-1") == 0)
        {
            return null;
        }
        else
        {
            Object o = cfjavaservice.findByName(value);
            return o;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value)
    {
        if (value == null)
        {
            return "-1";
        }
        else
        {
            String returnname = ((CfJava) value).getName();
            return returnname;
        }
    }
}
