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
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author raine
 */
@Entity
@Table(name = "cf_accessmanager", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfAccessmanager.findAll", query = "SELECT c FROM CfAccessmanager c"),
    @NamedQuery(name = "CfAccessmanager.findById", query = "SELECT c FROM CfAccessmanager c WHERE c.id = :id"),
    @NamedQuery(name = "CfAccessmanager.findByType", query = "SELECT c FROM CfAccessmanager c WHERE c.type = :type"),
    @NamedQuery(name = "CfAccessmanager.findByRef", query = "SELECT c FROM CfAccessmanager c WHERE c.ref = :ref"),
    @NamedQuery(name = "CfAccessmanager.findByRefclasscontent", query = "SELECT c FROM CfAccessmanager c WHERE c.refclasscontent = :refclasscontent"),
    @NamedQuery(name = "CfAccessmanager.findByTypeAndRef", query = "SELECT c FROM CfAccessmanager c WHERE c.type = :type AND c.ref = :ref"),
    @NamedQuery(name = "CfAccessmanager.findByTypeAndRefAndRefclasscontent", query = "SELECT c FROM CfAccessmanager c WHERE c.type = :type AND c.ref = :ref AND c.refclasscontent = :refclasscontent")
})
public class CfAccessmanager implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "type")
    private int type;
    @Column(name = "ref")
    private BigInteger ref;
    @Column(name = "refclasscontent")
    private BigInteger refclasscontent;

    public CfAccessmanager() {
    }

    public CfAccessmanager(Long id) {
        this.id = id;
    }

    public CfAccessmanager(Long id, int type) {
        this.id = id;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public BigInteger getRef() {
        return ref;
    }

    public void setRef(BigInteger ref) {
        this.ref = ref;
    }

    public BigInteger getRefclasscontent() {
        return refclasscontent;
    }

    public void setRefclasscontent(BigInteger refclasscontent) {
        this.refclasscontent = refclasscontent;
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
        if (!(object instanceof CfAccessmanager)) {
            return false;
        }
        CfAccessmanager other = (CfAccessmanager) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfAccessmanager[ id=" + id + " ]";
    }
    
}
