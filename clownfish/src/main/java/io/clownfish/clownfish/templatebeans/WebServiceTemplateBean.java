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
package io.clownfish.clownfish.templatebeans;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.clownfish.clownfish.datamodels.WebserviceCache;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author raine
 */
@Scope("singleton")
@Component
public class WebServiceTemplateBean implements Serializable {
    private transient @Getter @Setter Map contentmap;
    private transient @Getter @Setter List contentlist;
    private static @Getter @Setter Map contentCache;
    private static @Getter @Setter String contentCacheString;
    final transient Logger LOGGER = LoggerFactory.getLogger(WebServiceTemplateBean.class);

    public WebServiceTemplateBean() {
        contentmap = new HashMap<>();
        contentlist = new ArrayList();
        if (null == contentCache) {
            contentCache = new HashMap<>();
        }
    }
    
    public Map callServiceMap(String url, int seconds) {
        if (contentCache.containsKey(url)) {
            if (DateTime.now().isBefore(((WebserviceCache)contentCache.get(url)).getValiduntil())) {
                return ((WebserviceCache)contentCache.get(url)).getContentmap();
            } else {
                contentmap.putAll(getContentMap(url));
                ((WebserviceCache)contentCache.get(url)).setContentmap(contentmap);
                ((WebserviceCache)contentCache.get(url)).setValiduntil(DateTime.now().plusSeconds(seconds));
                
                return contentmap;
            }
        } else {
            contentmap.putAll(getContentMap(url));

            WebserviceCache webservicecache = new WebserviceCache();
            webservicecache.setContentmap(contentmap);
            webservicecache.setValiduntil(DateTime.now().plusSeconds(seconds));
            contentCache.put(url, webservicecache);

            return contentmap;
        }
    }
    
    public Map callServiceMap(String url, int seconds, String user, String password) {
        if (contentCache.containsKey(url)) {
            if (DateTime.now().isBefore(((WebserviceCache)contentCache.get(url)).getValiduntil())) {
                return ((WebserviceCache)contentCache.get(url)).getContentmap();
            } else {
                contentmap.putAll(getContentMap(url, user, password));
                ((WebserviceCache)contentCache.get(url)).setContentmap(contentmap);
                ((WebserviceCache)contentCache.get(url)).setValiduntil(DateTime.now().plusSeconds(seconds));
                
                return contentmap;
            }
        } else {
            contentmap.putAll(getContentMap(url, user, password));

            WebserviceCache webservicecache = new WebserviceCache();
            webservicecache.setContentmap(contentmap);
            webservicecache.setValiduntil(DateTime.now().plusSeconds(seconds));
            contentCache.put(url, webservicecache);

            return contentmap;
        }
    }
    
  
    
    public List callServiceList(String url, int seconds) {
        if (contentCache.containsKey(url)) {
            if (DateTime.now().isBefore(((WebserviceCache)contentCache.get(url)).getValiduntil())) {
                return ((WebserviceCache)contentCache.get(url)).getContentlist();
            } else {
                contentlist.addAll(getContentList(url));
                ((WebserviceCache)contentCache.get(url)).setContentlist(contentlist);
                ((WebserviceCache)contentCache.get(url)).setValiduntil(DateTime.now().plusSeconds(seconds));
                
                return contentlist;
            }
        } else {
            contentlist.addAll(getContentList(url));

            WebserviceCache webservicecache = new WebserviceCache();
            webservicecache.setContentlist(contentlist);
            webservicecache.setValiduntil(DateTime.now().plusSeconds(seconds));
            contentCache.put(url, webservicecache);

            return contentlist;
        }
    }
    
    public List callServiceList(String url, int seconds, String user, String password) {
        if (contentCache.containsKey(url)) {
            if (DateTime.now().isBefore(((WebserviceCache)contentCache.get(url)).getValiduntil())) {
                return ((WebserviceCache)contentCache.get(url)).getContentlist();
            } else {
                contentlist.addAll(getContentList(url, user, password));
                ((WebserviceCache)contentCache.get(url)).setContentlist(contentlist);
                ((WebserviceCache)contentCache.get(url)).setValiduntil(DateTime.now().plusSeconds(seconds));
                
                return contentlist;
            }
        } else {
            contentlist.addAll(getContentList(url, user, password));

            WebserviceCache webservicecache = new WebserviceCache();
            webservicecache.setContentlist(contentlist);
            webservicecache.setValiduntil(DateTime.now().plusSeconds(seconds));
            contentCache.put(url, webservicecache);

            return contentlist;
        }
    }
    
    private Map getContentMap(String url) {
        try {
            url = url.replace("+", "%2B");
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            Map<String, Object> result = mapper.convertValue(root, new TypeReference<Map<String, Object>>(){});
            return result;
        } catch (JsonProcessingException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }
    
    private Map getContentMap(String url, String user, String password) {
        try {
            url = url.replace("+", "%2B");
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getInterceptors().add(
                new BasicAuthorizationInterceptor(user, password));
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            Map<String, Object> result = mapper.convertValue(root, new TypeReference<Map<String, Object>>(){});
            return result;
        } catch (JsonProcessingException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }
    
    private List getContentList(String url) {
        try {
            url = url.replace("+", "%2B");
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            List<Map<String, Object>> result = mapper.convertValue(root, new TypeReference<List<Map<String, Object>>>(){});
            return result;
        } catch (JsonProcessingException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }
    
    private List getContentList(String url, String user, String password) {
        try {
            url = url.replace("+", "%2B");
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getInterceptors().add(
                new BasicAuthorizationInterceptor(user, password));
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            List<Map<String, Object>> result = mapper.convertValue(root, new TypeReference<List<Map<String, Object>>>(){});
            return result;
        } catch (JsonProcessingException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }
}
