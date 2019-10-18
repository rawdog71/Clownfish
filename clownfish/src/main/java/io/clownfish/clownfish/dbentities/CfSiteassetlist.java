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
@Table(name = "cf_siteassetlist", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfSiteassetlist.findAll", query = "SELECT c FROM CfSiteassetlist c"),
    @NamedQuery(name = "CfSiteassetlist.findBySiteref", query = "SELECT c FROM CfSiteassetlist c WHERE c.cfSiteassetlistPK.siteref = :siteref"),
    @NamedQuery(name = "CfSiteassetlist.findByAssetlistref", query = "SELECT c FROM CfSiteassetlist c WHERE c.cfSiteassetlistPK.assetlistref = :assetlistref")})
public class CfSiteassetlist implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfSiteassetlistPK cfSiteassetlistPK;

    public CfSiteassetlist() {
    }

    public CfSiteassetlist(CfSiteassetlistPK cfSiteassetlistPK) {
        this.cfSiteassetlistPK = cfSiteassetlistPK;
    }

    public CfSiteassetlist(long siteref, long assetlistref) {
        this.cfSiteassetlistPK = new CfSiteassetlistPK(siteref, assetlistref);
    }

    public CfSiteassetlistPK getCfSiteassetlistPK() {
        return cfSiteassetlistPK;
    }

    public void setCfSiteassetlistPK(CfSiteassetlistPK cfSiteassetlistPK) {
        this.cfSiteassetlistPK = cfSiteassetlistPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cfSiteassetlistPK != null ? cfSiteassetlistPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfSiteassetlist)) {
            return false;
        }
        CfSiteassetlist other = (CfSiteassetlist) object;
        if ((this.cfSiteassetlistPK == null && other.cfSiteassetlistPK != null) || (this.cfSiteassetlistPK != null && !this.cfSiteassetlistPK.equals(other.cfSiteassetlistPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfSiteassetlist[ cfSiteassetlistPK=" + cfSiteassetlistPK + " ]";
    }
    
}
