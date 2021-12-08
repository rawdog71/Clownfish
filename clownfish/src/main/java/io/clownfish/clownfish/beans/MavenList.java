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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.clownfish.clownfish.dbentities.CfMaven;
import io.clownfish.clownfish.serviceinterface.CfMavenService;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import java.util.Map;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import io.clownfish.clownfish.datamodels.MvnDocs;
import io.clownfish.clownfish.utils.ClassPathUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.faces.event.ActionEvent;
import javax.persistence.NoResultException;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author sulzbachr
 */
@Named("mavenList")
@Scope("singleton")
@Component
public class MavenList implements Serializable {
    @Autowired transient CfMavenService cfmavenService;
    @Autowired transient PropertyUtil propertyUtil;
    @Autowired transient ClassPathUtil classpathUtil;
    
    private transient @Getter @Setter List<CfMaven> mavenlist = null;
    private @Getter @Setter CfMaven selectedMaven = null;
    private @Getter @Setter String group;
    private @Getter @Setter String artifact;
    private @Getter @Setter String version;
    private @Getter @Setter String packaging;
    private @Getter @Setter boolean newContentButtonDisabled = false;
    private @Getter @Setter List<MvnDocs> mvnDocs = null;
    private @Getter @Setter MvnDocs selectedMavenDoc = null;
    String mavenpath;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(MavenList.class);

    @PostConstruct
    public void init() {
        LOGGER.info("INIT MAVENLIST START");
        mavenlist = cfmavenService.findAll();
        packaging = "jar";
        newContentButtonDisabled = false;
        mvnDocs = new ArrayList<MvnDocs>();
        mavenpath = propertyUtil.getPropertyValue("folder_maven");
        LOGGER.info("INIT MAVENLIST END");
    }
    
    public void onRefreshAll() {
        mavenlist = cfmavenService.findAll();
    }
    
    /**
     * Selects an external datasource
     * @param event
     */
    public void onSelectMavenDoc(SelectEvent event) {
        selectedMavenDoc = (MvnDocs) event.getObject();
    }
    
    /**
     * Selects an external datasource
     * @param event
     */
    public void onSelectMaven(SelectEvent event) {
        selectedMaven = (CfMaven) event.getObject();
    }
    
    /**
     * Deletes an external datasource
     * @param actionEvent
     */
    public void onInstall(ActionEvent actionEvent) {
        if (null != selectedMavenDoc) {
            CfMaven maven = new CfMaven();
            maven.setMavenArtifact(selectedMavenDoc.getA());
            maven.setMavenGroup(selectedMavenDoc.getG());
            maven.setMavenLatestversion(selectedMavenDoc.getLatestVersion());
            maven.setMavenPackage(selectedMavenDoc.getP());
            maven.setMavenId(selectedMavenDoc.getId());
            maven.setMavenFilename(selectedMavenDoc.getA() + "-" + selectedMavenDoc.getLatestVersion() + "." + selectedMavenDoc.getP());
            
            try {
                CfMaven dummy = cfmavenService.findByMavenId(selectedMavenDoc.getId());
            } catch (NoResultException ex) {
                maven = cfmavenService.create(maven);
                String filepath = maven.getMavenGroup().replaceAll("\\.", "/") + "/" + maven.getMavenArtifact() + "/" + maven.getMavenLatestversion();
                downloadMaven(filepath, maven.getMavenFilename());
                classpathUtil.addPath(mavenpath);
                mavenlist = cfmavenService.findAll();
            }
        }
    }
    
    /**
     * Deletes an external datasource
     * @param actionEvent
     */
    public void onDeinstall(ActionEvent actionEvent) {
        if (null != selectedMaven) {
            cfmavenService.delete(selectedMaven);
            new File(mavenpath + File.separator + selectedMaven.getMavenFilename()).delete();
            classpathUtil.addPath(mavenpath);
            mavenlist = cfmavenService.findAll();
        }
    }
    
    /**
     * Deletes an external datasource
     * @param actionEvent
     */
    public void onSearch(ActionEvent actionEvent) {
        selectedMavenDoc = null;
        StringBuilder s_url = new StringBuilder();
        s_url.append("https://search.maven.org/solrsearch/select?q=");
        StringBuilder s_query = new StringBuilder();
        if (!packaging.isBlank()) {
            s_query.append("p:").append(packaging).append(" AND ");
        }
        if (!group.isBlank()) {
            s_query.append("g:").append(group).append(" AND ");
        }
        if (!artifact.isBlank()) {
            s_query.append("a:").append(artifact).append(" AND ");
        }
        if (!version.isBlank()) {
            s_query.append("v:").append(version).append(" AND ");
        }
        s_url.append(s_query.substring(0, s_query.length()-5));
        s_url.append("&rows=200&wt=json");
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(s_url.toString(), String.class);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            Map<String, Object> result = mapper.convertValue(root, new TypeReference<Map<String, Object>>(){});
            
            Map<String, Object> mvnResponse = (Map<String, Object>) result.get("response");
            List<Map<String, Object>> mvnDocsList = (List<Map<String, Object>>) mvnResponse.get("docs");
            mvnDocs.clear();
            for (Map<String, Object> doc : mvnDocsList) {
                MvnDocs mvnDoc = new MvnDocs();
                mvnDoc.setId(doc.get("id").toString());
                mvnDoc.setA(doc.get("a").toString());
                mvnDoc.setG(doc.get("g").toString());
                if (null != doc.get("v")) {
                    mvnDoc.setLatestVersion(doc.get("v").toString());
                } else {
                    mvnDoc.setLatestVersion(doc.get("latestVersion").toString());
                }
                if (null != doc.get("repositoryId")) {
                    mvnDoc.setRepositoryId(doc.get("repositoryId").toString());
                } else {
                    mvnDoc.setRepositoryId("");
                }
                mvnDoc.setP(doc.get("p").toString());
                mvnDocs.add(mvnDoc);
            }
        } catch (JsonProcessingException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    private void downloadMaven(String filepath, String filename) {
        filepath = filepath + "/" + filename;
        try {
            FileUtils.copyURLToFile(
                    new URL("https://search.maven.org/remotecontent?filepath=" + filepath),
                    new File(mavenpath + File.separator + filename),
                    6000,
                    6000);
        } catch (MalformedURLException ex) {
            LOGGER.error(ex.getMessage());
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
}
