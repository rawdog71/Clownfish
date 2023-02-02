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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "cf_attribut", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfAttribut.findAll", query = "SELECT c FROM CfAttribut c"),
    @NamedQuery(name = "CfAttribut.findById", query = "SELECT c FROM CfAttribut c WHERE c.id = :id"),
    @NamedQuery(name = "CfAttribut.findByName", query = "SELECT c FROM CfAttribut c WHERE c.name = :name"),
    @NamedQuery(name = "CfAttribut.findByAttributetype", query = "SELECT c FROM CfAttribut c WHERE c.attributetype = :attributetype"),
    @NamedQuery(name = "CfAttribut.findByClassref", query = "SELECT c FROM CfAttribut c WHERE c.classref = :classref"),
    @NamedQuery(name = "CfAttribut.findByIdentity", query = "SELECT c FROM CfAttribut c WHERE c.identity = :identity"),
    @NamedQuery(name = "CfAttribut.findByIsindex", query = "SELECT c FROM CfAttribut c WHERE c.isindex = :isindex"),
    @NamedQuery(name = "CfAttribut.findByAutoincrementor", query = "SELECT c FROM CfAttribut c WHERE c.autoincrementor = :autoincrementor"),
    @NamedQuery(name = "CfAttribut.findByNameAndClassref", query = "SELECT c FROM CfAttribut c WHERE c.name = :name AND c.classref = :classref")
})
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
    @JoinColumn(name = "attributetype", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private CfAttributetype attributetype;
    @JoinColumn(name = "classref", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private CfClass classref;
    @Column(name = "identity")
    private Boolean identity;
    @Column(name = "autoincrementor")
    private Boolean autoincrementor;
    @Column(name = "isindex")
    private Boolean isindex;
    @JoinColumn(name = "relationref", referencedColumnName = "id")
    @ManyToOne(optional = true)
    private CfClass relationref;
    @Column(name = "relationtype")
    private int relationtype;
    @Column(name = "min_val")
    private long min_val;
    @Column(name = "max_val")
    private long max_val;
    @Column(name = "default_val")
    private String default_val;
    @Column(name = "mandatory")
    private Boolean mandatory;
    @Column(name = "description")
    private String description;

    public CfAttribut() {
    }

    public CfAttribut(Long id) {
        this.id = id;
    }

    public CfAttribut(Long id, String name, CfClass classref, CfAttributetype attributetype) {
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

    public CfAttributetype getAttributetype() {
        return attributetype;
    }
    
    public String getAttributetypeString() {
        if (0 == attributetype.getName().compareToIgnoreCase("classref")) {
            return attributetype.getName() + " [" + getRelationtypeFull() + "]";
        } else {
            return attributetype.getName();
        }
    }

    public void setAttributetype(CfAttributetype attributetype) {
        this.attributetype = attributetype;
    }

    public CfClass getClassref() {
        return classref;
    }

    public void setClassref(CfClass classref) {
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
    
    public Boolean getIsindex() {
        return isindex;
    }

    public void setIsindex(Boolean isindex) {
        this.isindex = isindex;
    }

    public CfClass getRelationref() {
        return relationref;
    }

    public void setRelationref(CfClass relationref) {
        this.relationref = relationref;
    }

    public int getRelationtype() {
        return relationtype;
    }

    public void setRelationtype(int relationtype) {
        this.relationtype = relationtype;
    }
    
    public String getRelationtypeFull() {
        if (0 == attributetype.getName().compareToIgnoreCase("classref")) {
            if (0 == relationtype)
                return "n:m";
            else
                return "1:n";
        } else {
            return "";
        }
    }

    public long getMin_val() {
        return min_val;
    }

    public void setMin_val(long min_val) {
        this.min_val = min_val;
    }

    public long getMax_val() {
        return max_val;
    }

    public void setMax_val(long max_val) {
        this.max_val = max_val;
    }

    public String getDefault_val() {
        return default_val;
    }

    public void setDefault_val(String default_val) {
        this.default_val = default_val;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
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
