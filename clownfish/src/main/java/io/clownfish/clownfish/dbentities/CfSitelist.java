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
@Table(name = "cf_sitelist", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfSitelist.findAll", query = "SELECT c FROM CfSitelist c"),
    @NamedQuery(name = "CfSitelist.findBySiteref", query = "SELECT c FROM CfSitelist c WHERE c.cfSitelistPK.siteref = :siteref"),
    @NamedQuery(name = "CfSitelist.findByListref", query = "SELECT c FROM CfSitelist c WHERE c.cfSitelistPK.listref = :listref")})
public class CfSitelist implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfSitelistPK cfSitelistPK;

    public CfSitelist() {
    }

    public CfSitelist(CfSitelistPK cfSitelistPK) {
        this.cfSitelistPK = cfSitelistPK;
    }

    public CfSitelist(long siteref, long listref) {
        this.cfSitelistPK = new CfSitelistPK(siteref, listref);
    }

    public CfSitelistPK getCfSitelistPK() {
        return cfSitelistPK;
    }

    public void setCfSitelistPK(CfSitelistPK cfSitelistPK) {
        this.cfSitelistPK = cfSitelistPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cfSitelistPK != null ? cfSitelistPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfSitelist)) {
            return false;
        }
        CfSitelist other = (CfSitelist) object;
        if ((this.cfSitelistPK == null && other.cfSitelistPK != null) || (this.cfSitelistPK != null && !this.cfSitelistPK.equals(other.cfSitelistPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfSitelist[ cfSitelistPK=" + cfSitelistPK + " ]";
    }
    
}
