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
package io.clownfish.clownfish.dbentities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

/**
 *
 * @author sulzbachr
 */
@Embeddable
public class CfStylesheetversionPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "stylesheetref")
    private long stylesheetref;
    @Basic(optional = false)
    @NotNull
    @Column(name = "version")
    private long version;

    public CfStylesheetversionPK() {
    }

    public CfStylesheetversionPK(long stylesheetref, long version) {
        this.stylesheetref = stylesheetref;
        this.version = version;
    }

    public long getStylesheetref() {
        return stylesheetref;
    }

    public void setStylesheetref(long stylesheetref) {
        this.stylesheetref = stylesheetref;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) stylesheetref;
        hash += (int) version;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfStylesheetversionPK)) {
            return false;
        }
        CfStylesheetversionPK other = (CfStylesheetversionPK) object;
        if (this.stylesheetref != other.stylesheetref) {
            return false;
        }
        if (this.version != other.version) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfStylesheetversionPK[ stylesheetref=" + stylesheetref + ", version=" + version + " ]";
    }
    
}
