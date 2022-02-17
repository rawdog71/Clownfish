/*
 * Copyright 2022 raine.
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
import java.math.BigInteger;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author raine
 */
@Entity
@Table(name = "cf_layoutcontent", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfLayoutcontent.findAll", query = "SELECT c FROM CfLayoutcontent c"),
    @NamedQuery(name = "CfLayoutcontent.findBySiteref", query = "SELECT c FROM CfLayoutcontent c WHERE c.cfLayoutcontentPK.siteref = :siteref"),
    @NamedQuery(name = "CfLayoutcontent.findBySiterefAndTemplateref", query = "SELECT c FROM CfLayoutcontent c WHERE c.cfLayoutcontentPK.siteref = :siteref AND c.cfLayoutcontentPK.divref = :templateref"),
    @NamedQuery(name = "CfLayoutcontent.findBySiterefAndTemplaterefAndContenttype", query = "SELECT c FROM CfLayoutcontent c WHERE c.cfLayoutcontentPK.siteref = :siteref AND c.cfLayoutcontentPK.divref = :templateref AND c.cfLayoutcontentPK.contenttype = :contenttype"),
    @NamedQuery(name = "CfLayoutcontent.findBySiterefAndTemplaterefAndContenttypeAndLfdnr", query = "SELECT c FROM CfLayoutcontent c WHERE c.cfLayoutcontentPK.siteref = :siteref AND c.cfLayoutcontentPK.divref = :templateref AND c.cfLayoutcontentPK.contenttype = :contenttype AND c.cfLayoutcontentPK.lfdnr = :lfdnr"),
    @NamedQuery(name = "CfLayoutcontent.findByDivref", query = "SELECT c FROM CfLayoutcontent c WHERE c.cfLayoutcontentPK.divref = :divref"),
    @NamedQuery(name = "CfLayoutcontent.findByContenttype", query = "SELECT c FROM CfLayoutcontent c WHERE c.cfLayoutcontentPK.contenttype = :contenttype"),
    @NamedQuery(name = "CfLayoutcontent.findByLfdnr", query = "SELECT c FROM CfLayoutcontent c WHERE c.cfLayoutcontentPK.lfdnr = :lfdnr"),
    @NamedQuery(name = "CfLayoutcontent.findByContentref", query = "SELECT c FROM CfLayoutcontent c WHERE c.contentref = :contentref")})
public class CfLayoutcontent implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfLayoutcontentPK cfLayoutcontentPK;
    @Column(name = "contentref")
    private BigInteger contentref;
    @Column(name = "preview_contentref")
    private BigInteger preview_contentref;

    public CfLayoutcontent() {
    }

    public CfLayoutcontent(CfLayoutcontentPK cfLayoutcontentPK) {
        this.cfLayoutcontentPK = cfLayoutcontentPK;
    }

    public CfLayoutcontent(long siteref, long divref, String contenttype, int lfdnr) {
        this.cfLayoutcontentPK = new CfLayoutcontentPK(siteref, divref, contenttype, lfdnr);
    }

    public CfLayoutcontentPK getCfLayoutcontentPK() {
        return cfLayoutcontentPK;
    }

    public void setCfLayoutcontentPK(CfLayoutcontentPK cfLayoutcontentPK) {
        this.cfLayoutcontentPK = cfLayoutcontentPK;
    }

    public BigInteger getContentref() {
        return contentref;
    }

    public void setContentref(BigInteger contentref) {
        this.contentref = contentref;
    }

    public BigInteger getPreview_contentref() {
        return preview_contentref;
    }

    public void setPreview_contentref(BigInteger preview_contentref) {
        this.preview_contentref = preview_contentref;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cfLayoutcontentPK != null ? cfLayoutcontentPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfLayoutcontent)) {
            return false;
        }
        CfLayoutcontent other = (CfLayoutcontent) object;
        if ((this.cfLayoutcontentPK == null && other.cfLayoutcontentPK != null) || (this.cfLayoutcontentPK != null && !this.cfLayoutcontentPK.equals(other.cfLayoutcontentPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfLayoutcontent[ cfLayoutcontentPK=" + cfLayoutcontentPK + " ]";
    }
    
}
