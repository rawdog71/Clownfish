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
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author sulzbachr
 */
@Entity
@Table(name = "cf_attributcontent", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfAttributcontent.findAll", query = "SELECT c FROM CfAttributcontent c"),
    @NamedQuery(name = "CfAttributcontent.findById", query = "SELECT c FROM CfAttributcontent c WHERE c.id = :id"),
    @NamedQuery(name = "CfAttributcontent.findByAttributref", query = "SELECT c FROM CfAttributcontent c WHERE c.attributref = :attributref"),
    @NamedQuery(name = "CfAttributcontent.findByClasscontentref", query = "SELECT c FROM CfAttributcontent c WHERE c.classcontentref = :classcontentref"),
    @NamedQuery(name = "CfAttributcontent.findByContentBoolean", query = "SELECT c FROM CfAttributcontent c WHERE c.contentBoolean = :contentBoolean"),
    @NamedQuery(name = "CfAttributcontent.findByContentInteger", query = "SELECT c FROM CfAttributcontent c WHERE c.contentInteger = :contentInteger"),
    @NamedQuery(name = "CfAttributcontent.findByContentReal", query = "SELECT c FROM CfAttributcontent c WHERE c.contentReal = :contentReal"),
    @NamedQuery(name = "CfAttributcontent.findByContentString", query = "SELECT c FROM CfAttributcontent c WHERE c.contentString = :contentString"),
    @NamedQuery(name = "CfAttributcontent.findByContentDate", query = "SELECT c FROM CfAttributcontent c WHERE c.contentDate = :contentDate"),
    @NamedQuery(name = "CfAttributcontent.findBySalt", query = "SELECT c FROM CfAttributcontent c WHERE c.salt = :salt")})
public class CfAttributcontent implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "attributref")
    private long attributref;
    @Basic(optional = false)
    @NotNull
    @Column(name = "classcontentref")
    private long classcontentref;
    @Column(name = "content_boolean")
    private Boolean contentBoolean;
    @Column(name = "content_integer")
    private BigInteger contentInteger;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "content_real")
    private Double contentReal;
    @Size(max = 256)
    @Column(name = "content_string")
    private String contentString;
    @Lob
    @Size(max = 2147483647)
    @Column(name = "content_text")
    private String contentText;
    @Column(name = "content_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date contentDate;
    @Size(max = 30)
    @Column(name = "salt")
    private String salt;

    public CfAttributcontent() {
    }

    public CfAttributcontent(Long id) {
        this.id = id;
    }

    public CfAttributcontent(Long id, long attributref, long classcontentref) {
        this.id = id;
        this.attributref = attributref;
        this.classcontentref = classcontentref;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getAttributref() {
        return attributref;
    }

    public void setAttributref(long attributref) {
        this.attributref = attributref;
    }

    public long getClasscontentref() {
        return classcontentref;
    }

    public void setClasscontentref(long classcontentref) {
        this.classcontentref = classcontentref;
    }

    public Boolean getContentBoolean() {
        return contentBoolean;
    }

    public void setContentBoolean(Boolean contentBoolean) {
        this.contentBoolean = contentBoolean;
    }

    public BigInteger getContentInteger() {
        return contentInteger;
    }

    public void setContentInteger(BigInteger contentInteger) {
        this.contentInteger = contentInteger;
    }

    public Double getContentReal() {
        return contentReal;
    }

    public void setContentReal(Double contentReal) {
        this.contentReal = contentReal;
    }

    public String getContentString() {
        return contentString;
    }

    public void setContentString(String contentString) {
        this.contentString = contentString;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public Date getContentDate() {
        return contentDate;
    }

    public void setContentDate(Date contentDate) {
        this.contentDate = contentDate;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
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
        if (!(object instanceof CfAttributcontent)) {
            return false;
        }
        CfAttributcontent other = (CfAttributcontent) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfAttributcontent[ id=" + id + " ]";
    }
    
}
