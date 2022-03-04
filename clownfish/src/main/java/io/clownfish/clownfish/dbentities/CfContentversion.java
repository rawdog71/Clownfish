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
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author raine
 */
@Entity
@Table(name = "cf_contentversion", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfContentversion.findAll", query = "SELECT c FROM CfContentversion c"),
    @NamedQuery(name = "CfContentversion.findByContentref", query = "SELECT c FROM CfContentversion c WHERE c.cfContentversionPK.contentref = :contentref"),
    @NamedQuery(name = "CfContentversion.findByVersion", query = "SELECT c FROM CfContentversion c WHERE c.cfContentversionPK.version = :version"),
    @NamedQuery(name = "CfContentversion.findByPK", query = "SELECT c FROM CfContentversion c WHERE c.cfContentversionPK.contentref = :contentref AND c.cfContentversionPK.version = :version"),
    @NamedQuery(name = "CfContentversion.findByTstamp", query = "SELECT c FROM CfContentversion c WHERE c.tstamp = :tstamp"),
    @NamedQuery(name = "CfContentversion.findByCommitedby", query = "SELECT c FROM CfContentversion c WHERE c.commitedby = :commitedby"),
    @NamedQuery(name = "CfContentversion.findMaxVersion", query = "SELECT MAX(c.cfContentversionPK.version) FROM CfContentversion c WHERE c.cfContentversionPK.contentref = :contentref")
})
public class CfContentversion implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfContentversionPK cfContentversionPK;
    @Lob
    @Column(name = "content")
    private byte[] content;
    @Column(name = "tstamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date tstamp;
    @Column(name = "commitedby")
    private BigInteger commitedby;

    public CfContentversion() {
    }

    public CfContentversion(CfContentversionPK cfContentversionPK) {
        this.cfContentversionPK = cfContentversionPK;
    }

    public CfContentversion(long contentref, long version) {
        this.cfContentversionPK = new CfContentversionPK(contentref, version);
    }

    public CfContentversionPK getCfContentversionPK() {
        return cfContentversionPK;
    }

    public void setCfContentversionPK(CfContentversionPK cfContentversionPK) {
        this.cfContentversionPK = cfContentversionPK;
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
        hash += (cfContentversionPK != null ? cfContentversionPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfContentversion)) {
            return false;
        }
        CfContentversion other = (CfContentversion) object;
        if ((this.cfContentversionPK == null && other.cfContentversionPK != null) || (this.cfContentversionPK != null && !this.cfContentversionPK.equals(other.cfContentversionPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfContentversion[ cfContentversionPK=" + cfContentversionPK + " ]";
    }
    
}
