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
package io.clownfish.clownfish.sap.models;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author sulzbachr
 */
public class RfcGroup implements Serializable {
    private @Getter @Setter String name;
    private @Getter @Setter String description;

    public RfcGroup() {
    }

    public RfcGroup(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (name != null ? name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RfcGroup)) {
            return false;
        }
        RfcGroup other = (RfcGroup) object;
        if ((this.name == null && other.name != null) || (this.name != null && !this.name.equals(other.name))) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
