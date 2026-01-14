/*
 * Copyright 2026 SulzbachR.
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
package io.clownfish.clownfish;

import io.clownfish.clownfish.beans.PropertyList;
import io.clownfish.clownfish.lucene.AssetIndexer;
import io.clownfish.clownfish.lucene.ContentIndexer;
import io.clownfish.clownfish.lucene.DatabasetableIndexer;
import io.clownfish.clownfish.lucene.IndexService;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import io.clownfish.clownfish.serviceinterface.CfSearchdatabaseService;
import io.clownfish.clownfish.utils.DatabaseUtil;
import io.clownfish.clownfish.utils.FolderUtil;
import java.io.IOException;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 *
 * @author SulzbachR
 */
@Component
public class SearchindexInitializer {
    @Autowired private IndexService indexService;
    @Autowired private FolderUtil folderUtil;
    @Autowired @Getter @Setter CfAttributcontentService cfattributcontentService;
    @Autowired @Getter @Setter CfAssetService cfassetService;
    @Autowired CfSearchdatabaseService cfsearchdatabaseService;
    @Autowired CfDatasourceService cfdatasourceService;
    @Autowired DatabaseUtil databaseUtil;
    @Autowired PropertyList propertylist;
    
    private @Getter @Setter ContentIndexer contentIndexer;
    private @Getter @Setter AssetIndexer assetIndexer;
    private @Getter @Setter DatabasetableIndexer databasetableIndexer;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(SearchindexInitializer.class);

    @EventListener(ApplicationReadyEvent.class)
    public void startIndexing() throws IOException {
        indexService.init();
        
        if ((null != folderUtil.getIndex_folder()) && (!folderUtil.getIndex_folder().isEmpty())) {
            // Call a parallel thread to index the content in Lucene
            if (null == contentIndexer) {
                contentIndexer = new ContentIndexer(cfattributcontentService, indexService);
            }
            Thread contentindexer_thread = new Thread(contentIndexer);
            contentindexer_thread.start();
            LOGGER.info("CONTENTINDEXER RUN");
            
            if (null == assetIndexer) {
                assetIndexer = new AssetIndexer(cfassetService, indexService, propertylist);
            }
            Thread assetindexer_thread = new Thread(assetIndexer);
            assetindexer_thread.start();
            LOGGER.info("ASSETINDEXER RUN");

            if (null == databasetableIndexer) {
                databasetableIndexer = new DatabasetableIndexer(cfsearchdatabaseService, cfdatasourceService, databaseUtil, indexService);
            }
            Thread databasetableIndexer_thread = new Thread(databasetableIndexer);
            databasetableIndexer_thread.start();
            LOGGER.info("DATABASETABLEINDEXER RUN");

            indexService.getWriter().commit();
        }
    }
}