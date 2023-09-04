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

import io.clownfish.clownfish.datamodels.AuthToken;
import io.clownfish.clownfish.datamodels.AuthTokenList;
import io.clownfish.clownfish.dbentities.CfBackend;
import io.clownfish.clownfish.dbentities.CfUser;
import io.clownfish.clownfish.dbentities.CfUserbackend;
import io.clownfish.clownfish.serviceinterface.CfBackendService;
import io.clownfish.clownfish.serviceinterface.CfUserBackendService;
import io.clownfish.clownfish.serviceinterface.CfUserService;
import io.clownfish.clownfish.utils.ApiKeyUtil;
import io.clownfish.clownfish.utils.LoginJob;
import io.clownfish.clownfish.utils.PasswordUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.persistence.NoResultException;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.util.DefaultPropertiesPersister;

/**
 *
 * @author sulzbachr
 */

@Named("loginBean")
@Scope(value="session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class LoginBean implements Serializable {
    @Autowired transient CfUserService cfuserService;
    @Autowired transient TemplateList templateList;
    @Autowired transient SiteTreeBean sitetree;
    @Autowired transient CfUserBackendService cfuserbackendService;
    @Autowired transient CfBackendService cfbackendService;
    @Autowired transient AuthTokenList authtokenlist;
    @Autowired ApiKeyUtil apikeyutil;
    
    private boolean login;
    private @Getter @Setter String vorname;
    private @Getter @Setter String nachname;
    private @Getter @Setter String email;
    private @Getter @Setter String passwort;
    private @Getter @Setter CfUser cfuser;
    private @Getter @Setter String apikey = "";
    private @Getter @Setter List<CfBackend> userrights = null;
    private @Getter @Setter String token = "";
    @Value("${logintimeout:10}") int MINUTES;
    private LoginJob job = new LoginJob();
    
    final transient Logger LOGGER = LoggerFactory.getLogger(LoginBean.class);

    public LoginBean() {
        login = false;
    }
    
    @PostConstruct
    public void init() {
        login = false;
        userrights = new ArrayList<>();
    }

    public boolean isLogin() {
        return login;
    }
    
    public boolean hasRights(int tab) {
        boolean rights = false;
        for (CfBackend bc : userrights) {
            if (bc.getId() == tab) {
                rights = true;
            }
        }
        return rights;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }
    
    public void onLogin(ActionEvent actionEvent) {
        if (job.getTries() > 3) {
            token = "";
            login = false;
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Too many trials");
            FacesContext.getCurrentInstance().addMessage(null, message);
            if (!job.isRunning()) {
                job.setRunning(true);
                DateTime datetime = new DateTime();
                Timer timer = new Timer();
                timer.schedule(job, datetime.plusMinutes(MINUTES).toDate());
            } else {
                InputStream fis = null;
                try {
                    Properties props = new Properties();
                    String propsfile = "application.properties";
                    fis = new FileInputStream(propsfile);
                    if (null != fis) {
                        props.load(fis);
                    }
                    int refreshlogin = Integer.parseInt(props.getProperty("loginrefresh"));
                    if (1 == refreshlogin) {
                        job.setRunning(false);
                        job.setTries(0);
                        
                        File f = new File("application.properties");
                        OutputStream out = new FileOutputStream(f);
                        props.setProperty("loginrefresh", String.valueOf("0"));
                        DefaultPropertiesPersister p = new DefaultPropertiesPersister();
                        p.store(props, out, "Bootstrap properties");
                        out.close();
                    }
                } catch (FileNotFoundException ex) {
                    LOGGER.error(ex.getMessage());
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage());
                } finally {
                    try {
                        if (null != fis) {
                            fis.close();
                        }
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                }
            }
        } else {
            try {
                //String remoteAddress = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest().getRemoteAddr();
                cfuser = cfuserService.findByEmail(email);
                String salt = cfuser.getSalt();
                String secure = PasswordUtil.generateSecurePassword(passwort, salt);
                if (secure.compareTo(cfuser.getPasswort()) == 0) {
                    token = AuthToken.generateToken(passwort, salt);
                    AuthToken at = new AuthToken(token, new DateTime().plusMinutes(60), cfuser);          // Tokens valid for 60 minutes
                    authtokenlist.getAuthtokens().put(token, at);
                    login = true;
                    List<CfUserbackend> selectedcontent = cfuserbackendService.findByUserRef(cfuser.getId());
                    userrights.clear();
                    if (!selectedcontent.isEmpty()) {
                        for (CfUserbackend listcontent : selectedcontent) {
                            CfBackend selectedContent = cfbackendService.findById(listcontent.getCfUserbackendPK().getBackendref());
                            userrights.add(selectedContent);
                        }
                    }
                    apikey = apikeyutil.getRestApikey(cfuser);
                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Login", "Welcome " + cfuser.getVorname());
                    FacesContext.getCurrentInstance().addMessage(null, message);
                } else {
                    token = "";
                    login = false;
                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Wrong password or wrong e-mail");
                    FacesContext.getCurrentInstance().addMessage(null, message);
                    job.setTries(job.getTries()+1);
                }
            } catch (NoResultException ex) {
                token = "";
                login = false;
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Wrong password or wrong e-mail");
                FacesContext.getCurrentInstance().addMessage(null, message);
                job.setTries(job.getTries()+1);
            }
        }
    }
    
    public void onLogout() {
        userrights.clear();
        templateList.setSelectedTemplate(null);
        templateList.setTemplateName("");
        sitetree.setSelectedSite(null);
        sitetree.setSiteName("");
        sitetree.setSelectedTemplate(null);
        sitetree.setSelectedStylesheet(null);
        sitetree.setSelectedJavascript(null);
        sitetree.getSelectedDatasources().clear();
        login = false;
    }
}
