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
@Table(name = "cf_classcontentkeyword", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfClasscontentkeyword.findAll", query = "SELECT c FROM CfClasscontentkeyword c"),
    @NamedQuery(name = "CfClasscontentkeyword.findByClasscontentref", query = "SELECT c FROM CfClasscontentkeyword c WHERE c.cfClasscontentkeywordPK.classcontentref = :classcontentref"),
    @NamedQuery(name = "CfClasscontentkeyword.findByKeywordref", query = "SELECT c FROM CfClasscontentkeyword c WHERE c.cfClasscontentkeywordPK.keywordref = :keywordref")})
public class CfClasscontentkeyword implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfClasscontentkeywordPK cfClasscontentkeywordPK;

    public CfClasscontentkeyword() {
    }

    public CfClasscontentkeyword(CfClasscontentkeywordPK cfClasscontentkeywordPK) {
        this.cfClasscontentkeywordPK = cfClasscontentkeywordPK;
    }

    public CfClasscontentkeyword(long classcontentref, long keywordref) {
        this.cfClasscontentkeywordPK = new CfClasscontentkeywordPK(classcontentref, keywordref);
    }

    public CfClasscontentkeywordPK getCfClasscontentkeywordPK() {
        return cfClasscontentkeywordPK;
    }

    public void setCfClasscontentkeywordPK(CfClasscontentkeywordPK cfClasscontentkeywordPK) {
        this.cfClasscontentkeywordPK = cfClasscontentkeywordPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cfClasscontentkeywordPK != null ? cfClasscontentkeywordPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfClasscontentkeyword)) {
            return false;
        }
        CfClasscontentkeyword other = (CfClasscontentkeyword) object;
        if ((this.cfClasscontentkeywordPK == null && other.cfClasscontentkeywordPK != null) || (this.cfClasscontentkeywordPK != null && !this.cfClasscontentkeywordPK.equals(other.cfClasscontentkeywordPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfClasscontentkeyword[ cfClasscontentkeywordPK=" + cfClasscontentkeywordPK + " ]";
    }
    
}
