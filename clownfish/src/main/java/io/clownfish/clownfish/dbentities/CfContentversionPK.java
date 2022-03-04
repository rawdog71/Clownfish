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

/**
 *
 * @author raine
 */
@Embeddable
public class CfContentversionPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "contentref")
    private long contentref;
    @Basic(optional = false)
    @NotNull
    @Column(name = "version")
    private long version;

    public CfContentversionPK() {
    }

    public CfContentversionPK(long contentref, long version) {
        this.contentref = contentref;
        this.version = version;
    }

    public long getContentref() {
        return contentref;
    }

    public void setContentref(long contentref) {
        this.contentref = contentref;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) contentref;
        hash += (int) version;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfContentversionPK)) {
            return false;
        }
        CfContentversionPK other = (CfContentversionPK) object;
        if (this.contentref != other.contentref) {
            return false;
        }
        if (this.version != other.version) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfContentversionPK[ contentref=" + contentref + ", version=" + version + " ]";
    }
    
}
