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
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author sulzbachr
 */
@Entity
@Table(name = "cf_relation", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfRelation.findAll", query = "SELECT c FROM CfRelation c"),
    @NamedQuery(name = "CfRelation.findById", query = "SELECT c FROM CfRelation c WHERE c.id = :id"),
    @NamedQuery(name = "CfRelation.findByTyp", query = "SELECT c FROM CfRelation c WHERE c.typ = :typ"),
    @NamedQuery(name = "CfRelation.findByRef1", query = "SELECT c FROM CfRelation c WHERE c.ref1 = :ref1"),
    @NamedQuery(name = "CfRelation.findByRef2", query = "SELECT c FROM CfRelation c WHERE c.ref2 = :ref2")})
public class CfRelation implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "typ")
    private int typ;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ref1")
    private long ref1;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ref2")
    private long ref2;

    public CfRelation() {
    }

    public CfRelation(Long id) {
        this.id = id;
    }

    public CfRelation(Long id, int typ, long ref1, long ref2) {
        this.id = id;
        this.typ = typ;
        this.ref1 = ref1;
        this.ref2 = ref2;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getTyp() {
        return typ;
    }

    public void setTyp(int typ) {
        this.typ = typ;
    }

    public long getRef1() {
        return ref1;
    }

    public void setRef1(long ref1) {
        this.ref1 = ref1;
    }

    public long getRef2() {
        return ref2;
    }

    public void setRef2(long ref2) {
        this.ref2 = ref2;
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
        if (!(object instanceof CfRelation)) {
            return false;
        }
        CfRelation other = (CfRelation) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfRelation[ id=" + id + " ]";
    }
    
}
