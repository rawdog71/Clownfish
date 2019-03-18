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
@Table(name = "cf_assetkeyword", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfAssetkeyword.findAll", query = "SELECT c FROM CfAssetkeyword c"),
    @NamedQuery(name = "CfAssetkeyword.findByAssetref", query = "SELECT c FROM CfAssetkeyword c WHERE c.cfAssetkeywordPK.assetref = :assetref"),
    @NamedQuery(name = "CfAssetkeyword.findByKeywordref", query = "SELECT c FROM CfAssetkeyword c WHERE c.cfAssetkeywordPK.keywordref = :keywordref")})
public class CfAssetkeyword implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfAssetkeywordPK cfAssetkeywordPK;

    public CfAssetkeyword() {
    }

    public CfAssetkeyword(CfAssetkeywordPK cfAssetkeywordPK) {
        this.cfAssetkeywordPK = cfAssetkeywordPK;
    }

    public CfAssetkeyword(long assetref, long keywordref) {
        this.cfAssetkeywordPK = new CfAssetkeywordPK(assetref, keywordref);
    }

    public CfAssetkeywordPK getCfAssetkeywordPK() {
        return cfAssetkeywordPK;
    }

    public void setCfAssetkeywordPK(CfAssetkeywordPK cfAssetkeywordPK) {
        this.cfAssetkeywordPK = cfAssetkeywordPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cfAssetkeywordPK != null ? cfAssetkeywordPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfAssetkeyword)) {
            return false;
        }
        CfAssetkeyword other = (CfAssetkeyword) object;
        if ((this.cfAssetkeywordPK == null && other.cfAssetkeywordPK != null) || (this.cfAssetkeywordPK != null && !this.cfAssetkeywordPK.equals(other.cfAssetkeywordPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfAssetkeyword[ cfAssetkeywordPK=" + cfAssetkeywordPK + " ]";
    }
    
}
