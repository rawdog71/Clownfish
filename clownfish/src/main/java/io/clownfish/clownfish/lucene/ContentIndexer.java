/*
 * Copyright 2019 rawdog.
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

import io.clownfish.clownfish.beans.AttributContentList;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import java.io.IOException;
import org.apache.lucene.index.IndexWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.CorruptIndexException;

/**
 *
 * @author rawdog
 */
public class ContentIndexer implements Runnable {
    private final AttributContentList attributContentList;
    private final IndexWriter writer;

    public ContentIndexer(IndexWriter writer, AttributContentList attributContentList) throws IOException {
        this.writer = writer;
        this.attributContentList = attributContentList;
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
    private Document getDocument(CfAttributcontent attributcontent) throws IOException {
        if (attributcontent.getClasscontentref().getClassref().isSearchrelevant()) {
            Document document = new Document();
            document.add(new StoredField(LuceneConstants.CONTENT_TYPE, "Clownfish/Content"));
            document.add(new StoredField(LuceneConstants.ID, attributcontent.getId()));
            document.add(new StoredField(LuceneConstants.CLASSCONTENT_REF, attributcontent.getClasscontentref().getId()));
            switch (attributcontent.getAttributref().getAttributetype().getName()) {
                case "string":
                    if (null != attributcontent.getContentString()) {
                        document.add(new TextField(LuceneConstants.CONTENT_STRING, attributcontent.getContentString(), Field.Store.YES));
                    }
                    break;
                case "text":
                    if (null != attributcontent.getContentText()) {
                        document.add(new TextField(LuceneConstants.CONTENT_TEXT, attributcontent.getContentText(), Field.Store.YES));
                    }
                    break;
                case "htmltext":
                    if (null != attributcontent.getContentText()) {
                        document.add(new TextField(LuceneConstants.CONTENT_TEXT, attributcontent.getContentText(), Field.Store.YES));
                    }
                    break;
                case "markdown":
                    if (null != attributcontent.getContentText()) {
                        document.add(new TextField(LuceneConstants.CONTENT_TEXT, attributcontent.getContentText(), Field.Store.YES));
                    }
                    break;
            }
            return document;
        } else {
            return null;
        }
    }

    private void indexAttributContent(CfAttributcontent attributcontent) throws IOException {
        if (attributcontent.getAttributref().getAttributetype().isSearchrelevant()) {
            Document document = getDocument(attributcontent);
            if (null != document) {
                //System.out.println("Indexing " + attributcontent.getId());
                writer.addDocument(document);
            }
        }
    }

    public long createIndex() throws IOException {
        for (CfAttributcontent attributcontent : attributContentList.getAttributcontentlist()) {
            indexAttributContent(attributcontent);
        }
        //writer.commit();
        return writer.numRamDocs();
    }

    @Override
    public void run() {
        try {
            long startTime = System.currentTimeMillis();
            createIndex();
            //writer.commit();
            long endTime = System.currentTimeMillis();
            System.out.println("Index Time: " + (endTime - startTime) + "ms");
        } catch (IOException ex) {
            Logger.getLogger(ContentIndexer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
