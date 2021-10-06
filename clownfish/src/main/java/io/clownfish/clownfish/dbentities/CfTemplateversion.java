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
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author sulzbachr
 */
@Entity
@Table(name = "cf_templateversion", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfTemplateversion.findAll", query = "SELECT c FROM CfTemplateversion c"),
    @NamedQuery(name = "CfTemplateversion.findByTemplateref", query = "SELECT c FROM CfTemplateversion c WHERE c.cfTemplateversionPK.templateref = :templateref"),
    @NamedQuery(name = "CfTemplateversion.findByVersion", query = "SELECT c FROM CfTemplateversion c WHERE c.cfTemplateversionPK.version = :version"),
    @NamedQuery(name = "CfTemplateversion.findByPK", query = "SELECT c FROM CfTemplateversion c WHERE c.cfTemplateversionPK.templateref = :templateref AND c.cfTemplateversionPK.version = :version"),
    @NamedQuery(name = "CfTemplateversion.findByTstamp", query = "SELECT c FROM CfTemplateversion c WHERE c.tstamp = :tstamp"),
    @NamedQuery(name = "CfTemplateversion.findByCommitedby", query = "SELECT c FROM CfTemplateversion c WHERE c.commitedby = :commitedby"),
    @NamedQuery(name = "CfTemplateversion.findMaxVersion", query = "SELECT MAX(c.cfTemplateversionPK.version) FROM CfTemplateversion c WHERE c.cfTemplateversionPK.templateref = :templateref")
})
public class CfTemplateversion implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfTemplateversionPK cfTemplateversionPK;
    @Lob
    @Column(name = "content")
    private byte[] content;
    @Basic(optional = false)
    @NotNull
    @Column(name = "tstamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date tstamp;
    @Column(name = "commitedby")
    private BigInteger commitedby;

    public CfTemplateversion() {
    }

    public CfTemplateversion(CfTemplateversionPK cfTemplateversionPK) {
        this.cfTemplateversionPK = cfTemplateversionPK;
    }

    public CfTemplateversion(CfTemplateversionPK cfTemplateversionPK, Date tstamp) {
        this.cfTemplateversionPK = cfTemplateversionPK;
        this.tstamp = tstamp;
    }

    public CfTemplateversion(long templateref, long version) {
        this.cfTemplateversionPK = new CfTemplateversionPK(templateref, version);
    }

    public CfTemplateversionPK getCfTemplateversionPK() {
        return cfTemplateversionPK;
    }

    public void setCfTemplateversionPK(CfTemplateversionPK cfTemplateversionPK) {
        this.cfTemplateversionPK = cfTemplateversionPK;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public Date getTstamp() {
        return tstamp;
    }

    public void setTstamp(Date tstamp) {
        this.tstamp = tstamp;
    }

    public BigInteger getCommitedby() {
        return commitedby;
    }

    public void setCommitedby(BigInteger commitedby) {
        this.commitedby = commitedby;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cfTemplateversionPK != null ? cfTemplateversionPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfTemplateversion)) {
            return false;
        }
        CfTemplateversion other = (CfTemplateversion) object;
        if ((this.cfTemplateversionPK == null && other.cfTemplateversionPK != null) || (this.cfTemplateversionPK != null && !this.cfTemplateversionPK.equals(other.cfTemplateversionPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfTemplateversion[ cfTemplateversionPK=" + cfTemplateversionPK + " ]";
    }
    
}
