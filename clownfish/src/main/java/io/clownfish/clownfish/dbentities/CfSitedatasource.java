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
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author sulzbachr
 */
@Entity
@Table(name = "cf_sitedatasource", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfSitedatasource.findAll", query = "SELECT c FROM CfSitedatasource c"),
    @NamedQuery(name = "CfSitedatasource.findBySiteref", query = "SELECT c FROM CfSitedatasource c WHERE c.cfSitedatasourcePK.siteref = :siteref"),
    @NamedQuery(name = "CfSitedatasource.findByDatasourceref", query = "SELECT c FROM CfSitedatasource c WHERE c.cfSitedatasourcePK.datasourceref = :datasourceref")})
public class CfSitedatasource implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfSitedatasourcePK cfSitedatasourcePK;

    public CfSitedatasource() {
    }

    public CfSitedatasource(CfSitedatasourcePK cfSitedatasourcePK) {
        this.cfSitedatasourcePK = cfSitedatasourcePK;
    }

    public CfSitedatasource(long siteref, long datasourceref) {
        this.cfSitedatasourcePK = new CfSitedatasourcePK(siteref, datasourceref);
    }

    public CfSitedatasourcePK getCfSitedatasourcePK() {
        return cfSitedatasourcePK;
    }

    public void setCfSitedatasourcePK(CfSitedatasourcePK cfSitedatasourcePK) {
        this.cfSitedatasourcePK = cfSitedatasourcePK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cfSitedatasourcePK != null ? cfSitedatasourcePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfSitedatasource)) {
            return false;
        }
        CfSitedatasource other = (CfSitedatasource) object;
        if ((this.cfSitedatasourcePK == null && other.cfSitedatasourcePK != null) || (this.cfSitedatasourcePK != null && !this.cfSitedatasourcePK.equals(other.cfSitedatasourcePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfSitedatasource[ cfSitedatasourcePK=" + cfSitedatasourcePK + " ]";
    }
    
}
