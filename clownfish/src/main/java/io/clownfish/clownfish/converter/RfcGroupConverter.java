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
import io.clownfish.clownfish.beans.PropertyList;
import io.clownfish.clownfish.sap.RFC_GROUP_SEARCH;
import io.clownfish.clownfish.sap.models.RfcGroup;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
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
@Named("rfcGroupConverterBean")
@FacesConverter(value = "rfcGroupConverter")
@Component
public class RfcGroupConverter implements Converter, Serializable {
    private List<RfcGroup> rfcgrouplist;
    private static SAPConnection sapc = null;
    private Map<String, String> propertymap = null;
    private boolean sapSupport = false;
    @Autowired transient PropertyList propertylist;
    
    public static final String SAPCONNECTION = "sapconnection.props";
    
    @PostConstruct
    public void init() {
        propertymap = propertylist.fillPropertyMap();
        String sapSupportProp = propertymap.get("sap_support");
        if (sapSupportProp.compareToIgnoreCase("true") == 0) {
            sapSupport = true;
        }
        if (sapSupport) {
            sapc = new SAPConnection(SAPCONNECTION, "Clownfish3");
        }
    }
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value.compareToIgnoreCase("-1") == 0) {
            return null;
        } else {
            if (sapSupport) {
                rfcgrouplist = new RFC_GROUP_SEARCH(sapc).getRfcGroupList();
                for (RfcGroup rfcgroup : rfcgrouplist) {
                    if (rfcgroup.getName().compareToIgnoreCase(value) == 0 ) {
                        return (Object) rfcgroup;
                    }
                }
                return null;
            } else {
                return null;
            }
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
