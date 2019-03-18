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
@Table(name = "cf_sitesaprfc", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfSitesaprfc.findAll", query = "SELECT c FROM CfSitesaprfc c"),
    @NamedQuery(name = "CfSitesaprfc.findBySiteref", query = "SELECT c FROM CfSitesaprfc c WHERE c.cfSitesaprfcPK.siteref = :siteref"),
    @NamedQuery(name = "CfSitesaprfc.findByRfcgroup", query = "SELECT c FROM CfSitesaprfc c WHERE c.cfSitesaprfcPK.rfcgroup = :rfcgroup"),
    @NamedQuery(name = "CfSitesaprfc.findByRfcfunction", query = "SELECT c FROM CfSitesaprfc c WHERE c.cfSitesaprfcPK.rfcfunction = :rfcfunction")})
public class CfSitesaprfc implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfSitesaprfcPK cfSitesaprfcPK;

    public CfSitesaprfc() {
    }

    public CfSitesaprfc(CfSitesaprfcPK cfSitesaprfcPK) {
        this.cfSitesaprfcPK = cfSitesaprfcPK;
    }

    public CfSitesaprfc(long siteref, String rfcgroup, String rfcfunction) {
        this.cfSitesaprfcPK = new CfSitesaprfcPK(siteref, rfcgroup, rfcfunction);
    }

    public CfSitesaprfcPK getCfSitesaprfcPK() {
        return cfSitesaprfcPK;
    }

    public void setCfSitesaprfcPK(CfSitesaprfcPK cfSitesaprfcPK) {
        this.cfSitesaprfcPK = cfSitesaprfcPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cfSitesaprfcPK != null ? cfSitesaprfcPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfSitesaprfc)) {
            return false;
        }
        CfSitesaprfc other = (CfSitesaprfc) object;
        if ((this.cfSitesaprfcPK == null && other.cfSitesaprfcPK != null) || (this.cfSitesaprfcPK != null && !this.cfSitesaprfcPK.equals(other.cfSitesaprfcPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfSitesaprfc[ cfSitesaprfcPK=" + cfSitesaprfcPK + " ]";
    }
    
}
