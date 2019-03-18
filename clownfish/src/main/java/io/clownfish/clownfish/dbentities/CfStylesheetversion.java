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
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author sulzbachr
 */
@Entity
@Table(name = "cf_stylesheetversion", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfStylesheetversion.findAll", query = "SELECT c FROM CfStylesheetversion c"),
    @NamedQuery(name = "CfStylesheetversion.findByStylesheetref", query = "SELECT c FROM CfStylesheetversion c WHERE c.cfStylesheetversionPK.stylesheetref = :stylesheetref"),
    @NamedQuery(name = "CfStylesheetversion.findByVersion", query = "SELECT c FROM CfStylesheetversion c WHERE c.cfStylesheetversionPK.version = :version"),
    @NamedQuery(name = "CfStylesheetversion.findByTstamp", query = "SELECT c FROM CfStylesheetversion c WHERE c.tstamp = :tstamp"),
    @NamedQuery(name = "CfStylesheetversion.findByCommitedby", query = "SELECT c FROM CfStylesheetversion c WHERE c.commitedby = :commitedby")})
public class CfStylesheetversion implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfStylesheetversionPK cfStylesheetversionPK;
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

    public CfStylesheetversion() {
    }

    public CfStylesheetversion(CfStylesheetversionPK cfStylesheetversionPK) {
        this.cfStylesheetversionPK = cfStylesheetversionPK;
    }

    public CfStylesheetversion(CfStylesheetversionPK cfStylesheetversionPK, Date tstamp) {
        this.cfStylesheetversionPK = cfStylesheetversionPK;
        this.tstamp = tstamp;
    }

    public CfStylesheetversion(long stylesheetref, long version) {
        this.cfStylesheetversionPK = new CfStylesheetversionPK(stylesheetref, version);
    }

    public CfStylesheetversionPK getCfStylesheetversionPK() {
        return cfStylesheetversionPK;
    }

    public void setCfStylesheetversionPK(CfStylesheetversionPK cfStylesheetversionPK) {
        this.cfStylesheetversionPK = cfStylesheetversionPK;
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
        hash += (cfStylesheetversionPK != null ? cfStylesheetversionPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfStylesheetversion)) {
            return false;
        }
        CfStylesheetversion other = (CfStylesheetversion) object;
        if ((this.cfStylesheetversionPK == null && other.cfStylesheetversionPK != null) || (this.cfStylesheetversionPK != null && !this.cfStylesheetversionPK.equals(other.cfStylesheetversionPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfStylesheetversion[ cfStylesheetversionPK=" + cfStylesheetversionPK + " ]";
    }
    
}
