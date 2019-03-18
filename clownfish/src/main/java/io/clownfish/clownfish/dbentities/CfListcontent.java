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
@Table(name = "cf_listcontent", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfListcontent.findAll", query = "SELECT c FROM CfListcontent c"),
    @NamedQuery(name = "CfListcontent.findByListref", query = "SELECT c FROM CfListcontent c WHERE c.cfListcontentPK.listref = :listref"),
    @NamedQuery(name = "CfListcontent.findByClasscontentref", query = "SELECT c FROM CfListcontent c WHERE c.cfListcontentPK.classcontentref = :classcontentref")})
public class CfListcontent implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfListcontentPK cfListcontentPK;

    public CfListcontent() {
    }

    public CfListcontent(CfListcontentPK cfListcontentPK) {
        this.cfListcontentPK = cfListcontentPK;
    }

    public CfListcontent(long listref, long classcontentref) {
        this.cfListcontentPK = new CfListcontentPK(listref, classcontentref);
    }

    public CfListcontentPK getCfListcontentPK() {
        return cfListcontentPK;
    }

    public void setCfListcontentPK(CfListcontentPK cfListcontentPK) {
        this.cfListcontentPK = cfListcontentPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cfListcontentPK != null ? cfListcontentPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfListcontent)) {
            return false;
        }
        CfListcontent other = (CfListcontent) object;
        if ((this.cfListcontentPK == null && other.cfListcontentPK != null) || (this.cfListcontentPK != null && !this.cfListcontentPK.equals(other.cfListcontentPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfListcontent[ cfListcontentPK=" + cfListcontentPK + " ]";
    }
    
}
