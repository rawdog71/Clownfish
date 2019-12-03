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

import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAssetlistcontent;
import io.clownfish.clownfish.dbentities.CfAssetlistcontentPK;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistcontentService;
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
@Named("assetLibrary")
@Scope("session")
@Component
public class AssetLibrary {
    @Autowired CfAssetService cfassetService;
    @Autowired CfAssetlistService cfassetlistService;
    @Autowired CfAssetlistcontentService cfassetlistcontentService;
    
    private @Getter @Setter List<CfAssetlist> assetlist;
    private @Getter @Setter String assetlistname;
    private @Getter @Setter List<CfAsset> assets;
    private @Getter @Setter CfAssetlist selectedAssetlist;
    private @Getter @Setter List<CfAssetlist> filteredAssetlist;
    private transient @Getter @Setter List<CfAsset> selectedAssetcontent = null;
    private transient @Getter @Setter List<CfAsset> filteredAssetcontent = null;
    
    final transient Logger logger = LoggerFactory.getLogger(AssetLibrary.class);
    
    /**
     * Initializes the AssetLibrary
     * Retrieves all assetlists from db
     * Retrieves all assets from db
     */
    @PostConstruct
    public void init() {
        assetlist = cfassetlistService.findAll();
        assets = cfassetService.findAll();
        
        selectedAssetcontent = new ArrayList<>();
    }

    /**
     * Creates an asset library in db
     * @param actionEvent
     */
    public void onCreate(ActionEvent actionEvent) {
        try {
            cfassetlistService.findByName(assetlistname);
        } catch (NoResultException ex) {
            CfAssetlist newassetlist = new CfAssetlist();
            newassetlist.setName(assetlistname);
            cfassetlistService.create(newassetlist);
            assetlist = cfassetlistService.findAll();
            assets = cfassetService.findAll();
        } catch (ConstraintViolationException ex) {
            logger.error(ex.getMessage());
        }
    }
    
    /**
     * Selection of an asset list
     * Updates the selected assets in the asset list
     * @param event
     */
    public void onSelect(SelectEvent event) {
        selectedAssetlist = (CfAssetlist) event.getObject();
        
        filteredAssetcontent = cfassetService.findAll();
        List<CfAssetlistcontent> selectedassetlist = cfassetlistcontentService.findByAssetlistref(selectedAssetlist.getId());
        
        selectedAssetcontent.clear();
        if (selectedassetlist.size() > 0) {
            for (CfAssetlistcontent assetcontent : selectedassetlist) {
                CfAsset selectedAasset = cfassetService.findById(assetcontent.getCfAssetlistcontentPK().getAssetref());
                selectedAssetcontent.add(selectedAasset);
            }
        }
    }
    
    /**
     * Changing of an asset list
     * Updates the selected assets in the asset list database
     * @param event
     */
    public void onChangeContent(AjaxBehaviorEvent event) {
        // Delete listcontent first
        List<CfAssetlistcontent> assetList = cfassetlistcontentService.findByAssetlistref(selectedAssetlist.getId());
        //List<CfAasset> assetList = cfassetlistService.findById(selectedAassetlist.getId());
        for (CfAssetlistcontent content : assetList) {
            cfassetlistcontentService.delete(content);
        }
        // Add selected listcontent
        if (selectedAssetcontent.size() > 0) {
            for (CfAsset selected : selectedAssetcontent) {
                CfAssetlistcontent assetlistcontent = new CfAssetlistcontent();
                CfAssetlistcontentPK cflistcontentPK = new CfAssetlistcontentPK();
                cflistcontentPK.setAssetlistref(selectedAssetlist.getId());
                cflistcontentPK.setAssetref(selected.getId());
                assetlistcontent.setCfAssetlistcontentPK(cflistcontentPK);
                cfassetlistcontentService.create(assetlistcontent);
            }
        }
    }
}
