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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author raine
 */
@Embeddable
public class CfSearchdatabasePK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "datasource_ref")
    private long datasourceRef;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(name = "tablename")
    private String tablename;

    public CfSearchdatabasePK() {
    }

    public CfSearchdatabasePK(long datasourceRef, String tablename) {
        this.datasourceRef = datasourceRef;
        this.tablename = tablename;
    }

    public long getDatasourceRef() {
        return datasourceRef;
    }

    public void setDatasourceRef(long datasourceRef) {
        this.datasourceRef = datasourceRef;
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) datasourceRef;
        hash += (tablename != null ? tablename.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfSearchdatabasePK)) {
            return false;
        }
        CfSearchdatabasePK other = (CfSearchdatabasePK) object;
        if (this.datasourceRef != other.datasourceRef) {
            return false;
        }
        if ((this.tablename == null && other.tablename != null) || (this.tablename != null && !this.tablename.equals(other.tablename))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbrepository.CfSearchdatabsePK[ datasourceRef=" + datasourceRef + ", tablename=" + tablename + " ]";
    }
    
}
