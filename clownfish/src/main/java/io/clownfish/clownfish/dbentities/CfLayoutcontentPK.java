/*
 * Copyright 2022 raine.
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
 * @author raine
 */
@Embeddable
public class CfLayoutcontentPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "siteref")
    private long siteref;
    @Basic(optional = false)
    @NotNull
    @Column(name = "divref")
    private long divref;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2)
    @Column(name = "contenttype")
    private String contenttype;
    @Basic(optional = false)
    @NotNull
    @Column(name = "lfdnr")
    private int lfdnr;

    public CfLayoutcontentPK() {
    }

    public CfLayoutcontentPK(long siteref, long divref, String contenttype, int lfdnr) {
        this.siteref = siteref;
        this.divref = divref;
        this.contenttype = contenttype;
        this.lfdnr = lfdnr;
    }

    public long getSiteref() {
        return siteref;
    }

    public void setSiteref(long siteref) {
        this.siteref = siteref;
    }

    public long getDivref() {
        return divref;
    }

    public void setDivref(long divref) {
        this.divref = divref;
    }

    public String getContenttype() {
        return contenttype;
    }

    public void setContenttype(String contenttype) {
        this.contenttype = contenttype;
    }

    public int getLfdnr() {
        return lfdnr;
    }

    public void setLfdnr(int lfdnr) {
        this.lfdnr = lfdnr;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) siteref;
        hash += (int) divref;
        hash += (contenttype != null ? contenttype.hashCode() : 0);
        hash += (int) lfdnr;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfLayoutcontentPK)) {
            return false;
        }
        CfLayoutcontentPK other = (CfLayoutcontentPK) object;
        if (this.siteref != other.siteref) {
            return false;
        }
        if (this.divref != other.divref) {
            return false;
        }
        if ((this.contenttype == null && other.contenttype != null) || (this.contenttype != null && !this.contenttype.equals(other.contenttype))) {
            return false;
        }
        if (this.lfdnr != other.lfdnr) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfLayoutcontentPK[ siteref=" + siteref + ", divref=" + divref + ", contenttype=" + contenttype + ", lfdnr=" + lfdnr + " ]";
    }
    
}
