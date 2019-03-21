package io.clownfish.clownfish.converter;

import KNSAPTools.SAPConnection;
import io.clownfish.clownfish.beans.SiteTreeBean;
import io.clownfish.clownfish.sap.RFC_FUNCTION_SEARCH;
import io.clownfish.clownfish.sap.models.RfcFunction;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedProperty;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@ViewScoped
@Named("rfcFunctionConverterBean")
@FacesConverter(value = "rfcFunctionConverter")
@Component
public class RfcFunctionConverter implements Converter, Serializable {
    private List<RfcFunction> rfcfunctionlist;
    @ManagedProperty(value="#{sitetree}")
    @Autowired SiteTreeBean sitetree;
    
    public static final String SAPCONNECTION = "sapconnection.props";
    private static SAPConnection sapc = null;
    
    @PostConstruct
    public void init() {
        //sapc = new SAPConnection(SAPCONNECTION, "Gemini2");
    }

    public SiteTreeBean getSitetree() {
        return sitetree;
    }

    public void setSitetree(SiteTreeBean sitetree) {
        this.sitetree = sitetree;
    }
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value.compareToIgnoreCase("-1") == 0) {
            return null;
        } else {
            rfcfunctionlist = new RFC_FUNCTION_SEARCH(sapc).getRfcFunctionsList(sitetree.getSelectedrfcgroup().getName());
            for (RfcFunction rfcfunction : rfcfunctionlist) {
                if (rfcfunction.getName().compareToIgnoreCase(value) == 0 ) {
                    return (Object) rfcfunction;
                }
            }
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "-1";
        } else {
            return ((RfcFunction) value).getName();
        }
    }
    
}
