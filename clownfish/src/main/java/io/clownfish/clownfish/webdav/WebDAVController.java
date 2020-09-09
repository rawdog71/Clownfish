/*
 * Copyright 2020 SulzbachR.
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
package io.clownfish.clownfish.webdav;

/**
 *
 * @author SulzbachR
 */
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.lucene.AssetIndexer;
import io.clownfish.clownfish.lucene.IndexService;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.utils.FolderUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.ContentLength;
import io.milton.annotations.Delete;
import io.milton.annotations.Get;
import io.milton.annotations.ModifiedDate;
import io.milton.annotations.Name;
import io.milton.annotations.PutChild;
import io.milton.annotations.ResourceController;
import io.milton.annotations.Root;
import io.milton.annotations.UniqueId;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

@Component
@ResourceController
public class WebDAVController  {
    @Autowired CfAssetService cfassetService;
    @Autowired PropertyUtil propertyUtil;
    @Autowired FolderUtil folderUtil;
    @Autowired IndexService indexService;
    @Autowired AssetIndexer assetIndexer;
    final transient Logger logger = LoggerFactory.getLogger(WebDAVController.class);
    private @Getter @Setter List<CfAsset> assetlist;
    private static CfAssetService assetService;
    private static PropertyUtil propUtil;
    private static FolderUtil folderUtiliy;
    private static IndexService idxService;
    private static AssetIndexer assetIndex;
    
    
    @PostConstruct
    public void init() {
        logger.info("PostConstruct WebDAVController");
        assetService = cfassetService;
        propUtil = propertyUtil;
        folderUtiliy = folderUtil;
        assetIndex = assetIndexer;
        idxService = indexService;
    }

    public WebDAVController() {
    }
    
    @Root
    public WebDAVController getRoot() {
        return this;
    }
    
    @ChildrenOf
    public List<CfAsset> getAssets(WebDAVController root) {
        assetlist = assetService.findAll();
        return assetlist;
    }
    
    @Name
    public String getName(CfAsset asset) {
        return asset.getName();
    }
    
    @ContentLength
    public Long getContentLength(CfAsset asset) {
        try {
            String imagefilename = asset.getName();
            return Files.size(Paths.get(propUtil.getPropertyValue("folder_media") + File.separator + imagefilename));
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(WebDAVController.class.getName()).log(Level.SEVERE, null, ex);
            return 0L;
        }
    }
    
    @ModifiedDate
    public Date getModifieyDate(CfAsset asset) {
        try {
            String imagefilename = asset.getName();
            return new Date(Files.getLastModifiedTime(Paths.get(propUtil.getPropertyValue("folder_media") + File.separator + imagefilename)).toMillis());
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(WebDAVController.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    @Get
    public byte[] download(CfAsset asset) {
        try {
            String imagefilename = asset.getName();
            return Files.readAllBytes(Paths.get(propUtil.getPropertyValue("folder_media") + File.separator + imagefilename));
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(WebDAVController.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    @PutChild
    public CfAsset upload(WebDAVController root, String filename, InputStream inputStream) {
        try {
            HashMap<String, String> metamap = new HashMap<>();
            File result = new File(folderUtiliy.getMedia_folder() + File.separator + filename);
            try (FileOutputStream fileOutputStream = new FileOutputStream(result)) {
                byte[] buffer = new byte[64535];
                int bulk;
                while (true) {
                    bulk = inputStream.read(buffer);
                    if (bulk < 0) {
                        break;
                    }
                    fileOutputStream.write(buffer, 0, bulk);
                    fileOutputStream.flush();
                }
                fileOutputStream.close();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(WebDAVController.class.getName()).log(Level.SEVERE, null, ex);
            }
            inputStream.close();
            
            //detecting the file type using detect method
            String fileextension = FilenameUtils.getExtension(folderUtiliy.getMedia_folder() + File.separator + filename);
            
            Parser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            try (FileInputStream inputstream = new FileInputStream(result)) {
                ParseContext context = new ParseContext();
                parser.parse(inputstream, handler, metadata, context);
                //System.out.println(handler.toString());
            } catch (SAXException | TikaException ex) {
                java.util.logging.Logger.getLogger(WebDAVController.class.getName()).log(Level.SEVERE, null, ex);
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
            if (newasset.getMimetype().contains("jpeg")) {
                newasset.setImagewidth(metamap.get("Image Width"));
                newasset.setImageheight(metamap.get("Image Height"));
            }
            assetService.create(newasset);
            assetlist = assetService.findAll();
            
            // Index the uploaded assets and merge the Index files
            if ((null != folderUtiliy.getIndex_folder()) && (!folderUtiliy.getMedia_folder().isEmpty())) {
                assetIndex.run();
                idxService.getWriter().commit();
                idxService.getWriter().forceMerge(10);
            }
            
            return newasset;
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(WebDAVController.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    @UniqueId
    public String getUniqueId(CfAsset asset) {
        return asset.getName();
    }
    
    @Delete
    public void delete(CfAsset asset) {
        assetService.delete(asset);
        assetlist = assetService.findAll();
    }
}