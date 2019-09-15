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

import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.serviceinterface.CfSitecontentService;
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
    CfSitecontentService sitecontentservice;
    CfSiteService siteservice;

    public Searcher(String indexDirectoryPath, CfSitecontentService sitecontentservice, CfSiteService siteservice) throws IOException {
        this.sitecontentservice = sitecontentservice;
        this.siteservice = siteservice;
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexReader reader = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(reader);
        //queryParser = new QueryParser(LuceneConstants.CONTENT_TEXT, new StandardAnalyzer());
        queryParser = new MultiFieldQueryParser(new String[] {LuceneConstants.CONTENT_TEXT, LuceneConstants.CONTENT_STRING}, new StandardAnalyzer());
        foundSites = new ArrayList<>();
    }
    
    public List<CfSite> search(String searchQuery) throws IOException, ParseException {
        foundSites.clear();
        query = queryParser.parse(searchQuery);
        TopDocs hits = indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
        for(ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = getDocument(scoreDoc);
            long classcontentref = Long.parseLong(doc.get(LuceneConstants.CLASSCONTENT_REF));
            List<CfSitecontent> sitelist = sitecontentservice.findByClasscontentref(classcontentref);
            for (CfSitecontent sitecontent : sitelist) {
                CfSite foundsite = siteservice.findById(sitecontent.getCfSitecontentPK().getSiteref());
                if ((!foundSites.contains(foundsite)) && (foundsite.isSearchrelevant())) {
                    foundSites.add(foundsite);
                }
            }
        }
        return foundSites;
    }
    
    public Document getDocument(ScoreDoc scoreDoc) throws CorruptIndexException, IOException {
        return indexSearcher.doc(scoreDoc.doc);  
    }
}
