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
public class CfSiteassetlistPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "siteref")
    private long siteref;
    @Basic(optional = false)
    @NotNull
    @Column(name = "assetlistref")
    private long assetlistref;

    public CfSiteassetlistPK() {
    }

    public CfSiteassetlistPK(long siteref, long assetlistref) {
        this.siteref = siteref;
        this.assetlistref = assetlistref;
    }

    public long getSiteref() {
        return siteref;
    }

    public void setSiteref(long siteref) {
        this.siteref = siteref;
    }

    public long getAssetlistref() {
        return assetlistref;
    }

    public void setAssetlistref(long assetlistref) {
        this.assetlistref = assetlistref;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) siteref;
        hash += (int) assetlistref;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfSiteassetlistPK)) {
            return false;
        }
        CfSiteassetlistPK other = (CfSiteassetlistPK) object;
        if (this.siteref != other.siteref) {
            return false;
        }
        if (this.assetlistref != other.assetlistref) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfSiteassetlistPK[ siteref=" + siteref + ", assetlistref=" + assetlistref + " ]";
    }
    
}
