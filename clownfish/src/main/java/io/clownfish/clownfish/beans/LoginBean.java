/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.beans;

import io.clownfish.clownfish.dbentities.CfUser;
import io.clownfish.clownfish.serviceinterface.CfUserService;
import io.clownfish.clownfish.utils.PasswordUtil;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.persistence.NoResultException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */

@ManagedBean(name="loginBean")
@Component
public class LoginBean implements Serializable {
    @Autowired CfUserService cfuserService;
    
    private @Getter @Setter boolean login;
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
