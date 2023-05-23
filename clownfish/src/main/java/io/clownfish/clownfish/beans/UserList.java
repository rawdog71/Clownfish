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

import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfBackend;
import io.clownfish.clownfish.dbentities.CfUser;
import io.clownfish.clownfish.dbentities.CfUserbackend;
import io.clownfish.clownfish.dbentities.CfUserbackendPK;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfBackendService;
import io.clownfish.clownfish.serviceinterface.CfUserBackendService;
import io.clownfish.clownfish.serviceinterface.CfUserService;
import io.clownfish.clownfish.utils.PasswordUtil;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import javax.persistence.NoResultException;
import jakarta.validation.ConstraintViolationException;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Named("userlist")
@Scope("session")
@Component
public class UserList implements Serializable {
    @Autowired CfUserService cfuserService;
    @Autowired CfBackendService cfbackendService;
    @Autowired CfUserBackendService cfuserbackendService;
    @Autowired transient CfAssetService cfassetService;
    
    private @Getter @Setter List<CfUser> userlist;
    private @Getter @Setter CfUser selectedUser;
    private @Getter @Setter String email;
    private @Getter @Setter String vorname;
    private @Getter @Setter String nachname;
    private @Getter @Setter CfAsset avatar;
    private @Getter @Setter String passwort;
    private @Getter @Setter String passwort_validate;
    private @Getter @Setter boolean newUserButtonDisabled;
    private transient @Getter @Setter List<CfBackend> selectedbackendListcontent = null;
    private transient @Getter @Setter List<CfBackend> backendListcontent = null;
    private @Getter @Setter List<CfAsset> assetlist;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(UserList.class);

    @PostConstruct
    public void init() {
        LOGGER.info("INIT USER START");
        userlist = cfuserService.findAll();
        newUserButtonDisabled = false;
        backendListcontent = cfbackendService.findAll();
        selectedbackendListcontent = new ArrayList<>();
        // only show assets for internal usage
        assetlist = cfassetService.findByAvatars();
        LOGGER.info("INIT USER END");
    }
    
    public void onSelect(SelectEvent event) {
        selectedUser = (CfUser) event.getObject();
        // only show assets for internal usage
        assetlist = cfassetService.findByAvatars();
        
        email = selectedUser.getEmail();
        vorname = selectedUser.getVorname();
        nachname = selectedUser.getNachname();
        avatar = selectedUser.getAssetref();
        //passwort = selectedUser.getPasswort();
        
        newUserButtonDisabled = true;
        
        List<CfUserbackend> selectedcontent = cfuserbackendService.findByUserRef(selectedUser.getId());
        
        selectedbackendListcontent.clear();
        if (!selectedcontent.isEmpty()) {
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
            newuser.setAssetref(avatar);
            newuser.setPasswort(passwort);
            String salt = PasswordUtil.getSalt(30);
            String secure = PasswordUtil.generateSecurePassword(passwort, salt);
            newuser.setSalt(salt);
            newuser.setPasswort(secure);

            if (!selectedbackendListcontent.isEmpty()) {
                for (CfBackend selected : selectedbackendListcontent) {
                    CfUserbackend listcontent = new CfUserbackend();
                    CfUserbackendPK cflistcontentPK = new CfUserbackendPK();
                    cflistcontentPK.setUserref(selectedUser.getId());
                    cflistcontentPK.setBackendref(selected.getId());
                    listcontent.setCfUserbackendPK(cflistcontentPK);
                    cfuserbackendService.create(listcontent);
                }
            }

            cfuserService.create(newuser);

            userlist = cfuserService.findAll();
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    public void onEditUser(ActionEvent actionEvent) {
        try {
            if (null != selectedUser) {
                selectedUser.setVorname(vorname);
                selectedUser.setNachname(nachname);
                if (!passwort.isBlank()) {
                    String salt = PasswordUtil.getSalt(30);
                    String secure = PasswordUtil.generateSecurePassword(passwort, salt);
                    selectedUser.setSalt(salt);
                    selectedUser.setPasswort(secure);
                }
                selectedUser.setAssetref(avatar);

                // Delete listcontent first
                List<CfUserbackend> contentList = cfuserbackendService.findByUserRef(selectedUser.getId());
                for (CfUserbackend content : contentList) {
                    cfuserbackendService.delete(content);
                }
                // Add selected listcontent
                if (!selectedbackendListcontent.isEmpty()) {
                    for (CfBackend selected : selectedbackendListcontent) {
                        CfUserbackend listcontent = new CfUserbackend();
                        CfUserbackendPK cflistcontentPK = new CfUserbackendPK();
                        cflistcontentPK.setUserref(selectedUser.getId());
                        cflistcontentPK.setBackendref(selected.getId());
                        listcontent.setCfUserbackendPK(cflistcontentPK);
                        cfuserbackendService.create(listcontent);
                    }
                }

                cfuserService.edit(selectedUser);
                userlist = cfuserService.findAll();
            }
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
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
        if (!selectedbackendListcontent.isEmpty()) {
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
}
