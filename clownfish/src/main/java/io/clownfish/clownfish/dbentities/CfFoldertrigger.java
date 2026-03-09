/*
 * Copyright 2026 SulzbachR.
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
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author SulzbachR
 */
@Entity
@Table(name = "cf_foldertrigger", catalog = "clownfish", schema = "")
@Cacheable(false)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfFoldertrigger.findAll", query = "SELECT c FROM CfFoldertrigger c"),
    @NamedQuery(name = "CfFoldertrigger.findById", query = "SELECT c FROM CfFoldertrigger c WHERE c.id = :id"),
    @NamedQuery(name = "CfFoldertrigger.findByName", query = "SELECT c FROM CfFoldertrigger c WHERE c.name = :name"),
    @NamedQuery(name = "CfFoldertrigger.findByFolder", query = "SELECT c FROM CfFoldertrigger c WHERE c.folder = :folder"),
    @NamedQuery(name = "CfFoldertrigger.findByRecursive", query = "SELECT c FROM CfFoldertrigger c WHERE c.recursive = :recursive"),
    @NamedQuery(name = "CfFoldertrigger.findByActive", query = "SELECT c FROM CfFoldertrigger c WHERE c.active = :active"),
    @NamedQuery(name = "CfFoldertrigger.findBySiteref", query = "SELECT c FROM CfFoldertrigger c WHERE c.siteref = :siteref"),
    @NamedQuery(name = "CfFoldertrigger.findByParameter", query = "SELECT c FROM CfFoldertrigger c WHERE c.parameter = :parameter")})
public class CfFoldertrigger implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "name")
    private String name;
    @Size(max = 512)
    @Column(name = "folder")
    private String folder;
    @Column(name = "recursive")
    private boolean recursive;
    @Column(name = "active")
    private boolean active;
    @Column(name = "siteref")
    private BigInteger siteref;
    @Size(max = 2048)
    @Column(name = "parameter")
    private String parameter;

    public CfFoldertrigger() {
    }

    public CfFoldertrigger(Long id) {
        this.id = id;
    }

    public CfFoldertrigger(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public boolean getRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public BigInteger getSiteref() {
        return siteref;
    }

    public void setSiteref(BigInteger siteref) {
        this.siteref = siteref;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
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
        if (!(object instanceof CfFoldertrigger)) {
            return false;
        }
        CfFoldertrigger other = (CfFoldertrigger) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.CfFoldertrigger[ id=" + id + " ]";
    }
    
}
