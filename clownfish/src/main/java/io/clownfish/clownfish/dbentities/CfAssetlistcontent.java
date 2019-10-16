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
@Table(name = "cf_assetlistcontent", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfAssetlistcontent.findAll", query = "SELECT c FROM CfAssetlistcontent c"),
    @NamedQuery(name = "CfAssetlistcontent.findByAssetlistref", query = "SELECT c FROM CfAssetlistcontent c WHERE c.cfAssetlistcontentPK.assetlistref = :assetlistref"),
    @NamedQuery(name = "CfAssetlistcontent.findByAssetref", query = "SELECT c FROM CfAssetlistcontent c WHERE c.cfAssetlistcontentPK.assetref = :assetref")})
public class CfAssetlistcontent implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfAssetlistcontentPK cfAssetlistcontentPK;

    public CfAssetlistcontent() {
    }

    public CfAssetlistcontent(CfAssetlistcontentPK cfAssetlistcontentPK) {
        this.cfAssetlistcontentPK = cfAssetlistcontentPK;
    }

    public CfAssetlistcontent(long assetlistref, long assetref) {
        this.cfAssetlistcontentPK = new CfAssetlistcontentPK(assetlistref, assetref);
    }

    public CfAssetlistcontentPK getCfAssetlistcontentPK() {
        return cfAssetlistcontentPK;
    }

    public void setCfAssetlistcontentPK(CfAssetlistcontentPK cfAssetlistcontentPK) {
        this.cfAssetlistcontentPK = cfAssetlistcontentPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cfAssetlistcontentPK != null ? cfAssetlistcontentPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfAssetlistcontent)) {
            return false;
        }
        CfAssetlistcontent other = (CfAssetlistcontent) object;
        if ((this.cfAssetlistcontentPK == null && other.cfAssetlistcontentPK != null) || (this.cfAssetlistcontentPK != null && !this.cfAssetlistcontentPK.equals(other.cfAssetlistcontentPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfAssetlistcontent[ cfAssetlistcontentPK=" + cfAssetlistcontentPK + " ]";
    }
    
}
