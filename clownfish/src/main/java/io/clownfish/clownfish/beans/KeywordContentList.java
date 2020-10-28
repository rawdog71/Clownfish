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
package io.clownfish.clownfish.beans;

import io.clownfish.clownfish.dbentities.CfKeyword;
import io.clownfish.clownfish.dbentities.CfKeywordlist;
import io.clownfish.clownfish.dbentities.CfKeywordlistcontent;
import io.clownfish.clownfish.dbentities.CfKeywordlistcontentPK;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfKeywordlistService;
import io.clownfish.clownfish.serviceinterface.CfKeywordlistcontentService;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Named("keywordContentList")
@Scope("session")
@Component
public class KeywordContentList {
    @Autowired CfKeywordService cfkeywordService;
    @Autowired CfKeywordlistService cfkeywordlistService;
    @Autowired CfKeywordlistcontentService cfkeywordlistcontentService;
    
    private @Getter @Setter List<CfKeywordlist> keywordlist;
    private @Getter @Setter String keywordlistname;
    private @Getter @Setter List<CfKeyword> keywords;
    private @Getter @Setter CfKeywordlist selectedKeywordlist;
    private @Getter @Setter List<CfKeywordlist> filteredKeywordlist;
    private transient @Getter @Setter List<CfKeyword> selectedKeywordcontent = null;
    private transient @Getter @Setter List<CfKeyword> filteredKeywordcontent = null;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(KeywordContentList.class);
    
    @PostConstruct
    public void init() {
        keywordlist = cfkeywordlistService.findAll();
        keywords = cfkeywordService.findAll();
        
        selectedKeywordcontent = new ArrayList<>();
    }

    public void onCreate(ActionEvent actionEvent) {
        try {
            cfkeywordlistService.findByName(keywordlistname);
        } catch (NoResultException ex) {
            CfKeywordlist newkeywordlist = new CfKeywordlist();
            newkeywordlist.setName(keywordlistname);
            cfkeywordlistService.create(newkeywordlist);
            keywordlist = cfkeywordlistService.findAll();
            keywords = cfkeywordService.findAll();
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    public void onSelect(SelectEvent event) {
        selectedKeywordlist = (CfKeywordlist) event.getObject();
        
        filteredKeywordcontent = cfkeywordService.findAll();
        List<CfKeywordlistcontent> selectedkeywordlist = cfkeywordlistcontentService.findByKeywordlistref(selectedKeywordlist.getId());
        
        selectedKeywordcontent.clear();
        if (selectedkeywordlist.size() > 0) {
            for (CfKeywordlistcontent keywordcontent : selectedkeywordlist) {
                CfKeyword selectedKeyword = cfkeywordService.findById(keywordcontent.getCfKeywordlistcontentPK().getKeywordref());
                selectedKeywordcontent.add(selectedKeyword);
            }
        }
    }
    
    public void onChangeContent(AjaxBehaviorEvent event) {
        // Delete listcontent first
        List<CfKeywordlistcontent> keywordList = cfkeywordlistcontentService.findByKeywordlistref(selectedKeywordlist.getId());
        //List<CfKeyword> keywordList = cfkeywordlistService.findById(selectedKeywordlist.getId());
        for (CfKeywordlistcontent content : keywordList) {
            cfkeywordlistcontentService.delete(content);
        }
        // Add selected listcontent
        if (selectedKeywordcontent.size() > 0) {
            for (CfKeyword selected : selectedKeywordcontent) {
                CfKeywordlistcontent keywordlistcontent = new CfKeywordlistcontent();
                CfKeywordlistcontentPK cflistcontentPK = new CfKeywordlistcontentPK();
                cflistcontentPK.setKeywordlistref(selectedKeywordlist.getId());
                cflistcontentPK.setKeywordref(selected.getId());
                keywordlistcontent.setCfKeywordlistcontentPK(cflistcontentPK);
                cfkeywordlistcontentService.create(keywordlistcontent);
            }
        }
    }
}
