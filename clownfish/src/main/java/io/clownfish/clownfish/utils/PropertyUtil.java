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
package io.clownfish.clownfish.utils;

import io.clownfish.clownfish.beans.PropertyList;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Scope("singleton")
@Component
public class PropertyUtil {
    private @Getter @Setter Map<String, String> propertymap = null;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(PropertyUtil.class);
    
    public String getPropertyValue(String key) {
        return propertymap.get(key);
    }
   
    public PropertyUtil(PropertyList propertylist) {
        if (propertymap == null) {
            // read all System Properties of the property table
            propertymap = propertylist.fillPropertyMap();
        }
    }
    
    public String getPropertySwitch(String property, int propertyfield) {
        String propertySwitch = getPropertymap().get(property);
        if (propertySwitch == null) {
            propertySwitch = "off";
        }
        switch (propertyfield) {
            case 1:
                propertySwitch = "on";
                break;
            case 2:
                propertySwitch = "off";
                break;
        }
        return propertySwitch;
    }
    
    public int getPropertyInt(String propertyfield, int defaultvalue) {
        int value;
        String intvalue = getPropertymap().get(propertyfield);
        if (null != intvalue) {
            try {
                value = Integer.parseInt(intvalue);
            } catch (NumberFormatException nex) {
                value = defaultvalue;
                LOGGER.warn(nex.getMessage());
            }
        } else {
            value = defaultvalue;
        }
        return value;
    }
    
    public boolean getPropertyBoolean(String propertyfield, boolean defaultvalue) {
        boolean value;
        String boolvalue = getPropertymap().get(propertyfield);
        if (null != boolvalue) {
            try {
                if ((0 == boolvalue.compareToIgnoreCase("on")) || (0 == boolvalue.compareToIgnoreCase("true"))) {
                    value = true;
                } else {
                    value = false;
                }
            } catch (NumberFormatException nex) {
                value = defaultvalue;
                LOGGER.warn(nex.getMessage());
            }
        } else {
            value = defaultvalue;
        }
        return value;
    }
}
