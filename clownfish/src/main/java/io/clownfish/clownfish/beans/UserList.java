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

import io.clownfish.clownfish.dbentities.CfUser;
import io.clownfish.clownfish.serviceinterface.CfUserService;
import io.clownfish.clownfish.utils.PasswordUtil;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Transactional
@Named("userlist")
@ViewScoped
@Component
public class UserList {
    @Autowired CfUserService cfuserService;
    
    private @Getter @Setter List<CfUser> userlist;
    private @Getter @Setter CfUser selectedUser;
    private @Getter @Setter String email;
    private @Getter @Setter String vorname;
    private @Getter @Setter String nachname;
    private @Getter @Setter String passwort;
    private @Getter @Setter String passwort_validate;
    private @Getter @Setter boolean newUserButtonDisabled;

    @PostConstruct
    public void init() {
        userlist = cfuserService.findAll();
        newUserButtonDisabled = false;
    }
    
    public void onSelect(SelectEvent event) {
        selectedUser = (CfUser) event.getObject();
        
        email = selectedUser.getEmail();
        vorname = selectedUser.getVorname();
        nachname = selectedUser.getNachname();
        passwort = selectedUser.getPasswort();
        
        newUserButtonDisabled = true;
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
            System.out.println(ex.getMessage());
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
            System.out.println(ex.getMessage());
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
            CfUser validateList = cfuserService.findByEmail(email);
            newUserButtonDisabled = true;
        } catch (NoResultException ex) {
            newUserButtonDisabled = email.isEmpty();
        }
    }
}
