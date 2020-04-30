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
package io.clownfish.clownfish.beans;

import io.clownfish.clownfish.dbentities.CfBackend;
import io.clownfish.clownfish.dbentities.CfUser;
import io.clownfish.clownfish.dbentities.CfWebservice;
import io.clownfish.clownfish.dbentities.CfWebserviceauth;
import io.clownfish.clownfish.dbentities.CfWebserviceauthPK;
import io.clownfish.clownfish.serviceinterface.CfBackendService;
import io.clownfish.clownfish.serviceinterface.CfUserBackendService;
import io.clownfish.clownfish.serviceinterface.CfUserService;
import io.clownfish.clownfish.serviceinterface.CfWebserviceService;
import io.clownfish.clownfish.serviceinterface.CfWebserviceauthService;
import io.clownfish.clownfish.utils.PasswordUtil;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Named("webserviceauthlist")
@Scope(value="session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class WebserviceAuthList {
    @Autowired CfUserService cfuserService;
    @Autowired CfWebserviceauthService cfwebserviceauthService;
    @Autowired CfWebserviceService cfwebserviceService;
    
    private @Getter @Setter List<CfWebserviceauth> webserviceauthlist;
    private @Getter @Setter CfWebserviceauth selectedWebserviceauth;
    private @Getter @Setter List<CfWebservice> webservicelist;
    private @Getter @Setter CfWebservice selectedWebservice;
    private @Getter @Setter boolean newAuthButtonDisabled;
    private @Getter @Setter CfUser currentUser;
    private transient @Getter @Setter List<CfBackend> selectedbackendListcontent = null;
    private transient @Getter @Setter List<CfBackend> backendListcontent = null;
    
    final transient Logger logger = LoggerFactory.getLogger(WebserviceAuthList.class);

    @PostConstruct
    public void init() {
        webservicelist = cfwebserviceService.findAll();
        newAuthButtonDisabled = false;
    }
    
    public void setUser(CfUser user) {
        currentUser = user;
        webserviceauthlist = cfwebserviceauthService.findByUserRef(currentUser);
    }
    
    public void onSelect(SelectEvent event) {
        selectedWebserviceauth = (CfWebserviceauth) event.getObject();
    }
    
    public void onCreateWebserviceAuth(ActionEvent actionEvent) {
        CfWebserviceauth webserviceauth = new CfWebserviceauth();
        CfWebserviceauthPK cfWebserviceauthPK = new CfWebserviceauthPK();
        cfWebserviceauthPK.setUserRef(currentUser);
        cfWebserviceauthPK.setWebserviceRef(selectedWebservice);
        webserviceauth.setCfWebserviceauthPK(cfWebserviceauthPK);
        String hash = PasswordUtil.generateSecurePassword(currentUser.getEmail()+selectedWebservice.getName(), currentUser.getEmail()+selectedWebservice.getName());
        webserviceauth.setHash(hash);
        cfwebserviceauthService.create(webserviceauth);
    }
    
    /*
    public void onSelect(SelectEvent event) {
        selectedUser = (CfUser) event.getObject();
        
        email = selectedUser.getEmail();
        vorname = selectedUser.getVorname();
        nachname = selectedUser.getNachname();
        passwort = selectedUser.getPasswort();
        
        newUserButtonDisabled = true;
        
        List<CfUserbackend> selectedcontent = cfuserbackendService.findByUserRef(selectedUser.getId());
        
        selectedbackendListcontent.clear();
        if (selectedcontent.size() > 0) {
            for (CfUserbackend listcontent : selectedcontent) {
                CfBackend selectedContent = cfbackendService.findById(listcontent.getCfUserbackendPK().getBackendref());
                selectedbackendListcontent.add(selectedContent);
            }
        }
    }
    
    public void onCreateUser(ActionEvent actionEvent) {
        try {
            CfUser newuser = new CfUser();
            newuser.setEmail(email);
            newuser.setVorname(vorname);
            newuser.setNachname(nachname);
            newuser.setPasswort(passwort);
            String salt = PasswordUtil.getSalt(30);
            String secure = PasswordUtil.generateSecurePassword(passwort, salt);
            newuser.setSalt(salt);
            newuser.setPasswort(secure);
            cfuserService.create(newuser);

            userlist = cfuserService.findAll();
        } catch (ConstraintViolationException ex) {
            logger.error(ex.getMessage());
        }
    }
    
    public void onEditUser(ActionEvent actionEvent) {
        try {
            if (null != selectedUser) {
                String salt = PasswordUtil.getSalt(30);
                String secure = PasswordUtil.generateSecurePassword(passwort, salt);
                selectedUser.setSalt(salt);
                selectedUser.setPasswort(secure);
                cfuserService.edit(selectedUser);
                userlist = cfuserService.findAll();
            }
        } catch (ConstraintViolationException ex) {
            logger.error(ex.getMessage());
        }
    }
    
    public void onDeleteUser(ActionEvent actionEvent) {
        if (null != selectedUser) {
            cfuserService.delete(selectedUser);
            userlist = cfuserService.findAll();
        }
    }
    
    public void onChangeName(ValueChangeEvent changeEvent) {
        try {
            cfuserService.findByEmail(email);
            newUserButtonDisabled = true;
        } catch (NoResultException ex) {
            newUserButtonDisabled = email.isEmpty();
        }
    }
    
    public void onChangeContent(AjaxBehaviorEvent event) {
        // Delete listcontent first
        List<CfUserbackend> contentList = cfuserbackendService.findByUserRef(selectedUser.getId());
        for (CfUserbackend content : contentList) {
            cfuserbackendService.delete(content);
        }
        // Add selected listcontent
        if (selectedbackendListcontent.size() > 0) {
            for (CfBackend selected : selectedbackendListcontent) {
                CfUserbackend listcontent = new CfUserbackend();
                CfUserbackendPK cflistcontentPK = new CfUserbackendPK();
                cflistcontentPK.setUserref(selectedUser.getId());
                cflistcontentPK.setBackendref(selected.getId());
                listcontent.setCfUserbackendPK(cflistcontentPK);
                cfuserbackendService.create(listcontent);
            }
        }
    }
    */
}
