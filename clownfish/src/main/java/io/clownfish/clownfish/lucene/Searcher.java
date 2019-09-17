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

import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.dbentities.CfSitelist;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.serviceinterface.CfSitecontentService;
import io.clownfish.clownfish.serviceinterface.CfSitelistService;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

/**
 *
 * @author sulzbachr
 */
public class Searcher {
    IndexSearcher indexSearcher;
    MultiFieldQueryParser queryParser;
    Query query;
    ArrayList<CfSite> foundSites;
    ArrayList<CfAsset> foundAssets;
    CfSitecontentService sitecontentservice;
    CfSiteService siteservice;
    CfListcontentService sitelistservice;
    CfListService cflistservice;
    CfSitelistService cfsitelistservice;
    CfAssetService cfassetservice;

    public Searcher(String indexDirectoryPath, CfSitecontentService sitecontentservice, CfSiteService siteservice, CfListcontentService sitelistservice, CfListService cflistservice, CfSitelistService cfsitelistservice, CfAssetService cfassetservice) throws IOException {
        this.sitecontentservice = sitecontentservice;
        this.siteservice = siteservice;
        this.sitelistservice = sitelistservice;
        this.cflistservice = cflistservice;
        this.cfsitelistservice = cfsitelistservice;
        this.cfassetservice = cfassetservice;
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexReader reader = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(reader);
        queryParser = new MultiFieldQueryParser(new String[] {LuceneConstants.CONTENT_TEXT, LuceneConstants.CONTENT_STRING, LuceneConstants.ASSET_NAME, LuceneConstants.ASSET_TEXT, LuceneConstants.ASSET_DESCRIPTION}, new StandardAnalyzer());
        foundSites = new ArrayList<>();
        foundAssets = new ArrayList<>();
    }
    
    public SearchResult search(String searchQuery) throws IOException, ParseException {
        SearchResult searchresult = new SearchResult();
        foundSites.clear();
        foundAssets.clear();
        query = queryParser.parse(searchQuery);
        TopDocs hits = indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = getDocument(scoreDoc);
            String contenttype = doc.get(LuceneConstants.CONTENT_TYPE);
            if (0 == contenttype.compareToIgnoreCase("Clownfish/Content")) {
                long classcontentref = Long.parseLong(doc.get(LuceneConstants.CLASSCONTENT_REF));
                // Search directly in site
                List<CfSitecontent> sitelist = sitecontentservice.findByClasscontentref(classcontentref);
                for (CfSitecontent sitecontent : sitelist) {
                    CfSite foundsite = siteservice.findById(sitecontent.getCfSitecontentPK().getSiteref());
                    if ((!foundSites.contains(foundsite)) && (foundsite.isSearchrelevant())) {
                        foundSites.add(foundsite);
                    }
                }
                // Search in sitelists
                List<CfListcontent> listcontent = sitelistservice.findByClasscontentref(classcontentref);
                for (CfListcontent listcontententry : listcontent) {
                    CfList foundlist = cflistservice.findById(listcontententry.getCfListcontentPK().getListref());
                    List<CfSitelist> foundsitelist = cfsitelistservice.findByListref(foundlist.getId());
                    for (CfSitelist sitelistentry : foundsitelist) {
                        CfSite foundsite = siteservice.findById(sitelistentry.getCfSitelistPK().getSiteref());
                        if ((!foundSites.contains(foundsite)) && (foundsite.isSearchrelevant())) {
                            foundSites.add(foundsite);
                        }
                    }
                }
            } else {
                String assetid = doc.getField(LuceneConstants.ID).stringValue();
                //System.out.println("ASSET-ID: " + assetid);
                CfAsset asset = cfassetservice.findById(Long.parseLong(assetid));
                if (!foundAssets.contains(assetid)) {
                    foundAssets.add(asset);
                }
            }
        }
        searchresult.foundSites = foundSites;
        searchresult.foundAssets = foundAssets;
        return searchresult;
    }
    
    public Document getDocument(ScoreDoc scoreDoc) throws CorruptIndexException, IOException {
        return indexSearcher.doc(scoreDoc.doc);  
    }
}
