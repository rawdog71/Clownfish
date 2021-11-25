/*
 * Copyright 2021 SulzbachR.
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
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author SulzbachR
 */
@Entity
@Table(name = "cf_maven", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfMaven.findAll", query = "SELECT c FROM CfMaven c"),
    @NamedQuery(name = "CfMaven.findById", query = "SELECT c FROM CfMaven c WHERE c.id = :id"),
    @NamedQuery(name = "CfMaven.findByMavenId", query = "SELECT c FROM CfMaven c WHERE c.mavenId = :mavenId"),
    @NamedQuery(name = "CfMaven.findByMavenGroup", query = "SELECT c FROM CfMaven c WHERE c.mavenGroup = :mavenGroup"),
    @NamedQuery(name = "CfMaven.findByMavenArtifact", query = "SELECT c FROM CfMaven c WHERE c.mavenArtifact = :mavenArtifact"),
    @NamedQuery(name = "CfMaven.findByMavenLatestversion", query = "SELECT c FROM CfMaven c WHERE c.mavenLatestversion = :mavenLatestversion"),
    @NamedQuery(name = "CfMaven.findByMavenPackage", query = "SELECT c FROM CfMaven c WHERE c.mavenPackage = :mavenPackage"),
    @NamedQuery(name = "CfMaven.findByMavenFilename", query = "SELECT c FROM CfMaven c WHERE c.mavenFilename = :mavenFilename")})
public class CfMaven implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "maven_id")
    private String mavenId;
    @Column(name = "maven_group")
    private String mavenGroup;
    @Column(name = "maven_artifact")
    private String mavenArtifact;
    @Column(name = "maven_latestversion")
    private String mavenLatestversion;
    @Column(name = "maven_package")
    private String mavenPackage;
    @Column(name = "maven_filename")
    private String mavenFilename;

    public CfMaven() {
    }

    public CfMaven(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMavenId() {
        return mavenId;
    }

    public void setMavenId(String mavenId) {
        this.mavenId = mavenId;
    }

    public String getMavenGroup() {
        return mavenGroup;
    }

    public void setMavenGroup(String mavenGroup) {
        this.mavenGroup = mavenGroup;
    }

    public String getMavenArtifact() {
        return mavenArtifact;
    }

    public void setMavenArtifact(String mavenArtifact) {
        this.mavenArtifact = mavenArtifact;
    }

    public String getMavenLatestversion() {
        return mavenLatestversion;
    }

    public void setMavenLatestversion(String mavenLatestversion) {
        this.mavenLatestversion = mavenLatestversion;
    }

    public String getMavenPackage() {
        return mavenPackage;
    }

    public void setMavenPackage(String mavenPackage) {
        this.mavenPackage = mavenPackage;
    }

    public String getMavenFilename() {
        return mavenFilename;
    }

    public void setMavenFilename(String mavenFilename) {
        this.mavenFilename = mavenFilename;
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
        if (!(object instanceof CfMaven)) {
            return false;
        }
        CfMaven other = (CfMaven) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfMaven[ id=" + id + " ]";
    }
    
}
