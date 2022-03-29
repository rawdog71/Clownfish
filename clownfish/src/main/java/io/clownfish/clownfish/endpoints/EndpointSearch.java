/*
 * Copyright 2022 raine.
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
package io.clownfish.clownfish.endpoints;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.clownfish.clownfish.Clownfish;
import io.clownfish.clownfish.beans.JsonFormParameter;
import io.clownfish.clownfish.beans.PropertyList;
import io.clownfish.clownfish.lucene.LuceneConstants;
import io.clownfish.clownfish.lucene.SearchResult;
import io.clownfish.clownfish.lucene.Searcher;
import io.clownfish.clownfish.serviceinterface.CfSearchhistoryService;
import io.clownfish.clownfish.utils.ClownfishUtil;
import io.clownfish.clownfish.utils.FolderUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import io.clownfish.clownfish.utils.SearchUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

/**
 *
 * @author raine
 */
@RestController
public class EndpointSearch {
    private HttpSession userSession;
    @Autowired private ClownfishUtil clownfishutil;
    @Autowired private FolderUtil folderUtil;
    @Autowired private PropertyUtil propertyUtil;
    @Autowired private SearchUtil searchUtil;
    @Autowired PropertyList propertylist;
    @Autowired Searcher searcher;
    @Autowired private Clownfish clownfish;
    
    @Autowired CfSearchhistoryService cfsearchhistoryService;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(EndpointSearch.class);
    
    @PostConstruct
    public void init() {
        // Init Lucene Search Map
        if (null == clownfish.getSearchcontentmap()) {
            clownfish.setSearchcontentmap(new HashMap<>());
        }
        if (null == clownfish.getSearchassetmap()) {
            clownfish.setSearchassetmap(new HashMap<>());
        }
        if (null == clownfish.getSearchassetmetadatamap()) {
            clownfish.setSearchassetmetadatamap(new HashMap<>());
        }
        if (null == clownfish.getSearchclasscontentmap()) {
            clownfish.setSearchclasscontentmap(new HashMap<>());
        }
        if (null == clownfish.getSearchmetadata()) {
            clownfish.setSearchmetadata(new HashMap<>());
        }
        clownfish.setSearchlimit(propertyUtil.getPropertyInt("lucene_searchlimit", LuceneConstants.MAX_SEARCH));
    }
    
    @PostMapping(path = "/search")
    public void postsearch(@Context HttpServletRequest request, @Context HttpServletResponse response) throws ParseException {
        try {
            userSession = request.getSession();
            String content = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

            Gson gson = new Gson();
            List<JsonFormParameter> map;
            map = (List<JsonFormParameter>) gson.fromJson(content, new TypeToken<List<JsonFormParameter>>() {}.getType());
            
            Map parametermap = clownfishutil.getParametermap(map);
            
            String[] searchexpressions = parametermap.get("searchparam").toString().split(" ");
            searchUtil.updateSearchhistory(searchexpressions);
            
            searcher.setIndexPath(folderUtil.getIndex_folder());
            searcher.setPropertyList(propertylist);
            long startTime = System.currentTimeMillis();
            SearchResult searchresult = searcher.search(parametermap.get("searchparam").toString(), clownfish.getSearchlimit());
            long endTime = System.currentTimeMillis();
            
            LOGGER.info("Search Time :" + (endTime - startTime));
            clownfish.getSearchmetadata().clear();
            clownfish.getSearchmetadata().put("cfSearchQuery", parametermap.get("searchparam").toString());
            clownfish.getSearchmetadata().put("cfSearchTime", String.valueOf(endTime - startTime));
            clownfish.getSearchcontentmap().clear();
            searchresult.getFoundSites().stream().forEach((site) -> {
                clownfish.getSearchcontentmap().put(site.getName(), site);
            });
            clownfish.getSearchassetmap().clear();
            searchresult.getFoundAssets().stream().forEach((asset) -> {
                clownfish.getSearchassetmap().put(asset.getName(), asset);
            });
            clownfish.getSearchassetmetadatamap().clear();
            searchresult.getFoundAssetsMetadata().keySet().stream().forEach((key) -> {
                clownfish.getSearchassetmetadatamap().put(key, searchresult.getFoundAssetsMetadata().get(key));
            });
            clownfish.getSearchclasscontentmap().clear();
            searchresult.getFoundClasscontent().keySet().stream().forEach((key) -> {
                clownfish.getSearchclasscontentmap().put(key, searchresult.getFoundClasscontent().get(key));
            });
            
            String search_site = propertyUtil.getPropertyValue("site_search");
            if (null == search_site) {
                search_site = "searchresult";
            }
            request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, search_site);
            clownfish.universalGet(search_site, request, response);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }    
    }
    
    /**
     * Call of the "search" site
     * Fetches the search site from the system property "site_search" and calls universalGet
     * Instantiates the Searcher class
     * Clears and fills the searchmetadata
     * @param query
     * @param request
     * @param response
     */
    @GetMapping(path = "/search/{query}")
    public void search(@PathVariable("query") String query, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        try {
            String[] searchexpressions = query.split(" ");
            searchUtil.updateSearchhistory(searchexpressions);
            
            searcher.setIndexPath(folderUtil.getIndex_folder());
            long startTime = System.currentTimeMillis();
            SearchResult searchresult = searcher.search(query, clownfish.getSearchlimit());
            long endTime = System.currentTimeMillis();
            
            LOGGER.info("Search Time :" + (endTime - startTime));
            clownfish.getSearchmetadata().clear();
            clownfish.getSearchmetadata().put("cfSearchQuery", query);
            clownfish.getSearchmetadata().put("cfSearchTime", String.valueOf(endTime - startTime));
            clownfish.getSearchcontentmap().clear();
            searchresult.getFoundSites().stream().forEach((site) -> {
                clownfish.getSearchcontentmap().put(site.getName(), site);
            });
            clownfish.getSearchassetmap().clear();
            searchresult.getFoundAssets().stream().forEach((asset) -> {
                clownfish.getSearchassetmap().put(asset.getName(), asset);
            });
            clownfish.getSearchassetmetadatamap().clear();
            searchresult.getFoundAssetsMetadata().keySet().stream().forEach((key) -> {
                clownfish.getSearchassetmetadatamap().put(key, searchresult.getFoundAssetsMetadata().get(key));
            });
            clownfish.getSearchclasscontentmap().clear();
            searchresult.getFoundClasscontent().keySet().stream().forEach((key) -> {
                clownfish.getSearchclasscontentmap().put(key, searchresult.getFoundClasscontent().get(key));
            });
            
            String search_site = propertyUtil.getPropertyValue("site_search");
            if (null == search_site) {
                search_site = "searchresult";
            }
            request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, search_site);
            clownfish.universalGet(search_site, request, response);
        } catch (IOException | ParseException ex) {
            LOGGER.error(ex.getMessage());
            String search_site = propertyUtil.getPropertyValue("site_search");
            if (null == search_site) {
                search_site = "searchresult";
            }
            clownfish.universalGet(search_site, request, response);
        }
    }
}
