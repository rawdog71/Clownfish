/*
 * Copyright 2023 SulzbachR.
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
import javax.validation.constraints.Size;

/**
 *
 * @author SulzbachR
 */
@Embeddable
public class CfApiPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "siteref")
    private long siteref;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "keyname")
    private String keyname;

    public CfApiPK() {
    }

    public CfApiPK(long siteref, String keyname) {
        this.siteref = siteref;
        this.keyname = keyname;
    }

    public long getSiteref() {
        return siteref;
    }

    public void setSiteref(long siteref) {
        this.siteref = siteref;
    }

    public String getKeyname() {
        return keyname;
    }

    public void setKeyname(String keyname) {
        this.keyname = keyname;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) siteref;
        hash += (keyname != null ? keyname.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfApiPK)) {
            return false;
        }
        CfApiPK other = (CfApiPK) object;
        if (this.siteref != other.siteref) {
            return false;
        }
        if ((this.keyname == null && other.keyname != null) || (this.keyname != null && !this.keyname.equals(other.keyname))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfApiPK[ siteref=" + siteref + ", keyname=" + keyname + " ]";
    }
    
}
