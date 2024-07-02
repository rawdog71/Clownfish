/*
 * Copyright 2024 SulzbachR.
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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "cf_npm", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfNpm.findAll", query = "SELECT c FROM CfNpm c"),
    @NamedQuery(name = "CfNpm.findById", query = "SELECT c FROM CfNpm c WHERE c.id = :id"),
    @NamedQuery(name = "CfNpm.findByNpmId", query = "SELECT c FROM CfNpm c WHERE c.npmId = :npmId"),
    @NamedQuery(name = "CfNpm.findByNpmLatestversion", query = "SELECT c FROM CfNpm c WHERE c.npmLatestversion = :npmLatestversion"),
    @NamedQuery(name = "CfNpm.findByNpmIdAndNpmLatestversion", query = "SELECT c FROM CfNpm c WHERE c.npmLatestversion = :npmLatestversion AND c.npmId = :npmId"),
    @NamedQuery(name = "CfNpm.findByNpmFilename", query = "SELECT c FROM CfNpm c WHERE c.npmFilename = :npmFilename")})
public class CfNpm implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Size(max = 255)
    @Column(name = "npm_id")
    private String npmId;
    @Size(max = 64)
    @Column(name = "npm_latestversion")
    private String npmLatestversion;
    @Size(max = 255)
    @Column(name = "npm_filename")
    private String npmFilename;

    public CfNpm() {
    }

    public CfNpm(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNpmId() {
        return npmId;
    }

    public void setNpmId(String npmId) {
        this.npmId = npmId;
    }

    public String getNpmLatestversion() {
        return npmLatestversion;
    }

    public void setNpmLatestversion(String npmLatestversion) {
        this.npmLatestversion = npmLatestversion;
    }

    public String getNpmFilename() {
        return npmFilename;
    }

    public void setNpmFilename(String npmFilename) {
        this.npmFilename = npmFilename;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfNpm)) {
            return false;
        }
        CfNpm other = (CfNpm) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfNpm[ id=" + id + " ]";
    }
    
}
