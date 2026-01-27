/*
 * Copyright 2020 sulzbachr.
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

/**
 *
 * @author sulzbachr
 */
@Embeddable
public class CfUserbackendPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "userref")
    private long userref;
    @Basic(optional = false)
    @NotNull
    @Column(name = "backendref")
    private long backendref;

    public CfUserbackendPK() {
    }

    public CfUserbackendPK(long userref, long backendref) {
        this.userref = userref;
        this.backendref = backendref;
    }

    public long getUserref() {
        return userref;
    }

    public void setUserref(long userref) {
        this.userref = userref;
    }

    public long getBackendref() {
        return backendref;
    }

    public void setBackendref(long backendref) {
        this.backendref = backendref;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) userref;
        hash += (int) backendref;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfUserbackendPK)) {
            return false;
        }
        CfUserbackendPK other = (CfUserbackendPK) object;
        if (this.userref != other.userref) {
            return false;
        }
        if (this.backendref != other.backendref) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfUserbackendPK[ userref=" + userref + ", backendref=" + backendref + " ]";
    }
    
}
