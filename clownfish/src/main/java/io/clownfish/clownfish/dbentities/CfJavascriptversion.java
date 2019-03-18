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
@Table(name = "cf_javascriptversion", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfJavascriptversion.findAll", query = "SELECT c FROM CfJavascriptversion c"),
    @NamedQuery(name = "CfJavascriptversion.findByJavascriptref", query = "SELECT c FROM CfJavascriptversion c WHERE c.cfJavascriptversionPK.javascriptref = :javascriptref"),
    @NamedQuery(name = "CfJavascriptversion.findByVersion", query = "SELECT c FROM CfJavascriptversion c WHERE c.cfJavascriptversionPK.version = :version"),
    @NamedQuery(name = "CfJavascriptversion.findByTstamp", query = "SELECT c FROM CfJavascriptversion c WHERE c.tstamp = :tstamp"),
    @NamedQuery(name = "CfJavascriptversion.findByCommitedby", query = "SELECT c FROM CfJavascriptversion c WHERE c.commitedby = :commitedby")})
public class CfJavascriptversion implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfJavascriptversionPK cfJavascriptversionPK;
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

    public CfJavascriptversion() {
    }

    public CfJavascriptversion(CfJavascriptversionPK cfJavascriptversionPK) {
        this.cfJavascriptversionPK = cfJavascriptversionPK;
    }

    public CfJavascriptversion(CfJavascriptversionPK cfJavascriptversionPK, Date tstamp) {
        this.cfJavascriptversionPK = cfJavascriptversionPK;
        this.tstamp = tstamp;
    }

    public CfJavascriptversion(long javascriptref, long version) {
        this.cfJavascriptversionPK = new CfJavascriptversionPK(javascriptref, version);
    }

    public CfJavascriptversionPK getCfJavascriptversionPK() {
        return cfJavascriptversionPK;
    }

    public void setCfJavascriptversionPK(CfJavascriptversionPK cfJavascriptversionPK) {
        this.cfJavascriptversionPK = cfJavascriptversionPK;
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
        hash += (cfJavascriptversionPK != null ? cfJavascriptversionPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfJavascriptversion)) {
            return false;
        }
        CfJavascriptversion other = (CfJavascriptversion) object;
        if ((this.cfJavascriptversionPK == null && other.cfJavascriptversionPK != null) || (this.cfJavascriptversionPK != null && !this.cfJavascriptversionPK.equals(other.cfJavascriptversionPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfJavascriptversion[ cfJavascriptversionPK=" + cfJavascriptversionPK + " ]";
    }
    
}
