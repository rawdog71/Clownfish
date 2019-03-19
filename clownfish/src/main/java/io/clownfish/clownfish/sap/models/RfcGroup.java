/*
 * Copyright Rainer Sulzbach
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
