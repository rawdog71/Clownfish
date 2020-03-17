/*
 * Copyright 2020 SulzbachR.
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
 * @author SulzbachR
 */
@Embeddable
public class CfSitekeywordlistPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "keywordlistref")
    private long keywordlistref;
    @Basic(optional = false)
    @NotNull
    @Column(name = "siteref")
    private long siteref;

    public CfSitekeywordlistPK() {
    }

    public CfSitekeywordlistPK(long keywordlistref, long siteref) {
        this.keywordlistref = keywordlistref;
        this.siteref = siteref;
    }

    public long getKeywordlistref() {
        return keywordlistref;
    }

    public void setKeywordlistref(long keywordlistref) {
        this.keywordlistref = keywordlistref;
    }

    public long getSiteref() {
        return siteref;
    }

    public void setSiteref(long siteref) {
        this.siteref = siteref;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) keywordlistref;
        hash += (int) siteref;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfSitekeywordlistPK)) {
            return false;
        }
        CfSitekeywordlistPK other = (CfSitekeywordlistPK) object;
        if (this.keywordlistref != other.keywordlistref) {
            return false;
        }
        if (this.siteref != other.siteref) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfSitekeywordlistPK[ keywordlistref=" + keywordlistref + ", siteref=" + siteref + " ]";
    }
    
}
