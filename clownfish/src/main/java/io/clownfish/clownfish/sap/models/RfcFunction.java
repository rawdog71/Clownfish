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
public class RfcFunction implements Serializable {
    private @Getter @Setter String name;
    private @Getter @Setter String groupname;
    private @Getter @Setter String appl;
    private @Getter @Setter String host;
    private @Getter @Setter String description;

    public RfcFunction() {
    }

    public RfcFunction(String name, String groupname, String appl, String host, String description) {
        this.name = name;
        this.groupname = groupname;
        this.appl = appl;
        this.host = host;
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
        if (!(object instanceof RfcFunction)) {
            return false;
        }
        RfcFunction other = (RfcFunction) object;
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
