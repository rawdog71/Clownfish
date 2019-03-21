package io.clownfish.clownfish.converter;

import KNSAPTools.SAPConnection;
import io.clownfish.clownfish.sap.RFC_GROUP_SEARCH;
import io.clownfish.clownfish.sap.models.RfcGroup;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@ViewScoped
@Named("rfcGroupConverterBean")
@FacesConverter(value = "rfcGroupConverter")
@Component
public class RfcGroupConverter implements Converter, Serializable {
    private List<RfcGroup> rfcgrouplist;
    private static SAPConnection sapc = null;
    
    public static final String SAPCONNECTION = "sapconnection.props";
    
    @PostConstruct
    public void init() {
        //sapc = new SAPConnection(SAPCONNECTION, "Gemini3");
    }
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value.compareToIgnoreCase("-1") == 0) {
            return null;
        } else {
            rfcgrouplist = new RFC_GROUP_SEARCH(sapc).getRfcGroupList();
            for (RfcGroup rfcgroup : rfcgrouplist) {
                if (rfcgroup.getName().compareToIgnoreCase(value) == 0 ) {
                    return (Object) rfcgroup;
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
            return ((RfcGroup) value).getName();
        }
    }
    
}
