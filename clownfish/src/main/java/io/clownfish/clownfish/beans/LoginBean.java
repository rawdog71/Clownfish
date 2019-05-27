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
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.persistence.NoResultException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author sulzbachr
 */

@Named("loginBean")
@Scope("session")
public class LoginBean implements Serializable {
    @Autowired transient CfUserService cfuserService;
    
    private boolean login;
    private @Getter @Setter String vorname;
    private @Getter @Setter String nachname;
    private @Getter @Setter String email;
    private @Getter @Setter String passwort;
    private @Getter @Setter CfUser cfuser;

    public LoginBean() {
        login = false;
    }
    
    @PostConstruct
    public void init() {
        login = false;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }
    
    public void onLogin(ActionEvent actionEvent) {
        try {
            cfuser = cfuserService.findByEmail(email);
            String salt = cfuser.getSalt();
            String secure = PasswordUtil.generateSecurePassword(passwort, salt);
            if (secure.compareTo(cfuser.getPasswort()) == 0) {
                login = true;
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Login", "Willkommen " + cfuser.getVorname());
                FacesContext.getCurrentInstance().addMessage(null, message);
            } else {
                login = false;
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Falsches Passwort oder falsche E-Mail");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        } catch (NoResultException ex) {
            login = false;
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Falsches Passwort oder falsche E-Mail");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }          
    }
    
    public void onLogout() {
        login = false;
    }
}
