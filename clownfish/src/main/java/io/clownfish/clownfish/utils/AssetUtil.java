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
package io.clownfish.clownfish.utils;

import io.clownfish.clownfish.beans.PropertyList;
import io.clownfish.clownfish.dbentities.CfAsset;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author SulzbachR
 */
public class AssetUtil {
    private static Map<String, String> propertymap = null;
    private final PropertyList propertylist;
    BodyContentHandler handler;
    private final Parser parser;
    
    final transient Logger logger = LoggerFactory.getLogger(AssetUtil.class);
    
    public AssetUtil(PropertyList propertylist) {
        this.propertylist = propertylist;
        
        parser = new AutoDetectParser();
    }
    
    public HashMap<String, String> getMetadata(CfAsset assetcontent, HashMap metamap) throws IOException {
        if (propertymap == null) {
            // read all System Properties of the property table
            propertymap = propertylist.fillPropertyMap();
        }
        String media_folder = propertymap.get("folder_media");
        
        handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        metamap.clear();
         
        try (FileInputStream inputstream = new FileInputStream(media_folder + File.separator + assetcontent.getName())) {
            ParseContext context = new ParseContext();
            parser.parse(inputstream, handler, metadata, context);
            //System.out.println(handler.toString());
        } catch (SAXException | TikaException ex) {
            logger.error(ex.getMessage());
        }

        //getting the list of all meta data elements 
        String[] metadataNames = metadata.names();
        for(String name : metadataNames) {		        
            //System.out.println(name + ": " + metadata.get(name));
            metamap.put(name, metadata.get(name));
        }
        
        return metamap;
    }
}
