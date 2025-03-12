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
package io.clownfish.clownfish.lucene;

import io.clownfish.clownfish.beans.PropertyList;
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetkeyword;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
import io.clownfish.clownfish.dbentities.CfJava;
import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfKeyword;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.serviceinterface.CfAssetKeywordService;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentKeywordService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfJavaService;
import io.clownfish.clownfish.serviceinterface.CfJavascriptService;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.serviceinterface.CfSitecontentService;
import io.clownfish.clownfish.serviceinterface.CfSitelistService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.utils.AssetUtil;
import io.clownfish.clownfish.utils.ClassUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Scope("singleton")
@Component
public class Searcher implements Serializable {
    IndexSearcher indexSearcher;
    MultiFieldQueryParser queryParser;
    Query query;
    ArrayList<CfSite> foundSites;
    ArrayList<CfAsset> foundAssets;
    ArrayList<CfJava> foundJavas;
    ArrayList<CfJavascript> foundJavascripts;
    ArrayList<CfStylesheet> foundStylesheets;
    ArrayList<CfTemplate> foundTemplates;
    AssetUtil assetutil;
    PropertyUtil propertyUtil;
    boolean getAssetMetadata;
    HashMap<String, HashMap> foundAssetMetadata;
    HashMap<String, String> foundClasscontent;
    @Autowired CfSitecontentService sitecontentservice;
    @Autowired CfSiteService siteservice;
    @Autowired CfListcontentService sitelistservice;
    @Autowired CfListService cflistservice;
    @Autowired CfSitelistService cfsitelistservice;
    @Autowired CfAssetService cfassetservice;
    @Autowired CfClassService cfclassservice;
    @Autowired CfClasscontentService cfclasscontentservice;
    @Autowired CfTemplateService cftemplateservice;
    @Autowired CfStylesheetService cfstylesheetservice;
    @Autowired CfJavascriptService cfjavascriptservice;
    @Autowired CfJavaService cfjavaservice;
    @Autowired ClassUtil classutil;
    @Autowired CfKeywordService cfkeywordservice;
    @Autowired CfClasscontentKeywordService cfclasscontentkeywordservice;
    @Autowired CfAssetKeywordService cfassetkeywordservice;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(Searcher.class);

    public Searcher() {
        foundSites = new ArrayList<>();
        foundAssets = new ArrayList<>();
        foundClasscontent = new HashMap<>();
        foundAssetMetadata = new HashMap<>();
        foundJavas = new ArrayList<>();
        foundJavascripts = new ArrayList<>();
        foundStylesheets = new ArrayList<>();
        foundTemplates = new ArrayList<>();
    }
    
    public void setPropertyList(PropertyList propertylist) {
        assetutil = new AssetUtil(propertylist);
        if (null == propertyUtil) {
            propertyUtil = new PropertyUtil(propertylist);
        }
        getAssetMetadata = propertyUtil.getPropertyBoolean("info_assetmetadata", false);
    }
   
