package io.clownfish.clownfish.dbentities;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigInteger;

@Entity
@Table(name = "cf_java", catalog = "clownfish", schema = "")
@Cacheable(false)
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "CfJava.findAll", query = "SELECT c FROM CfJava c"),
        @NamedQuery(name = "CfJava.findById", query = "SELECT c FROM CfJava c WHERE c.id = :id"),
        @NamedQuery(name = "CfJava.findByName", query = "SELECT c FROM CfJava c WHERE c.name = :name"),
        @NamedQuery(name = "CfJava.findByCheckedoutby", query = "SELECT c FROM CfJava c WHERE c.checkedoutby = :checkedoutby")})
public class CfJava implements Serializable {

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
    @Lob
    @Size(max = 16777215)
    @Column(name = "content")
    private String content;
    @Column(name = "checkedoutby")
    private BigInteger checkedoutby;
    @Basic(optional = false)
    @NotNull
    @Column(name = "language")
    private int language;
    // @Column(name = "compilestatus")
    // private boolean compileStatus;

    public CfJava() {}

    public CfJava(Long id) {
        this.id = id;
    }

    public CfJava(Long id, String name)
    {
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public BigInteger getCheckedoutby() {
        return checkedoutby;
    }

    public void setCheckedoutby(BigInteger checkedoutby) {
        this.checkedoutby = checkedoutby;
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }
    
    public String getNameExt() {
        String extension = "";
        switch (getLanguage()) {
            case 0:
                extension = ".java";
                break;
            case 1:
                extension = ".kt";
                break;
            case 2:
                extension = ".groovy";
                break;
        }
        return name + extension;
    }
    
    // public boolean getCompileStatus() { return compileStatus; }

    // public void setCompileStatus(boolean status) { compileStatus = status; }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in case the id fields are not set
        if (!(object instanceof CfJava))
            return false;

        CfJava other = (CfJava) object;

        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)))
            return false;

        return true;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
