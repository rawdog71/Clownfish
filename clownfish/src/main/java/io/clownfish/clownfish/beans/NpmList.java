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
import io.clownfish.clownfish.dbentities.CfNpm;
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
import io.clownfish.clownfish.npm.CfNpmSearchresult;
import io.clownfish.clownfish.npm.CfNpmVersion;
import io.clownfish.clownfish.serviceinterface.CfNpmService;
import io.clownfish.clownfish.utils.PropertyUtil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import javax.faces.event.ActionEvent;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author sulzbachr
 */
@Named("npmList")
@Scope("singleton")
@Component
public class NpmList implements Serializable {
    @Autowired transient CfNpmService cfnpmService;
    @Autowired transient PropertyUtil propertyUtil;
    
    private transient @Getter @Setter List<CfNpm> npmlist = null;
    private @Getter @Setter CfNpm selectedNpm = null;
    private @Getter @Setter String packagename;
    private @Getter @Setter boolean newContentButtonDisabled = false;
    private @Getter @Setter List<CfNpmSearchresult> npmDocs = null;
    private @Getter @Setter List<CfNpmVersion> npmVersions = null;
    private @Getter @Setter CfNpmSearchresult selectedNpmDoc = null;
    private @Getter @Setter CfNpmVersion selectedNpmVersion = null;
    String npmpath;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(NpmList.class);

    @PostConstruct
    public void init() {
        LOGGER.info("INIT NPMLIST START");
        npmlist = cfnpmService.findAll();
        packagename = "";
        newContentButtonDisabled = false;
        npmDocs = new ArrayList<>();
        npmVersions = new ArrayList<>();
        npmpath = propertyUtil.getPropertyValue("folder_js");
        LOGGER.info("INIT NPMLIST END");
    }
    
    public void onRefreshAll() {
        npmlist = cfnpmService.findAll();
    }
    
    /**
     * Selects an external datasource
     * @param event
     */
    public void onSelectNpmDoc(SelectEvent event) {
        selectedNpmDoc = (CfNpmSearchresult) event.getObject();
        
        StringBuilder s_url = new StringBuilder();
        s_url.append("https://registry.npmjs.org/");
        s_url.append(selectedNpmDoc.getName());
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(s_url.toString(), String.class);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            Map<String, Object> result = mapper.convertValue(root, new TypeReference<Map<String, Object>>(){});
            
            LinkedHashMap<String, Object> versions = (LinkedHashMap<String, Object>) result.get("versions");
            npmVersions.clear();
            
            for (String key : versions.keySet()) {
                CfNpmVersion version = new CfNpmVersion();
                if (!key.contains("-")) {
                    version.setName(key);
                    LinkedHashMap<String, Object> dummyversion = (LinkedHashMap<String, Object>)versions.get(key);
                    version.setVersion((String)dummyversion.get("version"));
                    LinkedHashMap<String, Object> dummydist = (LinkedHashMap<String, Object>)dummyversion.get("dist");
                    version.setTarball((String)dummydist.get("tarball"));
                    npmVersions.add(version);
                }
            }
            Collections.sort(npmVersions);
        } catch (JsonProcessingException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    /**
     * Selects an external datasource
     * @param event
     */
    public void onSelectNpm(SelectEvent event) {
        selectedNpm = (CfNpm) event.getObject();
    }
       
    public void onInstall(ActionEvent actionEvent) {
        if (null != selectedNpmVersion) {
            CfNpm npm = new CfNpm();
            npm.setNpmLatestversion(selectedNpmVersion.getVersion());
            npm.setNpmId(selectedNpmDoc.getName());
            npm.setNpmFilename(selectedNpmVersion.getTarball());
            
            CfNpm dummy = cfnpmService.findByNpmIdAndNpmLatestversion(selectedNpmDoc.getName(), selectedNpmVersion.getVersion());
            if (null == dummy) {
                downloadNpm(npm.getNpmFilename(), npm.getNpmId());
                npm = cfnpmService.create(npm);
                npmlist = cfnpmService.findAll();
            }
        }
    }
    
    /**
     * Deletes an external datasource
     * @param actionEvent
     */
    public void onDeinstall(ActionEvent actionEvent) {
        if (null != selectedNpm) {
            cfnpmService.delete(selectedNpm);
            try {
                FileUtils.deleteDirectory(new File(npmpath + File.separator + selectedNpm.getNpmId().replaceAll("[^a-zA-Z0-9//]", "")));
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(NpmList.class.getName()).log(Level.SEVERE, null, ex);
            }
            //new File(npmpath + File.separator + extractFilename(selectedNpm.getNpmId())).delete();
            npmlist = cfnpmService.findAll();
        }
    }
    
    /**
     * Deletes an external datasource
     * @param actionEvent
     */
    public void onSearch(ActionEvent actionEvent) {
        selectedNpmDoc = null;
        StringBuilder s_url = new StringBuilder();
        s_url.append("https://www.npmjs.com/search/suggestions?q=");
        s_url.append(packagename);
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(s_url.toString(), String.class);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            List<LinkedHashMap<String, Object>> result = mapper.convertValue(root, new TypeReference<List<LinkedHashMap<String, Object>>>(){});
            npmDocs.clear();

            for (Map<String, Object> doc : result) {
                CfNpmSearchresult searchresult = new CfNpmSearchresult();
                searchresult.setName(doc.get("name").toString());
                searchresult.setVersion(doc.get("version").toString());
                searchresult.setDate(doc.get("date").toString());
                searchresult.setDescription(doc.get("description").toString());
                searchresult.setScope(doc.get("scope").toString());
                npmDocs.add(searchresult);
            }
        } catch (JsonProcessingException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    private void downloadNpm(String downloadfile, String filename) {
        try {
            filename = filename.replaceAll("[^a-zA-Z0-9//]", "");
            FileUtils.copyURLToFile(
                new URL(downloadfile),
                new File(npmpath + File.separator + filename + File.separator + extractFilename(downloadfile)),
                6000,
                6000);
            
            try (TarArchiveInputStream tararchiveinputstream = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(Files.newInputStream(new File(npmpath + File.separator + filename + File.separator + extractFilename(downloadfile)).toPath()))))) {
                ArchiveEntry archiveentry = null;
                while ((archiveentry = tararchiveinputstream.getNextEntry()) != null) {
                    Path pathEntryOutput = new File(npmpath + File.separator + filename).toPath().resolve(archiveentry.getName());
                    if ( archiveentry.isDirectory() ) {
                        if(!Files.exists( pathEntryOutput))
                            Files.createDirectory( pathEntryOutput );
                    } else {
                        if (!Files.exists(pathEntryOutput.getParent())) {
                            Files.createDirectories(pathEntryOutput.getParent());
                        }
                        Files.copy(tararchiveinputstream, pathEntryOutput);
                    }
                }
            }
        } catch (MalformedURLException ex) {
            LOGGER.error(ex.getMessage());
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    private String extractFilename(String downloadfile) {
        return downloadfile.substring(downloadfile.lastIndexOf("/")+1);
    }
    
}
