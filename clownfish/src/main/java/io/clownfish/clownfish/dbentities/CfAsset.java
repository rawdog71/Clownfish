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
import javax.persistence.Cacheable;
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
@Table(name = "cf_asset", catalog = "clownfish", schema = "")
@Cacheable(false)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfAsset.findAll", query = "SELECT c FROM CfAsset c WHERE c.scrapped = 0"),
    @NamedQuery(name = "CfAsset.findById", query = "SELECT c FROM CfAsset c WHERE c.id = :id"),
    @NamedQuery(name = "CfAsset.findByName", query = "SELECT c FROM CfAsset c WHERE c.name = :name"),
    @NamedQuery(name = "CfAsset.findByFileextension", query = "SELECT c FROM CfAsset c WHERE c.fileextension = :fileextension"),
    @NamedQuery(name = "CfAsset.findByMimetype", query = "SELECT c FROM CfAsset c WHERE c.mimetype = :mimetype"),
    @NamedQuery(name = "CfAsset.findByImagewidth", query = "SELECT c FROM CfAsset c WHERE c.imagewidth = :imagewidth"),
    @NamedQuery(name = "CfAsset.findByImageheight", query = "SELECT c FROM CfAsset c WHERE c.imageheight = :imageheight"),
    @NamedQuery(name = "CfAsset.findByIndexed", query = "SELECT c FROM CfAsset c WHERE c.indexed = :indexed"),
    @NamedQuery(name = "CfAsset.findByScrapped", query = "SELECT c FROM CfAsset c WHERE c.scrapped = :scrapped")
})
public class CfAsset implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(name = "name")
    private String name;
    @Size(max = 255)
    @Column(name = "fileextension")
    private String fileextension;
    @Size(max = 255)
    @Column(name = "mimetype")
    private String mimetype;
    @Size(max = 255)
    @Column(name = "imagewidth")
    private String imagewidth;
    @Size(max = 255)
    @Column(name = "imageheight")
    private String imageheight;
    @Size(max = 255)
    @Column(name = "description")
    private String description;
    @Column(name = "indexed")
    private boolean indexed;
    @Column(name = "scrapped")
    private boolean scrapped;

    public CfAsset() {
    }

    public CfAsset(Long id) {
        this.id = id;
    }

    public CfAsset(Long id, String name) {
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
    
    public String getShortName() {
        String newname = name.substring(0, name.length()-fileextension.length()-1);
        if (newname.length() > 15) {
            return newname.substring(0, 15) + "..." + fileextension;
        } else {
            return newname + "." + fileextension;
        }
    }

    public String getFileextension() {
        return fileextension;
    }

    public void setFileextension(String fileextension) {
        this.fileextension = fileextension;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public String getImagewidth() {
        return imagewidth;
    }

    public void setImagewidth(String imagewidth) {
        this.imagewidth = imagewidth;
    }

    public String getImageheight() {
        return imageheight;
    }

    public void setImageheight(String imageheight) {
        this.imageheight = imageheight;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public boolean isScrapped() {
        return scrapped;
    }

    public void setScrapped(boolean scrapped) {
        this.scrapped = scrapped;
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
        if (!(object instanceof CfAsset)) {
            return false;
        }
        CfAsset other = (CfAsset) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfAsset[ id=" + id + " ]";
    }
    
}
