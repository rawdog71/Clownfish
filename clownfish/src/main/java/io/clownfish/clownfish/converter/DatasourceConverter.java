package io.clownfish.clownfish.converter;

import io.clownfish.clownfish.dbentities.CfDatasource;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
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
@Named("datasourceConverterBean")
@FacesConverter(value = "datasourceConverter")
@Component
public class DatasourceConverter implements Converter, Serializable {
    @Autowired CfDatasourceService cfdatasourceservice;
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value.compareToIgnoreCase("-1") == 0) {
            return null;
        } else {
            //Object o = em.createNamedQuery("Kndatasource.findById").setParameter("id", new Long(value)).getSingleResult();
            Object o = cfdatasourceservice.findById(new Long(value));
            return o;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "-1";
        } else {
            return ((CfDatasource) value).getId().toString(); 
        }
    }
}
