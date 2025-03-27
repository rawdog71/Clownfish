/*
 * Copyright 2025 SulzbachR.
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
package io.clownfish.clownfish.beans;

import io.clownfish.clownfish.datamodels.SearchEntry;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.lucene.SearchResult;
import io.clownfish.clownfish.lucene.Searcher;
import io.clownfish.clownfish.utils.FolderUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author SulzbachR
 */
@Scope("session")
@Named("searchBean")
public class SearchBean implements Serializable {
    private @Getter @Setter String searchphrase;
    private @Getter @Setter SearchResult searchresult;
    private @Getter @Setter ArrayList<SearchEntry> searchlist = new ArrayList<>();
    @Autowired Searcher searcher;
    @Autowired private FolderUtil folderUtil;
    
    public void onSearch(ActionEvent actionEvent) {
        try {
            if (!searchphrase.isBlank()) {
                System.out.println("Suche");
                
                searcher.setIndexPath(folderUtil.getIndex_folder());

                searchresult = searcher.search(searchphrase, 100);
                searchlist.clear();
                
                for(CfTemplate template : searchresult.getFoundTemplates()) {
                    SearchEntry entry = new SearchEntry();
                    entry.setId(template.getId());
                    entry.setName(template.getName());
                    entry.setCategory("Template");
                    searchlist.add(entry);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SearchBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(SearchBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
