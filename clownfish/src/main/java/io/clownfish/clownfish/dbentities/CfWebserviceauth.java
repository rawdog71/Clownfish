/*
 * Copyright 2020 SulzbachR.
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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author SulzbachR
 */
@Entity
@Table(name = "cf_webserviceauth", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfWebserviceauth.findAll", query = "SELECT c FROM CfWebserviceauth c"),
    @NamedQuery(name = "CfWebserviceauth.findByUserRef", query = "SELECT c FROM CfWebserviceauth c WHERE c.cfWebserviceauthPK.userRef = :userRef"),
    @NamedQuery(name = "CfWebserviceauth.findByWebserviceRef", query = "SELECT c FROM CfWebserviceauth c WHERE c.cfWebserviceauthPK.webserviceRef = :webserviceRef"),
    @NamedQuery(name = "CfWebserviceauth.findByHash", query = "SELECT c FROM CfWebserviceauth c WHERE c.hash = :hash")})
public class CfWebserviceauth implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfWebserviceauthPK cfWebserviceauthPK;
    @Basic(optional = false)
    //@NotNull
    @Size(min = 1, max = 255)
    @Column(name = "hash")
    private String hash;

    public CfWebserviceauth() {
    }

    public CfWebserviceauth(CfWebserviceauthPK cfWebserviceauthPK) {
        this.cfWebserviceauthPK = cfWebserviceauthPK;
    }

    public CfWebserviceauth(CfWebserviceauthPK cfWebserviceauthPK, String hash) {
        this.cfWebserviceauthPK = cfWebserviceauthPK;
        this.hash = hash;
    }

    public CfWebserviceauth(CfUser userRef, CfWebservice webserviceRef) {
        this.cfWebserviceauthPK = new CfWebserviceauthPK(userRef, webserviceRef);
    }

    public CfWebserviceauthPK getCfWebserviceauthPK() {
        return cfWebserviceauthPK;
    }

    public void setCfWebserviceauthPK(CfWebserviceauthPK cfWebserviceauthPK) {
        this.cfWebserviceauthPK = cfWebserviceauthPK;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cfWebserviceauthPK != null ? cfWebserviceauthPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfWebserviceauth)) {
            return false;
        }
        CfWebserviceauth other = (CfWebserviceauth) object;
        if ((this.cfWebserviceauthPK == null && other.cfWebserviceauthPK != null) || (this.cfWebserviceauthPK != null && !this.cfWebserviceauthPK.equals(other.cfWebserviceauthPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfWebserviceauth[ cfWebserviceauthPK=" + cfWebserviceauthPK + " ]";
    }
    
}
