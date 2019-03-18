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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author sulzbachr
 */
@Entity
@Table(name = "cf_attribut", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfAttribut.findAll", query = "SELECT c FROM CfAttribut c"),
    @NamedQuery(name = "CfAttribut.findById", query = "SELECT c FROM CfAttribut c WHERE c.id = :id"),
    @NamedQuery(name = "CfAttribut.findByName", query = "SELECT c FROM CfAttribut c WHERE c.name = :name"),
    @NamedQuery(name = "CfAttribut.findByAttributetype", query = "SELECT c FROM CfAttribut c WHERE c.attributetype = :attributetype"),
    @NamedQuery(name = "CfAttribut.findByClassref", query = "SELECT c FROM CfAttribut c WHERE c.classref = :classref"),
    @NamedQuery(name = "CfAttribut.findByIdentity", query = "SELECT c FROM CfAttribut c WHERE c.identity = :identity"),
    @NamedQuery(name = "CfAttribut.findByAutoincrementor", query = "SELECT c FROM CfAttribut c WHERE c.autoincrementor = :autoincrementor")})
public class CfAttribut implements Serializable {

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
    @Column(name = "attributetype")
    private long attributetype;
    @Basic(optional = false)
    @NotNull
    @Column(name = "classref")
    private long classref;
    @Column(name = "identity")
    private Boolean identity;
    @Column(name = "autoincrementor")
    private Boolean autoincrementor;

    public CfAttribut() {
    }

    public CfAttribut(Long id) {
        this.id = id;
    }

    public CfAttribut(Long id, String name, long attributetype, long classref) {
        this.id = id;
        this.name = name;
        this.attributetype = attributetype;
        this.classref = classref;
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

    public long getAttributetype() {
        return attributetype;
    }

    public void setAttributetype(long attributetype) {
        this.attributetype = attributetype;
    }

    public long getClassref() {
        return classref;
    }

    public void setClassref(long classref) {
        this.classref = classref;
    }

    public Boolean getIdentity() {
        return identity;
    }

    public void setIdentity(Boolean identity) {
        this.identity = identity;
    }

    public Boolean getAutoincrementor() {
        return autoincrementor;
    }

    public void setAutoincrementor(Boolean autoincrementor) {
        this.autoincrementor = autoincrementor;
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
        if (!(object instanceof CfAttribut)) {
            return false;
        }
        CfAttribut other = (CfAttribut) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfAttribut[ id=" + id + " ]";
    }
    
}
