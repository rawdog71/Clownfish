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
import io.clownfish.clownfish.dbentities.CfAssetkeyword;
import io.clownfish.clownfish.dbentities.CfKeyword;
import io.clownfish.clownfish.lucene.AssetIndexer;
import io.clownfish.clownfish.lucene.IndexService;
import io.clownfish.clownfish.serviceinterface.CfAssetKeywordService;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.utils.FolderUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DualListModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author sulzbachr
 */
@Named("assetList")
@Scope("singleton")
@Component
public class AssetList {
    @Autowired CfAssetService cfassetService;
    @Autowired CfKeywordService cfkeywordService;
    @Autowired CfAssetKeywordService cfassetkeywordService;
    @Autowired IndexService indexService;
    @Autowired AssetIndexer assetIndexer;
    @Autowired FolderUtil folderUtil;
    @Autowired ContentList classcontentlist;
    
    private @Getter @Setter List<CfAsset> assetlist;
    private @Getter @Setter CfAsset selectedAsset;
    private @Getter @Setter Boolean checkedAsset;
    private @Getter @Setter String assetName;
    private @Getter @Setter DualListModel<CfKeyword> keywords;
    private List<CfKeyword> keywordSource;
    private List<CfKeyword> keywordTarget;
    private @Getter @Setter boolean isImage;
    private @Getter @Setter boolean isPdf;
    private @Getter @Setter boolean renderDetail;
    private @Getter @Setter String description = "";
    
    private List<CfAssetkeyword> assetkeywordlist;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(AssetList.class);

    /**
     * Initializes the AssetList
     * Retrieves all assetlists from db
     * Retrieves all assets from db
     */
    @PostConstruct
    public void init() {
        LOGGER.info("INIT ASSETLIST START");
        renderDetail = false;
        assetName = "";
        assetlist = cfassetService.findAll();
        
        keywordSource = cfkeywordService.findAll();
        keywordTarget = new ArrayList<>();
        
        keywords = new DualListModel<>(keywordSource, keywordTarget);
        LOGGER.info("INIT ASSETLIST END");
    }
    
    public void onRefreshAll() {
        assetlist = cfassetService.findAll();
        keywordSource = cfkeywordService.findAll();
    }
    
