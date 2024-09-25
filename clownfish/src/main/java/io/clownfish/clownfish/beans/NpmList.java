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
import io.clownfish.clownfish.npm.NpmPackage;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import javax.faces.event.ActionEvent;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;

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
    private @Getter @Setter HashMap<String, String> npmNewestVersions = null;
    private @Getter @Setter CfNpmSearchresult selectedNpmDoc = null;
    private @Getter @Setter CfNpmVersion selectedNpmVersion = null;
    String npmpath;
    @Value("${server.port:9000}") int serverPort;
    private @Getter @Setter NpmPackage npmpackage = null;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(NpmList.class);

    @PostConstruct
    public void init() {
        LOGGER.info("INIT NPMLIST START");
        npmlist = cfnpmService.findAll();
        npmNewestVersions = new HashMap<>();
        fetchNewestVersions(npmlist, npmNewestVersions);
        packagename = "";
        newContentButtonDisabled = false;
        npmDocs = new ArrayList<>();
        npmVersions = new ArrayList<>();
        npmpath = propertyUtil.getPropertyValue("folder_js");
        LOGGER.info("INIT NPMLIST END");
    }
    
    public void onRefreshAll() {
        npmlist = cfnpmService.findAll();
        fetchNewestVersions(npmlist, npmNewestVersions);
    }
    
    private List<CfNpmVersion> getNpmVersions(String name) {
        List<CfNpmVersion> npmversions = new ArrayList<>();
        StringBuilder s_url = new StringBuilder();
        s_url.append("https://registry.npmjs.org/");
        s_url.append(name);
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(s_url.toString(), String.class);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            Map<String, Object> result = mapper.convertValue(root, new TypeReference<Map<String, Object>>(){});
            
            LinkedHashMap<String, Object> versions = (LinkedHashMap<String, Object>) result.get("versions");
            npmversions.clear();
            
            for (String key : versions.keySet()) {
                CfNpmVersion version = new CfNpmVersion();
                if (!key.contains("-")) {
                    version.setName(key);
                    LinkedHashMap<String, Object> dummyversion = (LinkedHashMap<String, Object>)versions.get(key);
                    version.setVersion((String)dummyversion.get("version"));
                    LinkedHashMap<String, Object> dummydist = (LinkedHashMap<String, Object>)dummyversion.get("dist");
                    version.setTarball((String)dummydist.get("tarball"));
                    npmversions.add(version);
                }
            }
            Collections.sort(npmversions);
            return npmversions;
        } catch (JsonProcessingException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }
    
    public void onSelectNpmDoc(SelectEvent event) {
        selectedNpmDoc = (CfNpmSearchresult) event.getObject();
        npmVersions = getNpmVersions(selectedNpmDoc.getName());
    }
    
    public CfNpmVersion getNewestNpmVersion(CfNpm npm) {
        npmVersions = getNpmVersions(npm.getNpmId());
        return npmVersions.get(0);
    }
    
    public void onSelectNpm(SelectEvent event) {
        selectedNpm = (CfNpm) event.getObject();
        onDetail();
    }
       
    public void onInstall(ActionEvent actionEvent) {
        if (null != selectedNpmVersion) {
            CfNpm npm = new CfNpm();
            npm.setNpmLatestversion(selectedNpmVersion.getVersion());
            npm.setNpmId(selectedNpmDoc.getName());
            npm.setNpmFilename(selectedNpmVersion.getTarball());
            
            CfNpm dummy = cfnpmService.findByNpmIdAndNpmLatestversion(selectedNpmDoc.getName(), selectedNpmVersion.getVersion());
            if (null == dummy) {
                for (CfNpm currentnpm : npmlist) {
                    if (0 == currentnpm.getNpmId().compareToIgnoreCase(selectedNpmDoc.getName())) {
                        deinstall(currentnpm);
                    }
                }
                downloadNpm(npm.getNpmFilename(), npm.getNpmId());
                npm = cfnpmService.create(npm);
                npmlist = cfnpmService.findAll();
                fetchNewestVersions(npmlist, npmNewestVersions);
            }
        }
    }
    
    public void upgrade(CfNpm npm, String newestversion) {
        if (null != npm) {
            CfNpmVersion npmversion = getNewestNpmVersion(npm);
            deinstall(npm);
            npm.setNpmFilename(npmversion.getTarball());
            npm.setNpmLatestversion(newestversion);
            npm.setId(null);
            downloadNpm(npm.getNpmFilename(), npm.getNpmId());
            npm = cfnpmService.create(npm);
            npmlist = cfnpmService.findAll();
            fetchNewestVersions(npmlist, npmNewestVersions);
        }
    }
    
    public void onDeinstall(ActionEvent actionEvent) {
        if (null != selectedNpm) {
            deinstall(selectedNpm);
            npmlist = cfnpmService.findAll();
            fetchNewestVersions(npmlist, npmNewestVersions);
        }
    }
    
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
            filename = filename.replaceAll("[^a-zA-Z0-9//@/-]", "");
            FileUtils.copyURLToFile(
                new URL(downloadfile),
                new File(npmpath + File.separator + filename + File.separator + extractFilename(downloadfile)),
                6000,
                6000);
            
            try (TarArchiveInputStream tararchiveinputstream = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(Files.newInputStream(new File(npmpath + File.separator + filename + File.separator + extractFilename(downloadfile)).toPath()))))) {
                ArchiveEntry archiveentry = null;
                while ((archiveentry = tararchiveinputstream.getNextEntry()) != null) {
                    Path pathEntryOutput = new File(npmpath + File.separator + filename).toPath().resolve(archiveentry.getName().replaceFirst("package/", ""));
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
    
    public void onDetail() {
        String npmid = selectedNpm.getNpmId();
        try {
            StringBuilder s_url = new StringBuilder();
            s_url.append("http://localhost:");
            s_url.append(serverPort);
            s_url.append("/js/");
            s_url.append(npmid.replaceAll("[^a-zA-Z0-9//@/-]", ""));
            s_url.append("/package.json");
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(s_url.toString(), String.class);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            LinkedHashMap<String, Object> result = mapper.convertValue(root, new TypeReference<LinkedHashMap<String, Object>>(){});
            
            npmpackage = new NpmPackage();
            npmpackage.setName(result.get("name").toString());
            try {
                npmpackage.setType(result.get("type").toString());
            } catch (Exception ex) {
                npmpackage.setType("");
            }
            npmpackage.setVersion(result.get("version").toString());
            try {
                npmpackage.setMain(result.get("main").toString());
            } catch (Exception ex) {
                npmpackage.setMain("");
            }
            try {
                npmpackage.setModule(result.get("module").toString());
            } catch (Exception ex) {
                npmpackage.setModule("");
            }
            try {
                npmpackage.setStyle(result.get("style").toString());
            } catch (Exception ex) {
                npmpackage.setStyle("");
            }
            npmpackage.setDescription(result.get("description").toString());
        } catch (JsonProcessingException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    private String extractFilename(String downloadfile) {
        return downloadfile.substring(downloadfile.lastIndexOf("/")+1);
    }
    
    private void fetchNewestVersions(List<CfNpm> npmlist, HashMap<String, String> map) {
        map.clear();
        for (CfNpm npm : npmlist) {
            StringBuilder s_url = new StringBuilder();
            s_url.append("https://registry.npmjs.org/");
            s_url.append(npm.getNpmId());
            
            try {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> response = restTemplate.getForEntity(s_url.toString(), String.class);
            
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                Map<String, Object> result = mapper.convertValue(root, new TypeReference<Map<String, Object>>(){});
            
                LinkedHashMap<String, Object> disttags = (LinkedHashMap<String, Object>) result.get("dist-tags");
                String latestversion = (String) disttags.get("latest");
                
                map.put(npm.getNpmId(), latestversion);
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(NpmList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public String getNewestVersion(String name) {
        return npmNewestVersions.get(name);
    }
    
    public boolean checkVersion(String version, String newestversion) {
        return (0 != version.compareToIgnoreCase(newestversion));
    }
    
    private void deinstall(CfNpm npm) {
        if (null != npm) {
            cfnpmService.delete(npm);
            try {
                FileUtils.deleteDirectory(new File(npmpath + File.separator + npm.getNpmId().replaceAll("[^a-zA-Z0-9//@/-]", "")));
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(NpmList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
