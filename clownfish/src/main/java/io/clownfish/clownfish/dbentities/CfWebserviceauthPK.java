/*
 * Copyright 2020 SulzbachR.
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
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author SulzbachR
 */
@Embeddable
public class CfWebserviceauthPK implements Serializable {

    //@Basic(optional = false)
    //@NotNull
    //@Column(name = "user_ref")
    //private long userRef;
    //@Basic(optional = false)
    //@NotNull
    //@Column(name = "webservice_ref")
    //private long webserviceRef;
    
    
    @JoinColumn(name = "user_ref", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private CfUser userRef;
    @JoinColumn(name = "webservice_ref", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private CfWebservice webserviceRef;
    

    public CfWebserviceauthPK() {
    }

    public CfWebserviceauthPK(CfUser userRef, CfWebservice webserviceRef) {
        this.userRef = userRef;
        this.webserviceRef = webserviceRef;
    }

    public CfUser getUserRef() {
        return userRef;
    }

    public void setUserRef(CfUser userRef) {
        this.userRef = userRef;
    }

    public CfWebservice getWebserviceRef() {
        return webserviceRef;
    }

    public void setWebserviceRef(CfWebservice webserviceRef) {
        this.webserviceRef = webserviceRef;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) userRef.hashCode();
        hash += (int) webserviceRef.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfWebserviceauthPK)) {
            return false;
        }
        CfWebserviceauthPK other = (CfWebserviceauthPK) object;
        if (this.userRef != other.userRef) {
            return false;
        }
        if (this.webserviceRef != other.webserviceRef) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "io.clownfish.clownfish.dbentities.CfWebserviceauthPK[ userRef=" + userRef + ", webserviceRef=" + webserviceRef + " ]";
    }
    
}
