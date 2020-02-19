/*
 * Copyright 2020 sulzbachr.
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
@Table(name = "cf_userbackend", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfUserbackend.findAll", query = "SELECT c FROM CfUserbackend c"),
    @NamedQuery(name = "CfUserbackend.findByUserref", query = "SELECT c FROM CfUserbackend c WHERE c.cfUserbackendPK.userref = :userref"),
    @NamedQuery(name = "CfUserbackend.findByBackendref", query = "SELECT c FROM CfUserbackend c WHERE c.cfUserbackendPK.backendref = :backendref")})
public class CfUserbackend implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfUserbackendPK cfUserbackendPK;

    public CfUserbackend() {
    }

    public CfUserbackend(CfUserbackendPK cfUserbackendPK) {
        this.cfUserbackendPK = cfUserbackendPK;
    }

    public CfUserbackend(long userref, long backendref) {
        this.cfUserbackendPK = new CfUserbackendPK(userref, backendref);
    }

    public CfUserbackendPK getCfUserbackendPK() {
        return cfUserbackendPK;
    }

    public void setCfUserbackendPK(CfUserbackendPK cfUserbackendPK) {
        this.cfUserbackendPK = cfUserbackendPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cfUserbackendPK != null ? cfUserbackendPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfUserbackend)) {
            return false;
        }
        CfUserbackend other = (CfUserbackend) object;
        if ((this.cfUserbackendPK == null && other.cfUserbackendPK != null) || (this.cfUserbackendPK != null && !this.cfUserbackendPK.equals(other.cfUserbackendPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfUserbackend[ cfUserbackendPK=" + cfUserbackendPK + " ]";
    }
    
}
