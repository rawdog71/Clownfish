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
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author SulzbachR
 */
@Entity
@Table(name = "cf_sitekeywordlist", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfSitekeywordlist.findAll", query = "SELECT c FROM CfSitekeywordlist c"),
    @NamedQuery(name = "CfSitekeywordlist.findByKeywordlistref", query = "SELECT c FROM CfSitekeywordlist c WHERE c.cfSitekeywordlistPK.keywordlistref = :keywordlistref"),
    @NamedQuery(name = "CfSitekeywordlist.findBySiteref", query = "SELECT c FROM CfSitekeywordlist c WHERE c.cfSitekeywordlistPK.siteref = :siteref")})
public class CfSitekeywordlist implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfSitekeywordlistPK cfSitekeywordlistPK;

    public CfSitekeywordlist() {
    }

    public CfSitekeywordlist(CfSitekeywordlistPK cfSitekeywordlistPK) {
        this.cfSitekeywordlistPK = cfSitekeywordlistPK;
    }

    public CfSitekeywordlist(long keywordlistref, long siteref) {
        this.cfSitekeywordlistPK = new CfSitekeywordlistPK(keywordlistref, siteref);
    }

    public CfSitekeywordlistPK getCfSitekeywordlistPK() {
        return cfSitekeywordlistPK;
    }

    public void setCfSitekeywordlistPK(CfSitekeywordlistPK cfSitekeywordlistPK) {
        this.cfSitekeywordlistPK = cfSitekeywordlistPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cfSitekeywordlistPK != null ? cfSitekeywordlistPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfSitekeywordlist)) {
            return false;
        }
        CfSitekeywordlist other = (CfSitekeywordlist) object;
        if ((this.cfSitekeywordlistPK == null && other.cfSitekeywordlistPK != null) || (this.cfSitekeywordlistPK != null && !this.cfSitekeywordlistPK.equals(other.cfSitekeywordlistPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfSitekeywordlist[ cfSitekeywordlistPK=" + cfSitekeywordlistPK + " ]";
    }
    
}
