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
public class CfSitedatasourcePK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "siteref")
    private long siteref;
    @Basic(optional = false)
    @NotNull
    @Column(name = "datasourceref")
    private long datasourceref;

    public CfSitedatasourcePK() {
    }

    public CfSitedatasourcePK(long siteref, long datasourceref) {
        this.siteref = siteref;
        this.datasourceref = datasourceref;
    }

    public long getSiteref() {
        return siteref;
    }

    public void setSiteref(long siteref) {
        this.siteref = siteref;
    }

    public long getDatasourceref() {
        return datasourceref;
    }

    public void setDatasourceref(long datasourceref) {
        this.datasourceref = datasourceref;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) siteref;
        hash += (int) datasourceref;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfSitedatasourcePK)) {
            return false;
        }
        CfSitedatasourcePK other = (CfSitedatasourcePK) object;
        if (this.siteref != other.siteref) {
            return false;
        }
        if (this.datasourceref != other.datasourceref) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfSitedatasourcePK[ siteref=" + siteref + ", datasourceref=" + datasourceref + " ]";
    }
    
}
