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
@Table(name = "cf_sitecontent", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfSitecontent.findAll", query = "SELECT c FROM CfSitecontent c"),
    @NamedQuery(name = "CfSitecontent.findBySiteref", query = "SELECT c FROM CfSitecontent c WHERE c.cfSitecontentPK.siteref = :siteref"),
    @NamedQuery(name = "CfSitecontent.findByClasscontentref", query = "SELECT c FROM CfSitecontent c WHERE c.cfSitecontentPK.classcontentref = :classcontentref")})
public class CfSitecontent implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfSitecontentPK cfSitecontentPK;

    public CfSitecontent() {
    }

    public CfSitecontent(CfSitecontentPK cfSitecontentPK) {
        this.cfSitecontentPK = cfSitecontentPK;
    }

    public CfSitecontent(long siteref, long classcontentref) {
        this.cfSitecontentPK = new CfSitecontentPK(siteref, classcontentref);
    }

    public CfSitecontentPK getCfSitecontentPK() {
        return cfSitecontentPK;
    }

    public void setCfSitecontentPK(CfSitecontentPK cfSitecontentPK) {
        this.cfSitecontentPK = cfSitecontentPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cfSitecontentPK != null ? cfSitecontentPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfSitecontent)) {
            return false;
        }
        CfSitecontent other = (CfSitecontent) object;
        if ((this.cfSitecontentPK == null && other.cfSitecontentPK != null) || (this.cfSitecontentPK != null && !this.cfSitecontentPK.equals(other.cfSitecontentPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfSitecontent[ cfSitecontentPK=" + cfSitecontentPK + " ]";
    }
    
}
