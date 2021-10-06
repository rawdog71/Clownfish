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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

/**
 *
 * @author sulzbachr
 */
@Embeddable
public class CfListcontentPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "listref")
    private long listref;
    @Basic(optional = false)
    @NotNull
    @Column(name = "classcontentref")
    private long classcontentref;

    public CfListcontentPK() {
    }

    public CfListcontentPK(long listref, long classcontentref) {
        this.listref = listref;
        this.classcontentref = classcontentref;
    }

    public long getListref() {
        return listref;
    }

    public void setListref(long listref) {
        this.listref = listref;
    }

    public long getClasscontentref() {
        return classcontentref;
    }

    public void setClasscontentref(long classcontentref) {
        this.classcontentref = classcontentref;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) listref;
        hash += (int) classcontentref;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfListcontentPK)) {
            return false;
        }
        CfListcontentPK other = (CfListcontentPK) object;
        if (this.listref != other.listref) {
            return false;
        }
        if (this.classcontentref != other.classcontentref) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfListcontentPK[ listref=" + listref + ", classcontentref=" + classcontentref + " ]";
    }
    
}
