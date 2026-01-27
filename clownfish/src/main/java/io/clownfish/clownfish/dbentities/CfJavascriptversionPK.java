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
import javax.validation.constraints.NotNull;

/**
 *
 * @author sulzbachr
 */
@Embeddable
public class CfJavascriptversionPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "javascriptref")
    private long javascriptref;
    @Basic(optional = false)
    @NotNull
    @Column(name = "version")
    private long version;

    public CfJavascriptversionPK() {
    }

    public CfJavascriptversionPK(long javascriptref, long version) {
        this.javascriptref = javascriptref;
        this.version = version;
    }

    public long getJavascriptref() {
        return javascriptref;
    }

    public void setJavascriptref(long javascriptref) {
        this.javascriptref = javascriptref;
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
        hash += (int) javascriptref;
        hash += (int) version;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfJavascriptversionPK)) {
            return false;
        }
        CfJavascriptversionPK other = (CfJavascriptversionPK) object;
        if (this.javascriptref != other.javascriptref) {
            return false;
        }
        if (this.version != other.version) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfJavascriptversionPK[ javascriptref=" + javascriptref + ", version=" + version + " ]";
    }
    
}
