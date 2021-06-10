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
import java.util.Locale;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author sulzbachr
 */
@Entity
@Table(name = "cf_attributcontent", catalog = "clownfish", schema = "")
@Cacheable(false)
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
    @NamedQuery(name = "CfAttributcontent.findBySalt", query = "SELECT c FROM CfAttributcontent c WHERE c.salt = :salt"),
    @NamedQuery(name = "CfAttributcontent.findByContentclassRef", query = "SELECT c FROM CfAttributcontent c WHERE c.classcontentlistref = :classcontentlistref"),
    @NamedQuery(name = "CfAttributcontent.findByContentAssetRef", query = "SELECT c FROM CfAttributcontent c WHERE c.assetcontentlistref = :assetcontentlistref"),
    @NamedQuery(name = "CfAttributcontent.findByAttributrefAndClasscontentref", query = "SELECT c FROM CfAttributcontent c WHERE c.attributref = :attributref AND c.classcontentref = :classcontentref"),
    @NamedQuery(name = "CfAttributcontent.findByIndexed", query = "SELECT c FROM CfAttributcontent c WHERE c.indexed = :indexed"),
    @NamedQuery(name = "CfAttributcontent.findByIndexedAndSearchrelevant", query = "SELECT atc FROM CfAttributcontent atc INNER JOIN CfAttribut atr ON atc.attributref = atr.id INNER JOIN CfAttributetype att ON atr.attributetype = att.id WHERE att.searchrelevant = true AND atc.indexed = :indexed")
})
public class CfAttributcontent implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @JoinColumn(name = "attributref", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private CfAttribut attributref;
    @JoinColumn(name = "classcontentref", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private CfClasscontent classcontentref;
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
    @Column(name = "indexed")
    private boolean indexed;
    @JoinColumn(name = "content_classref", referencedColumnName = "id")
    @ManyToOne(optional = true)
    private CfList classcontentlistref;
    @JoinColumn(name = "content_assetref", referencedColumnName = "id")
    @ManyToOne(optional = true)
    private CfAssetlist assetcontentlistref;
    
    public CfAttributcontent() {
    }

    public CfAttributcontent(Long id) {
        this.id = id;
    }

    public CfAttributcontent(Long id, CfAttribut attributref, CfClasscontent classcontentref) {
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

    public CfAttribut getAttributref() {
        return attributref;
    }

    public void setAttributref(CfAttribut attributref) {
        this.attributref = attributref;
    }

    public CfClasscontent getClasscontentref() {
        return classcontentref;
    }

    public void setClasscontentref(CfClasscontent classcontentref) {
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

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public CfList getClasscontentlistref() {
        return classcontentlistref;
    }

    public void setClasscontentlistref(CfList classcontentlistref) {
        this.classcontentlistref = classcontentlistref;
    }

    public CfAssetlist getAssetcontentlistref() {
        return assetcontentlistref;
    }

    public void setAssetcontentlistref(CfAssetlist assetcontentlistref) {
        this.assetcontentlistref = assetcontentlistref;
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
        switch (attributref.getAttributetype().getId().intValue()) {
            case 1: // boolean
                if (null != getContentBoolean()) {
                    return getContentBoolean().toString();
                } else {
                    return "";
                }    
            case 2: // string
                if (null != getContentString()) {
                    return getContentString();
                } else {
                    return "";
                }
            case 3: // integer
                if (null != getContentInteger()) {
                    return getContentInteger().toString();
                } else {
                    return "";
                }
            case 4: // real
                if (null != getContentReal()) {
                    return getContentReal().toString();
                } else {
                    return "";
                }    
            case 5: // htmltext (formatted)
                if (null != getContentText()) {
                    return getContentText();
                } else {
                    return "";
                }
            case 6: // datetime
                if (null != getContentDate()) {
                    DateTime dt = new DateTime(getContentDate());
                    DateTimeFormatter dtf1 = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy").withLocale(Locale.GERMANY);
                    
                    dt.toString(dtf1);
                    DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.yyyy");
                    
                    return dt.toString(dtf);
                } else {
                    return "";
                }
            case 7: // hashstring (crypted with salt - for passwords)
                if (null != getContentString()) {
                    return getContentString();
                } else {
                    return "";
                }
            case 8: // media (id to asset)
                if (null != getContentInteger()) {
                    return getContentInteger().toString();
                } else {
                    return "";
                }
            case 9: // text (unformatted)
                if (null != getContentText()) {
                    return getContentText();
                } else {
                    return "";
                }
            case 10: // text (markdown formatted)
                if (null != getContentText()) {
                    return getContentText();
                } else {
                    return "";
                } 
            case 11: // 
                if (null != getClasscontentlistref()) {
                    return getClasscontentlistref().getName();
                } else {
                    return "";
                }
            case 12: // 
                if (null != getAssetcontentlistref()) {
                    return getAssetcontentlistref().getName();
                } else {
                    return "";
                }     
        }
        return "?";
    }
    
}
