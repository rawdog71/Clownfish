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
@Table(name = "cf_relationcontent", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfRelationcontent.findAll", query = "SELECT c FROM CfRelationcontent c"),
    @NamedQuery(name = "CfRelationcontent.findById", query = "SELECT c FROM CfRelationcontent c WHERE c.id = :id"),
    @NamedQuery(name = "CfRelationcontent.findByRelationref", query = "SELECT c FROM CfRelationcontent c WHERE c.relationref = :relationref"),
    @NamedQuery(name = "CfRelationcontent.findByContent1ref", query = "SELECT c FROM CfRelationcontent c WHERE c.content1ref = :content1ref"),
    @NamedQuery(name = "CfRelationcontent.findByContent2ref", query = "SELECT c FROM CfRelationcontent c WHERE c.content2ref = :content2ref")})
public class CfRelationcontent implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "relationref")
    private long relationref;
    @Basic(optional = false)
    @NotNull
    @Column(name = "content1ref")
    private long content1ref;
    @Basic(optional = false)
    @NotNull
    @Column(name = "content2ref")
    private long content2ref;

    public CfRelationcontent() {
    }

    public CfRelationcontent(Long id) {
        this.id = id;
    }

    public CfRelationcontent(Long id, long relationref, long content1ref, long content2ref) {
        this.id = id;
        this.relationref = relationref;
        this.content1ref = content1ref;
        this.content2ref = content2ref;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getRelationref() {
        return relationref;
    }

    public void setRelationref(long relationref) {
        this.relationref = relationref;
    }

    public long getContent1ref() {
        return content1ref;
    }

    public void setContent1ref(long content1ref) {
        this.content1ref = content1ref;
    }

    public long getContent2ref() {
        return content2ref;
    }

    public void setContent2ref(long content2ref) {
        this.content2ref = content2ref;
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
        if (!(object instanceof CfRelationcontent)) {
            return false;
        }
        CfRelationcontent other = (CfRelationcontent) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfRelationcontent[ id=" + id + " ]";
    }
    
}
