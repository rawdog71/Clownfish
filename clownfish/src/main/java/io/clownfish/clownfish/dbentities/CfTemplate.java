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
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
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
@Table(name = "cf_template", catalog = "clownfish", schema = "")
@Cacheable(false)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfTemplate.findAll", query = "SELECT c FROM CfTemplate c"),
    @NamedQuery(name = "CfTemplate.findById", query = "SELECT c FROM CfTemplate c WHERE c.id = :id"),
    @NamedQuery(name = "CfTemplate.findByName", query = "SELECT c FROM CfTemplate c WHERE c.name = :name"),
    @NamedQuery(name = "CfTemplate.findByScriptlanguage", query = "SELECT c FROM CfTemplate c WHERE c.scriptlanguage = :scriptlanguage"),
    @NamedQuery(name = "CfTemplate.findByCheckedoutby", query = "SELECT c FROM CfTemplate c WHERE c.checkedoutby = :checkedoutby")})
public class CfTemplate implements Serializable {

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
    @Lob
    @Size(min = 1, max = 16777215)
    @Column(name = "content")
    private String content;
    @Basic(optional = false)
    @NotNull
    @Column(name = "scriptlanguage")
    private int scriptlanguage;
    @Column(name = "checkedoutby")
    private BigInteger checkedoutby;

    public CfTemplate() {
    }

    public CfTemplate(Long id) {
        this.id = id;
    }

    public CfTemplate(Long id, String name, String content, int scriptlanguage) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.scriptlanguage = scriptlanguage;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getScriptlanguage() {
        return scriptlanguage;
    }

    public void setScriptlanguage(int scriptlanguage) {
        this.scriptlanguage = scriptlanguage;
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
        if (!(object instanceof CfTemplate)) {
            return false;
        }
        CfTemplate other = (CfTemplate) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
    
    public String getScriptLanguageTxt() {
        switch (getScriptlanguage()) {
            case 0:
                return "freemarker";
            case 1:
                return "velocity";
            case 2:
                return "htmlmixed";
            case 3:
                return "jrxml";
            default:
                return "";
        }
    }
}
