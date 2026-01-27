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
import java.math.BigInteger;
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
@Table(name = "cf_quartz", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfQuartz.findAll", query = "SELECT c FROM CfQuartz c"),
    @NamedQuery(name = "CfQuartz.findById", query = "SELECT c FROM CfQuartz c WHERE c.id = :id"),
    @NamedQuery(name = "CfQuartz.findByName", query = "SELECT c FROM CfQuartz c WHERE c.name = :name"),
    @NamedQuery(name = "CfQuartz.findBySchedule", query = "SELECT c FROM CfQuartz c WHERE c.schedule = :schedule"),
    @NamedQuery(name = "CfQuartz.findBySiteRef", query = "SELECT c FROM CfQuartz c WHERE c.siteRef = :siteRef"),
    @NamedQuery(name = "CfQuartz.findByParameter", query = "SELECT c FROM CfQuartz c WHERE c.parameter = :parameter")})
public class CfQuartz implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "name")
    private String name;
    @Size(max = 50)
    @Column(name = "schedule")
    private String schedule;
    @Column(name = "site_ref")
    private BigInteger siteRef;
    @Column(name = "active")
    private boolean active;
    @Column(name = "parameter")
    private String parameter;

    public CfQuartz() {
    }

    public CfQuartz(Long id) {
        this.id = id;
        this.name = "";
    }

    public CfQuartz(Long id, String name) {
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

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public BigInteger getSiteRef() {
        return siteRef;
    }

    public void setSiteRef(BigInteger siteRef) {
        this.siteRef = siteRef;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getParameter() { return this.parameter; }

    public void setParameter(String parameter) { this.parameter = parameter; }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfQuartz)) {
            return false;
        }
        CfQuartz other = (CfQuartz) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfQuartz[ id=" + id + " ]";
    }
    
}
