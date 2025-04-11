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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author sulzbachr
 */
@Entity
@Table(name = "cf_attributetype", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfAttributetype.findAll", query = "SELECT c FROM CfAttributetype c"),
    @NamedQuery(name = "CfAttributetype.findById", query = "SELECT c FROM CfAttributetype c WHERE c.id = :id"),
    @NamedQuery(name = "CfAttributetype.findByName", query = "SELECT c FROM CfAttributetype c WHERE c.name = :name")})
public class CfAttributetype implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @NotNull
    @Column(name = "searchrelevant")
    private boolean searchrelevant;
    @Basic(optional = false)
    @NotNull
    @Column(name = "canidentity")
    private boolean canidentity;

    public CfAttributetype() {
    }

    public CfAttributetype(Long id) {
        this.id = id;
        this.searchrelevant = false;
    }

    public CfAttributetype(Long id, String name) {
        this.id = id;
        this.name = name;
        this.searchrelevant = false;
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

    public boolean isSearchrelevant() {
        return searchrelevant;
    }

    public void setSearchrelevant(boolean searchrelevant) {
        this.searchrelevant = searchrelevant;
    }
    
    public boolean isCanidentity() {
        return canidentity;
    }

    public void setCanidentity(boolean canidentity) {
        this.canidentity = canidentity;
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
        if (!(object instanceof CfAttributetype)) {
            return false;
        }
        CfAttributetype other = (CfAttributetype) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
    
}
