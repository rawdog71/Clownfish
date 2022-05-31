/*
 * Copyright 2022 raine.
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
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author raine
 */
@Entity
@Table(name = "cf_searchdatabase", catalog = "clownfish", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfSearchdatabase.findAll", query = "SELECT c FROM CfSearchdatabase c"),
    @NamedQuery(name = "CfSearchdatabase.findByDatasourceRef", query = "SELECT c FROM CfSearchdatabase c WHERE c.cfSearchdatabasePK.datasourceRef = :datasourceRef"),
    @NamedQuery(name = "CfSearchdatabase.findByTablename", query = "SELECT c FROM CfSearchdatabase c WHERE c.cfSearchdatabasePK.tablename = :tablename"),
    @NamedQuery(name = "CfSearchdatabase.findByDatasourceRefAndTable", query = "SELECT c FROM CfSearchdatabase c WHERE c.cfSearchdatabasePK.datasourceRef = :datasourceRef AND c.cfSearchdatabasePK.tablename = :tablename")        
})
public class CfSearchdatabase implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CfSearchdatabasePK cfSearchdatabasePK;

    public CfSearchdatabase() {
    }

    public CfSearchdatabase(CfSearchdatabasePK cfSearchdatabasePK) {
        this.cfSearchdatabasePK = cfSearchdatabasePK;
    }

    public CfSearchdatabase(long datasourceRef, String tablename) {
        this.cfSearchdatabasePK = new CfSearchdatabasePK(datasourceRef, tablename);
    }

    public CfSearchdatabasePK getCfSearchdatabsePK() {
        return cfSearchdatabasePK;
    }

    public void setCfSearchdatabsePK(CfSearchdatabasePK cfSearchdatabasePK) {
        this.cfSearchdatabasePK = cfSearchdatabasePK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cfSearchdatabasePK != null ? cfSearchdatabasePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfSearchdatabase)) {
            return false;
        }
        CfSearchdatabase other = (CfSearchdatabase) object;
        if ((this.cfSearchdatabasePK == null && other.cfSearchdatabasePK != null) || (this.cfSearchdatabasePK != null && !this.cfSearchdatabasePK.equals(other.cfSearchdatabasePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbrepository.CfSearchdatabse[ cfSearchdatabasePK=" + cfSearchdatabasePK + " ]";
    }
    
}
