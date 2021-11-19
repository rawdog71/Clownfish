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

import io.clownfish.clownfish.dbentities.CfJava;
import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.serviceinterface.CfJavaService;
import io.clownfish.clownfish.serviceinterface.CfJavascriptService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import java.io.IOException;
import java.util.List;
import org.apache.lucene.index.IndexWriter;
import javax.inject.Named;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.CorruptIndexException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Named("sourceindexerservice")
@Scope("singleton")
@Component
public class SourceIndexer implements Runnable {
    private final IndexWriter writer;
    List<CfTemplate> templatelist;
    List<CfStylesheet> stylesheetlist;
    List<CfJavascript> javascriptlist;
    List<CfJava> javalist;
    private final CfTemplateService cftemplateService;
    private final CfStylesheetService cfstylesheetService;
    private final CfJavascriptService cfjavascriptService;
    private final CfJavaService cfjavaService;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(SourceIndexer.class);
    
    public SourceIndexer(CfTemplateService cftemplateService, CfStylesheetService cfstylesheetService, CfJavascriptService cfjavascriptService, CfJavaService cfjavaService, IndexService indexService) throws IOException {
        writer = indexService.getWriter();
        this.cftemplateService = cftemplateService;
        this.cfstylesheetService = cfstylesheetService;
        this.cfjavascriptService = cfjavascriptService;
        this.cfjavaService = cfjavaService;
    }

    public void close() throws CorruptIndexException, IOException {
        writer.close();
    }

    /*
        getDocument makes the IndexDocument for a content element with following fields:
        id
        classcontent_ref
        content-type (always Clownfish/Content)
        content
    */
    private Document getDocumentTemplate(CfTemplate template) throws IOException {
        Document document = new Document();
        document.add(new StoredField(LuceneConstants.CONTENT_TYPE, "Clownfish/Source"));
        document.add(new StoredField(LuceneConstants.ID, template.getId()));
        if (null != template.getContent()) {
            document.add(new TextField(LuceneConstants.CONTENT_STRING, template.getContent(), Field.Store.YES));
        }
        return document;
    }
    
    private Document getDocumentCSS(CfStylesheet stylesheet) throws IOException {
        Document document = new Document();
        document.add(new StoredField(LuceneConstants.CONTENT_TYPE, "Clownfish/CSS"));
        document.add(new StoredField(LuceneConstants.ID, stylesheet.getId()));
        if (null != stylesheet.getContent()) {
            document.add(new TextField(LuceneConstants.CONTENT_STRING, stylesheet.getContent(), Field.Store.YES));
        }
        return document;
    }
    
    private Document getDocumentJS(CfJavascript javascript) throws IOException {
        Document document = new Document();
        document.add(new StoredField(LuceneConstants.CONTENT_TYPE, "Clownfish/JS"));
        document.add(new StoredField(LuceneConstants.ID, javascript.getId()));
        if (null != javascript.getContent()) {
            document.add(new TextField(LuceneConstants.CONTENT_STRING, javascript.getContent(), Field.Store.YES));
        }
        return document;
    }

    private Document getDocumentJava(CfJava java) throws IOException {
        Document document = new Document();
        document.add(new StoredField(LuceneConstants.CONTENT_TYPE, "Clownfish/Java"));
        document.add(new StoredField(LuceneConstants.ID, java.getId()));
        if (null != java.getContent()) {
            document.add(new TextField(LuceneConstants.CONTENT_STRING, java.getContent(), Field.Store.YES));
        }
        return document;
    }

    private void indexTemplate(CfTemplate template) throws IOException {
        Document document = getDocumentTemplate(template);
        if (null != document) {
            writer.addDocument(document);
        }
    }
    
    private void indexStylesheet(CfStylesheet stylesheet) throws IOException {
        Document document = getDocumentCSS(stylesheet);
        if (null != document) {
            writer.addDocument(document);
        }
    }
    
    private void indexJavascript(CfJavascript javascript) throws IOException {
        Document document = getDocumentJS(javascript);
        if (null != document) {
            writer.addDocument(document);
        }
    }

    private void indexJava(CfJava java) throws IOException {
        Document document = getDocumentJava(java);
        if (null != document) {
            writer.addDocument(document);
        }
    }

    public long createIndex() throws IOException {
        for (CfTemplate template : templatelist) {
            indexTemplate(template);
        }
        for (CfStylesheet stylesheet : stylesheetlist) {
            indexStylesheet(stylesheet);
        }
        for (CfJavascript javascript : javascriptlist) {
            indexJavascript(javascript);
        }
        for (CfJava java : javalist) {
            indexJava(java);
        }
        return writer.numRamDocs();
    }

    @Override
    public void run() {
        try {
            templatelist = cftemplateService.findAll();
            stylesheetlist = cfstylesheetService.findAll();
            javascriptlist = cfjavascriptService.findAll();
            javalist = cfjavaService.findAll();
            createIndex();
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
}
