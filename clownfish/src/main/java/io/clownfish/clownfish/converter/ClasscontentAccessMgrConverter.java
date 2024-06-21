package io.clownfish.clownfish.converter;

import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
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
@Named("classcontentAccessMgrConverterBean")
@FacesConverter(value = "classcontentAccessMgrConverter")
@Component
public class ClasscontentAccessMgrConverter implements Converter, Serializable {
    @Autowired
    transient CfClasscontentService cfclasscontentservice;
    @Autowired transient CfAttributcontentService cfAttributcontentService;
    @Autowired transient CfAttributService cfAttributService;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value.compareToIgnoreCase("-1") == 0) {
            return null;
        } else {
            Object o = cfclasscontentservice.findById(Long.parseLong(value));
            return o;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "-1";
        } else {
            CfClasscontent cc = (CfClasscontent)value;
            CfAttribut mail = cfAttributService.findByNameAndClassref("email", cc.getClassref());
            if (mail == null) {
                return "-1";
            } else {
                CfAttributcontent att = cfAttributcontentService.findByAttributrefAndClasscontentref(mail, cc);
                return att.getContentString();
            }
        }
    }
}