    public void setIndexPath(String indexDirectoryPath) {
        try {
            Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
            IndexReader reader = DirectoryReader.open(indexDirectory);
            indexSearcher = new IndexSearcher(reader);
            queryParser = new MultiFieldQueryParser(new String[] {LuceneConstants.CONTENT_TEXT, LuceneConstants.CONTENT_STRING, LuceneConstants.ASSET_NAME, LuceneConstants.ASSET_TEXT, LuceneConstants.ASSET_DESCRIPTION}, new StandardAnalyzer());
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    public SearchResult search(String searchQuery, int searchlimit) throws IOException, ParseException {
        SearchResult searchresult = new SearchResult();
        if (!searchQuery.isBlank()) {
            foundSites.clear();
            foundAssets.clear();
            foundJavas.clear();
            foundJavascripts.clear();
            foundStylesheets.clear();
            foundTemplates.clear();
            query = queryParser.parse(searchQuery);
            TopDocs hits = indexSearcher.search(query, searchlimit);
            foundClasscontent.clear();
            foundAssetMetadata.clear();
            HashMap searchclasscontentmap = new HashMap<>();       
            for (ScoreDoc scoreDoc : hits.scoreDocs) {
                Document doc = getDocument(scoreDoc);
                String contenttype = doc.get(LuceneConstants.CONTENT_TYPE);
                switch (contenttype) {
                    case "Clownfish/Content":
                        long classcontentref = Long.parseLong(doc.get(LuceneConstants.CLASSCONTENT_REF));
                        // Search directly in site
                        List<CfSitecontent> sitelist = sitecontentservice.findByClasscontentref(classcontentref);
                        sitelist.stream().map((sitecontent) -> siteservice.findById(sitecontent.getCfSitecontentPK().getSiteref())).filter((foundsite) -> ((!foundSites.contains(foundsite)) && (foundsite.isSearchrelevant()))).forEach((foundsite) -> {
                            foundSites.add(foundsite);
                        });
                        // Search in sitelists
                        List<CfListcontent> listcontent = sitelistservice.findByClasscontentref(classcontentref);
                        listcontent.stream().map((listcontententry) -> cflistservice.findById(listcontententry.getCfListcontentPK().getListref())).map((foundlist) -> cfsitelistservice.findByListref(foundlist.getId())).forEach((foundsitelist) -> {
                            foundsitelist.stream().map((sitelistentry) -> siteservice.findById(sitelistentry.getCfSitelistPK().getSiteref())).filter((foundsite) -> ((!foundSites.contains(foundsite)) && (foundsite.isSearchrelevant()))).forEach((foundsite) -> {
                                foundSites.add(foundsite);
                            });
                        });                
                        addClasscontentMap(classcontentref, searchclasscontentmap);
                        break;
                    case "Clownfish/Source":
                        long templateref = Long.parseLong(doc.get(LuceneConstants.ID));
                        CfTemplate template = cftemplateservice.findById(templateref);
                        foundTemplates.add(template);
                        break;
                    case "Clownfish/CSS":
                        long stylesheetref = Long.parseLong(doc.get(LuceneConstants.ID));
                        CfStylesheet stylesheet = cfstylesheetservice.findById(stylesheetref);
                        foundStylesheets.add(stylesheet);
                        break;
                    case "Clownfish/JS":
                        long javascriptref = Long.parseLong(doc.get(LuceneConstants.ID));
                        CfJavascript javascript = cfjavascriptservice.findById(javascriptref);
                        foundJavascripts.add(javascript);
                        break;
                    case "Clownfish/Java":
                        long javaref = Long.parseLong(doc.get(LuceneConstants.ID));
                        CfJava java = cfjavaservice.findById(javaref);
                        if (!foundJavas.contains(java)) {
                            foundJavas.add(java);
                        }
                        break;
                    default:
                        try {
                            String assetid = doc.getField(LuceneConstants.ID).stringValue();
                            CfAsset asset = cfassetservice.findById(Long.parseLong(assetid));
                            if (!foundAssets.contains(asset)) {
                                foundAssets.add(asset);
                                if (getAssetMetadata) {
                                    HashMap<String, String> metamap = new HashMap<>();
                                    assetutil.getMetadata(asset, metamap);
                                    foundAssetMetadata.put(asset.getName(), metamap);
                                }
                            }
                        } catch (IOException | NumberFormatException ex) {
                            LOGGER.warn(ex.getMessage());
                        }
                }
            }

            String[] searchtermlist = searchQuery.split(" ");
            for (String searchterm : searchtermlist) {
                try {
                    CfKeyword keyword = cfkeywordservice.findByName(searchterm);
                    // Search in classcontent associations
                    List<CfClasscontentkeyword> classcontentlist = cfclasscontentkeywordservice.findByKeywordRef(keyword.getId());
                    classcontentlist.stream().forEach((cck) -> {
                        addClasscontentMap(cck.getCfClasscontentkeywordPK().getClasscontentref(), searchclasscontentmap);
                    });

                    // Search in asset associations
                    List<CfAssetkeyword> assetlist = cfassetkeywordservice.findByKeywordRef(keyword.getId());
                    assetlist.stream().map((ask) -> cfassetservice.findById(ask.getCfAssetkeywordPK().getAssetref())).filter((asset) -> (!foundAssets.contains(asset))).forEach((asset) -> {
                        foundAssets.add(asset);
                        if (getAssetMetadata) {
                            try {
                                HashMap<String, String> metamap = new HashMap<>();
                                assetutil.getMetadata(asset, metamap);
                                foundAssetMetadata.put(asset.getName(), metamap);
                            } catch (IOException ex) {
                                LOGGER.error(ex.getMessage());
                            }
                        }
                    });

                } catch (Exception ex) {
                    // Is not a keyword...do nothing at all
                }
            }

            searchresult.foundSites = foundSites;
            searchresult.foundAssets = foundAssets;
            searchresult.foundClasscontent = searchclasscontentmap;
            searchresult.foundAssetsMetadata = foundAssetMetadata;
            searchresult.foundJavas = foundJavas;
            searchresult.foundJavascripts = foundJavascripts;
            searchresult.foundTemplates = foundTemplates;
            searchresult.foundStylesheets = foundStylesheets;
        }
        
        return searchresult;
    }
    
    public Document getDocument(ScoreDoc scoreDoc) throws CorruptIndexException, IOException {
        return indexSearcher.doc(scoreDoc.doc);  
    }
    
    private void addClasscontentMap(long classcontentref, HashMap searchclasscontentmap) {
        // Search in classes and put it via template to the output
        try {
            CfClasscontent findclasscontent = cfclasscontentservice.findById(classcontentref);
            CfClass findclass = cfclassservice.findById(findclasscontent.getClassref().getId());

            if (findclass.isSearchrelevant()) {
                Map attributmap = classutil.getattributmap(findclasscontent);
                if (searchclasscontentmap.containsKey(findclass.getName())) {
                    ArrayList searchclassarray = (ArrayList) searchclasscontentmap.get(findclass.getName());
                    if (!searchclassarray.contains(attributmap)) {
                        searchclassarray.add(attributmap);
                        searchclasscontentmap.put(findclass.getName(), searchclassarray);
                    }
                } else {
                    ArrayList searchclassarray = new ArrayList<>();
                    searchclassarray.add(attributmap);
                    searchclasscontentmap.put(findclass.getName(), searchclassarray);
                }
            }
        } catch (Exception ex) {
            LOGGER.warn("LUCENE SEARCHER CONTENT NOT FOUND: " + classcontentref);
        }
    }
}