    /**
     * Handles the file upload
     * Stores the files in the media path
     * Tika parser retrieves the metadata
     * Lucene indexes the assets
     * @param event
     * @throws org.apache.tika.exception.TikaException
     * @throws org.xml.sax.SAXException
     */
    public void handleFileUpload(FileUploadEvent event) throws TikaException, SAXException {
        String filename = event.getFile().getFileName().toLowerCase();
        LOGGER.info("UPLOAD: {}", filename);
        HashMap<String, String> metamap = new HashMap<>();
        try {
            File result = new File(folderUtil.getMedia_folder() + File.separator + filename);
            InputStream inputStream;
            try (FileOutputStream fileOutputStream = new FileOutputStream(result)) {
                byte[] buffer = new byte[64535];
                int bulk;
                inputStream = event.getFile().getInputStream();
                while (true) {
                    bulk = inputStream.read(buffer);
                    if (bulk < 0) {
                        break;
                    }
                    fileOutputStream.write(buffer, 0, bulk);
                    fileOutputStream.flush();
                }
                fileOutputStream.close();
            }
            inputStream.close();
            
            //detecting the file type using detect method
            String fileextension = FilenameUtils.getExtension(folderUtil.getMedia_folder() + File.separator + filename);
            
            Parser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            try (FileInputStream inputstream = new FileInputStream(result)) {
                ParseContext context = new ParseContext();
                parser.parse(inputstream, handler, metadata, context);
                //System.out.println(handler.toString());
            }

            //getting the list of all meta data elements 
            String[] metadataNames = metadata.names();
            for(String name : metadataNames) {		        
                //System.out.println(name + ": " + metadata.get(name));
                metamap.put(name, metadata.get(name));
            }
            
            CfAsset newasset = new CfAsset();
            newasset.setName(filename);
            newasset.setFileextension(fileextension.toLowerCase());
            newasset.setMimetype(metamap.get("Content-Type"));
            newasset.setImagewidth(metamap.get("Image Width"));
            newasset.setImageheight(metamap.get("Image Height"));
            cfassetService.create(newasset);
            assetlist = cfassetService.findAll();
            
            // Index the uploaded assets and merge the Index files
            if ((null != folderUtil.getIndex_folder()) && (!folderUtil.getMedia_folder().isEmpty())) {
                Thread assetindexer_thread = new Thread(assetIndexer);
                assetindexer_thread.start();
            }
            
            assetName = "";
            
            classcontentlist.initAssetlist();
            FacesMessage message = new FacesMessage("Succesful", filename + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        } catch (IOException | PersistenceException e) {
            LOGGER.error(e.getMessage());
            FacesMessage error = new FacesMessage("The files were not uploaded!");
            FacesContext.getCurrentInstance().addMessage(null, error);
        }
    }
    
    /**
     * Handles the file scrapping
     * Sets the scrapped flag to indicate the asset is on the scrapyard
     */
    public void onScrapp() {
        selectedAsset.setScrapped(true);
        cfassetService.edit(selectedAsset);
        assetlist = cfassetService.findAll();
        FacesMessage message = new FacesMessage("Succesful", selectedAsset.getName() + " has been scrapped.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    /**
     * Handles the file recycling
     * Sets the scrapped flag to indicate the asset is recycled from the scrapyard
     */
    public void onRecycle() {
        selectedAsset.setScrapped(true);
        cfassetService.edit(selectedAsset);
        assetlist = cfassetService.findAll();
        FacesMessage message = new FacesMessage("Succesful", selectedAsset.getName() + " has been recycled.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    /**
     * Handles the file delete
     * Deletes the files from the media path and the database
     * Removes from Lucene index
     */
    /*
    public void onDelete() {
        try {
            assetIndexer.removeDocument(selectedAsset);
            indexService.getWriter().commit();
            indexService.getWriter().forceMerge(10);
            cfassetService.delete(selectedAsset);
            File file = new File(folderUtil.getMedia_folder() + File.separator + selectedAsset.getName());
            assetlist = cfassetService.findAll();
            if (file.delete()) {
                FacesMessage message = new FacesMessage("Succesful", selectedAsset.getName() + " has been deleted.");
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    */
    
    /**
     * Handles the detail event
     * Retrieves the metadata from the asset
     */
    public void onDetail() {
        description = selectedAsset.getDescription();
        keywords.getTarget().clear();
        keywords.getSource().clear();
        renderDetail = true;
        keywords.setSource(cfkeywordService.findAll());
        assetkeywordlist = cfassetkeywordService.findByAssetRef(selectedAsset.getId());
        for (CfAssetkeyword assetkeyword : assetkeywordlist) {
            CfKeyword kw = cfkeywordService.findById(assetkeyword.getCfAssetkeywordPK().getKeywordref());
            keywords.getTarget().add(kw);
            keywords.getSource().remove(kw);
        }
        isImage = selectedAsset.getMimetype().contains("jpeg");
        isPdf = selectedAsset.getMimetype().contains("pdf") || selectedAsset.getMimetype().contains("octet-stream");
    }
 
    /**
     * Attache/Detaches the keywords to assetss 
     * @param actionEvent
     */
    public void onAttach(ActionEvent actionEvent) {
        assetkeywordlist = cfassetkeywordService.findByAssetRef(selectedAsset.getId());
        for (CfAssetkeyword assetkeyword : assetkeywordlist) {
            cfassetkeywordService.delete(assetkeyword);
        }
        List<CfKeyword> selectedkeyword = keywords.getTarget();
        try {
            for (Object keyword : selectedkeyword) {
                CfAssetkeyword assetkeyword = new CfAssetkeyword(selectedAsset.getId(), ((CfKeyword)keyword).getId());
                cfassetkeywordService.create(assetkeyword);
            }
        } catch (ConstraintViolationException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    /**
     * Edits the description for an asset
     * @param actionEvent
     */
    public void editDescription(ActionEvent actionEvent) {
        selectedAsset.setDescription(description);
        cfassetService.edit(selectedAsset);
        FacesMessage message = new FacesMessage("Succesful", selectedAsset.getName() + " has been updated.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
}
