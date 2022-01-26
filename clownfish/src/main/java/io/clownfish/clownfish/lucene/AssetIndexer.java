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
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.index.IndexWriter;
import javax.inject.Named;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

/**
 *
 * @author sulzbachr
 */
@Named("assetindexerservice")
@Scope("singleton")
@Component
public class AssetIndexer implements Runnable {
    List<CfAsset> assetList;
    private final IndexWriter writer;
    private final Parser parser;
    BodyContentHandler handler;
    HashMap<String, String> metamap;
    private final CfAssetService cfassetService;
    private static Map<String, String> propertymap = null;
    private final PropertyList propertylist;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(AssetIndexer.class);

    public AssetIndexer(CfAssetService cfassetService, IndexService indexService, PropertyList propertylist) throws IOException {
        this.cfassetService = cfassetService;
        this.writer = indexService.getWriter();
        this.propertylist = propertylist;
        
        parser = new AutoDetectParser();
        metamap = new HashMap<>();
    }

    public void close() throws CorruptIndexException, IOException {
        writer.close();
    }

    
    /*
        getDocument makes the IndexDocument for an asset with following fields:
        id
        name
        description (if available)
        content-type
        content (if asset type has content)
        author
        date
    */
    private Document getDocument(CfAsset assetcontent) throws IOException {
        if (propertymap == null) {
            // read all System Properties of the property table
            propertymap = propertylist.fillPropertyMap();
        }
        String media_folder = propertymap.get("folder_media");
        Document document = new Document();
        document.add(new StoredField(LuceneConstants.ID, assetcontent.getId()));
        document.add(new StoredField(LuceneConstants.ASSET_NAME, assetcontent.getName()));
        if (null != assetcontent.getDescription()) {
            document.add(new StoredField(LuceneConstants.ASSET_DESCRIPTION, assetcontent.getDescription()));
        }
        handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        metamap.clear();
         
        try (FileInputStream inputstream = new FileInputStream(media_folder + File.separator + assetcontent.getName())) {
            ParseContext context = new ParseContext();
            parser.parse(inputstream, handler, metadata, context);
            //System.out.println(handler.toString());
        } catch (SAXException | TikaException ex) {
            LOGGER.error(ex.getMessage());
        }

        //getting the list of all meta data elements 
        String[] metadataNames = metadata.names();
        for(String name : metadataNames) {		        
            //System.out.println(name + ": " + metadata.get(name));
            metamap.put(name, metadata.get(name));
        }

        if (metamap.containsKey("Author")) {
            document.add(new StoredField(LuceneConstants.ASSET_AUTHOR, metamap.get("Author")));
        }
        if (metamap.containsKey("date")) {
            document.add(new StoredField(LuceneConstants.ASSET_DATE, metamap.get("date")));
        }
        document.add(new StoredField(LuceneConstants.CONTENT_TYPE, metamap.get("Content-Type")));
        switch (metamap.get("Content-Type")) {
            case "application/xml":
                document.add(new TextField(LuceneConstants.ASSET_TEXT, handler.toString(), Field.Store.YES));
                break;
            case "application/pdf":
                document.add(new TextField(LuceneConstants.ASSET_TEXT, handler.toString(), Field.Store.YES));
                break;
            case "application/msword":
                document.add(new TextField(LuceneConstants.ASSET_TEXT, handler.toString(), Field.Store.YES));
                break;    
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                document.add(new TextField(LuceneConstants.ASSET_TEXT, handler.toString(), Field.Store.YES));
                break;
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                document.add(new TextField(LuceneConstants.ASSET_TEXT, handler.toString(), Field.Store.YES));
                break;
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                document.add(new TextField(LuceneConstants.ASSET_TEXT, handler.toString(), Field.Store.YES));
                break;    
        }
    
        return document;
    }

    public void indexAssetContent(CfAsset assetcontent) throws IOException {
        assetcontent.setIndexed(true);
        cfassetService.edit(assetcontent);
        Document document = getDocument(assetcontent);
        if (null != document) {
            writer.addDocument(document);
        }
    }

    public long createIndex() throws IOException {
        for (CfAsset asset : assetList) {
            indexAssetContent(asset);
        }
        writer.commit();
        writer.forceMerge(10);
        return writer.numRamDocs();
    }
    
    public void removeDocument(CfAsset asset) {
        try {
            Term term = new Term(LuceneConstants.ID, String.valueOf(asset.getId()));
            writer.deleteDocuments(term);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        
    }

    @Override
    public void run() {
        try {
            assetList = cfassetService.findByIndexed(false);
            createIndex();
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
}
