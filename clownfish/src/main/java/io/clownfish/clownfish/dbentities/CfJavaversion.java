package io.clownfish.clownfish.dbentities;


import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "cf_javaversion", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "CfJavaversion.findAll", query = "SELECT c FROM CfJavaversion c"),
        @NamedQuery(name = "CfJavaversion.findByJavaref", query = "SELECT c FROM CfJavaversion c WHERE c.cfJavaversionPK.javaref = :javaref"),
        @NamedQuery(name = "CfJavaversion.findByVersion", query = "SELECT c FROM CfJavaversion c WHERE c.cfJavaversionPK.version = :version"),
        @NamedQuery(name = "CfJavaversion.findByPK", query = "SELECT c FROM CfJavaversion c WHERE c.cfJavaversionPK.javaref = :javaref AND c.cfJavaversionPK.version = :version"),
        @NamedQuery(name = "CfJavaversion.findByTstamp", query = "SELECT c FROM CfJavaversion c WHERE c.tstamp = :tstamp"),
        @NamedQuery(name = "CfJavaversion.findByCommitedby", query = "SELECT c FROM CfJavaversion c WHERE c.commitedby = :commitedby"),
        @NamedQuery(name = "CfJavaversion.findMaxVersion", query = "SELECT MAX(c.cfJavaversionPK.version) FROM CfJavaversion c WHERE c.cfJavaversionPK.javaref = :javaref")
})
public class CfJavaversion implements Serializable
{
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfJavaversionPK cfJavaversionPK;
    @Lob
    @Column(name = "content")
    private byte[] content;
    @Basic(optional = false)
    @NotNull
    @Column(name = "tstamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date tstamp;
    @Column(name = "commitedby")
    private BigInteger commitedby;

    public CfJavaversion() {}

    public CfJavaversion(CfJavaversionPK cfJavaversionPK)
    {
        this.cfJavaversionPK = cfJavaversionPK;
    }

    public CfJavaversion(CfJavaversionPK cfJavaversionPK, Date tstamp)
    {
        this.cfJavaversionPK = cfJavaversionPK;
        this.tstamp = tstamp;
    }

    public CfJavaversion(long javaref, long version)
    {
        this.cfJavaversionPK = new CfJavaversionPK(javaref, version);
    }

    public CfJavaversionPK getCfJavaversionPK()
    {
        return cfJavaversionPK;
    }

    public void setCfJavaversionPK(CfJavaversionPK cfJavaversionPK)
    {
        this.cfJavaversionPK = cfJavaversionPK;
    }

    public byte[] getContent()
    {
        return content;
    }

    public void setContent(byte[] content)
    {
        this.content = content;
    }

    public Date getTstamp()
    {
        return tstamp;
    }

    public void setTstamp(Date tstamp)
    {
        this.tstamp = tstamp;
    }

    public BigInteger getCommitedby()
    {
        return commitedby;
    }

    public void setCommitedby(BigInteger commitedby)
    {
        this.commitedby = commitedby;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (cfJavaversionPK != null ? cfJavaversionPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in case the id fields are not set
        if (!(object instanceof CfJavaversion))
            return false;

        CfJavaversion other = (CfJavaversion) object;

        if ((this.cfJavaversionPK == null && other.cfJavaversionPK != null) || (this.cfJavaversionPK != null && !this.cfJavaversionPK.equals(other.cfJavaversionPK)))
            return false;

        return true;
    }

    @Override
    public String toString()
    {
        return "io.clownfish.clownfish.dbentities.CfJavaversion[ cfJavaversionPK=" + cfJavaversionPK + " ]";
    }
}