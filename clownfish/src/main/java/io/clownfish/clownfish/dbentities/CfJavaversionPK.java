package io.clownfish.clownfish.dbentities;


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import javax.validation.constraints.NotNull;

@Embeddable
public class CfJavaversionPK implements Serializable
{
    @Basic(optional = false)
    @NotNull
    @Column(name = "javaref")
    private long javaref;
    @Basic(optional = false)
    @NotNull
    @Column(name = "version")
    private long version;

    public CfJavaversionPK() {}

    public CfJavaversionPK(long javaref, long version)
    {
        this.javaref = javaref;
        this.version = version;
    }

    public long getJavaref()
    {
        return javaref;
    }

    public void setJavaref(long javaref)
    {
        this.javaref = javaref;
    }

    public long getVersion()
    {
        return version;
    }

    public void setVersion(long version)
    {
        this.version = version;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (int) javaref;
        hash += (int) version;
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfJavaversionPK))
        {
            return false;
        }
        CfJavaversionPK other = (CfJavaversionPK) object;
        if (this.javaref != other.javaref)
            return false;

        if (this.version != other.version)
            return false;

        return true;
    }

    @Override
    public String toString()
    {
        return "io.clownfish.clownfish.dbentities.CfJavaversionPK[ javaref=" + javaref + ", version=" + version + " ]";
    }
}
