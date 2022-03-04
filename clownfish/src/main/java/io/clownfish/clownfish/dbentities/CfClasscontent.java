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
import java.math.BigInteger;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author sulzbachr
 */
@Entity
@Table(name = "cf_classcontent", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfClasscontent.findAll", query = "SELECT c FROM CfClasscontent c WHERE c.scrapped = 0"),
    @NamedQuery(name = "CfClasscontent.findById", query = "SELECT c FROM CfClasscontent c WHERE c.id = :id"),
    @NamedQuery(name = "CfClasscontent.findByClassref", query = "SELECT c FROM CfClasscontent c WHERE c.classref = :classref AND c.scrapped = 0"),
    @NamedQuery(name = "CfClasscontent.findByName", query = "SELECT c FROM CfClasscontent c WHERE c.name = :name"),
    @NamedQuery(name = "CfClasscontent.findByScrapped", query = "SELECT c FROM CfClasscontent c WHERE c.scrapped = :scrapped"),
    @NamedQuery(name = "CfClasscontent.findByMaintenance", query = "SELECT cc FROM CfClasscontent cc INNER JOIN CfClass c ON cc.classref = c.id WHERE c.maintenance = :maintenance AND cc.scrapped = 0")
       
})
public class CfClasscontent implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @JoinColumn(name = "classref", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private CfClass classref;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "name")
    private String name;
    @Column(name = "scrapped")
    private boolean scrapped;
    @Column(name = "checkedoutby")
    private BigInteger checkedoutby;

    public CfClasscontent() {
    }

    public CfClasscontent(Long id) {
        this.id = id;
    }

    public CfClasscontent(Long id, CfClass classref, String name) {
        this.id = id;
        this.classref = classref;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CfClass getClassref() {
        return classref;
    }

    public void setClassref(CfClass classref) {
        this.classref = classref;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isScrapped() {
        return scrapped;
    }

    public void setScrapped(boolean scrapped) {
        this.scrapped = scrapped;
    }

    public BigInteger getCheckedoutby() {
        return checkedoutby;
    }

    public void setCheckedoutby(BigInteger checkedoutby) {
        this.checkedoutby = checkedoutby;
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
        if (!(object instanceof CfClasscontent)) {
            return false;
        }
        CfClasscontent other = (CfClasscontent) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfClasscontent[ id=" + id + " ]";
    }
    
}
