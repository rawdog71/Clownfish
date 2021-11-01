/*
 * Copyright 2021 raine.
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

import io.clownfish.clownfish.utils.ClownfishUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.apache.catalina.util.ServerInfo;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author raine
 */
@Named("versionBean")
@Scope("singleton")
@Component
public class VersionBean implements Serializable {
    @Autowired @Getter @Setter ClownfishUtil clownfishUtil;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(VersionBean.class);
    
    public VersionBean() {
    }
    
    @PostConstruct
    public void init() {
        Package p = FacesContext.class.getPackage();
        if (null == clownfishUtil) {
            clownfishUtil = new ClownfishUtil();
        }
        
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = null;
        if ((new File("pom.xml")).exists()) {
            try {
                model = reader.read(new FileReader("pom.xml"));
            } catch (FileNotFoundException ex) {
                LOGGER.error(ex.getMessage());
            } catch (IOException | XmlPullParserException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        if (null != model) {
            clownfishUtil.setVersion(model.getVersion()).setVersionMojarra(p.getImplementationVersion()).setVersionTomcat(ServerInfo.getServerNumber());
        } else {
            clownfishUtil.setVersion(getClass().getPackage().getImplementationVersion()).setVersionMojarra(p.getImplementationVersion()).setVersionTomcat(ServerInfo.getServerNumber());
        }
    }
}
