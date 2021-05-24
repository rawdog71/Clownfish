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
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
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
    private static @Getter @Setter Map contentCache;
    final transient Logger LOGGER = LoggerFactory.getLogger(WebServiceTemplateBean.class);

    public WebServiceTemplateBean() {
        contentmap = new HashMap<>();
        if (null == contentCache) {
            contentCache = new HashMap<>();
        }
    }
    
    public Map callService(String url, int seconds) {
        if (contentCache.containsKey(url)) {
            if (DateTime.now().isBefore(((WebserviceCache)contentCache.get(url)).getValiduntil())) {
                return ((WebserviceCache)contentCache.get(url)).getContentmap();
            } else {
                contentmap.putAll(getContent(url));
                ((WebserviceCache)contentCache.get(url)).setContentmap(contentmap);
                ((WebserviceCache)contentCache.get(url)).setValiduntil(DateTime.now().plusSeconds(seconds));
                
                return contentmap;
            }
        } else {
            contentmap.putAll(getContent(url));

            WebserviceCache webservicecache = new WebserviceCache();
            webservicecache.setContentmap(contentmap);
            webservicecache.setValiduntil(DateTime.now().plusSeconds(seconds));
            contentCache.put(url, webservicecache);

            return contentmap;
        }
    }
    
    private Map getContent(String url) {
        try {
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
}
