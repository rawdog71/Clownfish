/*
 * Copyright 2023 raine.
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
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author raine
 */
@Entity
@Table(name = "cf_staticsite", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfStaticsite.findAll", query = "SELECT c FROM CfStaticsite c"),
    @NamedQuery(name = "CfStaticsite.findById", query = "SELECT c FROM CfStaticsite c WHERE c.id = :id"),
    @NamedQuery(name = "CfStaticsite.findBySite", query = "SELECT c FROM CfStaticsite c WHERE c.site = :site"),
    @NamedQuery(name = "CfStaticsite.findByUrlparams", query = "SELECT c FROM CfStaticsite c WHERE c.urlparams = :urlparams"),
    @NamedQuery(name = "CfStaticsite.findBySiteAndUrlparams", query = "SELECT c FROM CfStaticsite c WHERE c.site = :site AND c.urlparams = :urlparams"),
    @NamedQuery(name = "CfStaticsite.findByTstamp", query = "SELECT c FROM CfStaticsite c WHERE c.tstamp = :tstamp")})
public class CfStaticsite implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Size(max = 64)
    @Column(name = "site")
    private String site;
    @Size(max = 1024)
    @Column(name = "urlparams")
    private String urlparams;
    @Column(name = "tstamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date tstamp;
    @Column(name = "offline")
    private boolean offline;

    public CfStaticsite() {
    }

    public CfStaticsite(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getUrlparams() {
        return urlparams;
    }

    public void setUrlparams(String urlparams) {
        this.urlparams = urlparams;
    }

    public Date getTstamp() {
        return tstamp;
    }

    public void setTstamp(Date tstamp) {
        this.tstamp = tstamp;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
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
        if (!(object instanceof CfStaticsite)) {
            return false;
        }
        CfStaticsite other = (CfStaticsite) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfStaticsite[ id=" + id + " ]";
    }
    
}
