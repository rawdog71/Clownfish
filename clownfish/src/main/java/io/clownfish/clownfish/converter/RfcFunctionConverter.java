/*
 * Copyright 2019 sulzbachr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.clownfish.clownfish.converter;

import de.destrukt.sapconnection.SAPConnection;
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
import javax.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Scope("session")
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
        sapc = new SAPConnection(SAPCONNECTION, "Clownfish2");
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
