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
public class CfAssetkeywordPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "assetref")
    private long assetref;
    @Basic(optional = false)
    @NotNull
    @Column(name = "keywordref")
    private long keywordref;

    public CfAssetkeywordPK() {
    }

    public CfAssetkeywordPK(long assetref, long keywordref) {
        this.assetref = assetref;
        this.keywordref = keywordref;
    }

    public long getAssetref() {
        return assetref;
    }

    public void setAssetref(long assetref) {
        this.assetref = assetref;
    }

    public long getKeywordref() {
        return keywordref;
    }

    public void setKeywordref(long keywordref) {
        this.keywordref = keywordref;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) assetref;
        hash += (int) keywordref;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfAssetkeywordPK)) {
            return false;
        }
        CfAssetkeywordPK other = (CfAssetkeywordPK) object;
        if (this.assetref != other.assetref) {
            return false;
        }
        if (this.keywordref != other.keywordref) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfAssetkeywordPK[ assetref=" + assetref + ", keywordref=" + keywordref + " ]";
    }
    
}
