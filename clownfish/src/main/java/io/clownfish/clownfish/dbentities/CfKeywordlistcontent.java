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
@Table(name = "cf_keywordlistcontent", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfKeywordlistcontent.findAll", query = "SELECT c FROM CfKeywordlistcontent c"),
    @NamedQuery(name = "CfKeywordlistcontent.findByKeywordlistref", query = "SELECT c FROM CfKeywordlistcontent c WHERE c.cfKeywordlistcontentPK.keywordlistref = :keywordlistref"),
    @NamedQuery(name = "CfKeywordlistcontent.findByKeywordref", query = "SELECT c FROM CfKeywordlistcontent c WHERE c.cfKeywordlistcontentPK.keywordref = :keywordref")})
public class CfKeywordlistcontent implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfKeywordlistcontentPK cfKeywordlistcontentPK;

    public CfKeywordlistcontent() {
    }

    public CfKeywordlistcontent(CfKeywordlistcontentPK cfKeywordlistcontentPK) {
        this.cfKeywordlistcontentPK = cfKeywordlistcontentPK;
    }

    public CfKeywordlistcontent(long keywordlistref, long keywordref) {
        this.cfKeywordlistcontentPK = new CfKeywordlistcontentPK(keywordlistref, keywordref);
    }

    public CfKeywordlistcontentPK getCfKeywordlistcontentPK() {
        return cfKeywordlistcontentPK;
    }

    public void setCfKeywordlistcontentPK(CfKeywordlistcontentPK cfKeywordlistcontentPK) {
        this.cfKeywordlistcontentPK = cfKeywordlistcontentPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cfKeywordlistcontentPK != null ? cfKeywordlistcontentPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfKeywordlistcontent)) {
            return false;
        }
        CfKeywordlistcontent other = (CfKeywordlistcontent) object;
        if ((this.cfKeywordlistcontentPK == null && other.cfKeywordlistcontentPK != null) || (this.cfKeywordlistcontentPK != null && !this.cfKeywordlistcontentPK.equals(other.cfKeywordlistcontentPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfKeywordlistcontent[ cfKeywordlistcontentPK=" + cfKeywordlistcontentPK + " ]";
    }
    
}
