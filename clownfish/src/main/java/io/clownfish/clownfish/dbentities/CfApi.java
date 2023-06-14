/*
 * Copyright 2023 SulzbachR.
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
@Table(name = "cf_api", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfApi.findAll", query = "SELECT c FROM CfApi c"),
    @NamedQuery(name = "CfApi.findBySiteref", query = "SELECT c FROM CfApi c WHERE c.cfApiPK.siteref = :siteref"),
    @NamedQuery(name = "CfApi.findByKeyname", query = "SELECT c FROM CfApi c WHERE c.cfApiPK.keyname = :keyname"),
    @NamedQuery(name = "CfApi.findByDescription", query = "SELECT c FROM CfApi c WHERE c.description = :description")})
public class CfApi implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfApiPK cfApiPK;
    @Size(max = 255)
    @Column(name = "description")
    private String description;

    public CfApi() {
    }

    public CfApi(CfApiPK cfApiPK) {
        this.cfApiPK = cfApiPK;
    }

    public CfApi(long siteref, String keyname) {
        this.cfApiPK = new CfApiPK(siteref, keyname);
    }

    public CfApiPK getCfApiPK() {
        return cfApiPK;
    }

    public void setCfApiPK(CfApiPK cfApiPK) {
        this.cfApiPK = cfApiPK;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cfApiPK != null ? cfApiPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfApi)) {
            return false;
        }
        CfApi other = (CfApi) object;
        if ((this.cfApiPK == null && other.cfApiPK != null) || (this.cfApiPK != null && !this.cfApiPK.equals(other.cfApiPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfApi[ cfApiPK=" + cfApiPK + " ]";
    }
    
}
