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
package io.clownfish.clownfish.utils;

import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetlistcontent;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfAttributetype;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfClasscontentkeyword;
import io.clownfish.clownfish.dbentities.CfListcontent;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributetypeService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentKeywordService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ViewScoped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@ViewScoped
@Component
public class ClassUtil {
    @Autowired CfAttributService cfattributService;
    @Autowired CfAttributetypeService cfattributetypeService;
    @Autowired CfAttributcontentService cfattributcontentService;
    @Autowired CfClasscontentService cfclasscontentService;
    @Autowired CfListcontentService cflistcontentService;
    @Autowired CfClasscontentKeywordService cfclasscontentkeywordService;
    @Autowired CfKeywordService cfkeywordService;
    @Autowired CfAssetlistcontentService cfassetlistcontentService;
    @Autowired CfAssetService cfassetService;
    @Autowired MarkdownUtil markdownUtil;
    
    public ClassUtil() {
    }
    
    public Map getattributmap (CfClasscontent classcontent) {
        List<CfAttributcontent> attributcontentlist = new ArrayList<>();
        attributcontentlist.addAll(cfattributcontentService.findByClasscontentref(classcontent));
        
        Map attributcontentmap = new LinkedHashMap();

        for (CfAttributcontent attributcontent : attributcontentlist) {
            CfAttribut cfattribut = cfattributService.findById(attributcontent.getAttributref().getId());
            CfAttributetype cfattributtype = cfattributetypeService.findById(cfattribut.getAttributetype().getId());
            switch (cfattributtype.getName()) {
                case "boolean":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentBoolean());
                    break;
                case "string":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentString());
                    break;
                case "hashstring":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentString());
                    break;    
                case "integer":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentInteger());
                    break;
                case "real":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentReal());
                    break;
                case "htmltext":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentText());
                    break;
                case "datetime":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentDate());
                    break;
                case "media":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentInteger());
                    break;
                case "text":
                    attributcontentmap.put(cfattribut.getName(), attributcontent.getContentText());
                    break;    
                case "markdown":
                    markdownUtil.initOptions();
                    if (null != attributcontent.getContentText()) {
                        attributcontentmap.put(cfattribut.getName(), markdownUtil.parseMarkdown(attributcontent.getContentText(), markdownUtil.getMarkdownOptions()));
                    } else {
                        attributcontentmap.put(cfattribut.getName(), markdownUtil.parseMarkdown("", markdownUtil.getMarkdownOptions()));
                    }
                    break;
                case "classref":
                    if (null != attributcontent.getClasscontentlistref()) {
                        Map listcontentmap = new LinkedHashMap();
                        List<CfListcontent> selectedcontent = cflistcontentService.findByListref(attributcontent.getClasscontentlistref().getId());
                        List<CfClasscontent> selectedListcontent = new ArrayList<>();
                        selectedListcontent.clear();
                        if (selectedcontent.size() > 0) {
                            selectedcontent.stream().map((listcontent) -> cfclasscontentService.findById(listcontent.getCfListcontentPK().getClasscontentref())).forEach((selectedContent) -> {
                                if (null != selectedContent) {
                                    selectedListcontent.add(selectedContent);
                                }
                            });
                        }
                        selectedListcontent.stream().forEach((cc) -> {
                            Map dummy_attributcontentmap = new LinkedHashMap();
                            dummy_attributcontentmap = getattributmap(cc);
                            listcontentmap.put(cc.getName(), dummy_attributcontentmap);
                        });
                        attributcontentmap.put(attributcontent.getAttributref().getName(), listcontentmap);
                    }
                    break;
                case "assetref":
                    if (null != attributcontent.getAssetcontentlistref()) {
                        Map assetlistcontentmap = new LinkedHashMap();
                        List<CfAssetlistcontent> selectedlistassets = cfassetlistcontentService.findByAssetlistref(attributcontent.getAssetcontentlistref().getId());
                        List<CfAsset> selectedAssets = new ArrayList<>();
                        selectedAssets.clear();
                        if (selectedlistassets.size() > 0) {
                            selectedlistassets.stream().map((listcontent) -> cfassetService.findById(listcontent.getCfAssetlistcontentPK().getAssetref())).forEach((selectedContent) -> {
                                if (null != selectedContent) {
                                    selectedAssets.add(selectedContent);
                                }
                            });
                        }                        
                        selectedAssets.stream().forEach((asset) -> {
                            assetlistcontentmap.put(asset.getName(), asset);
                        });
                        attributcontentmap.put(attributcontent.getAttributref().getName(), assetlistcontentmap);
                    }
                    break;    
            }
        }
        /* add keywords  */
        List<CfClasscontentkeyword> contentkeywordlist;
        contentkeywordlist = cfclasscontentkeywordService.findByClassContentRef(classcontent.getId());
        if (contentkeywordlist.size() > 0) {
            ArrayList listcontentmap = new ArrayList();
            contentkeywordlist.stream().forEach((contentkeyword) -> {
                listcontentmap.add(cfkeywordService.findById(contentkeyword.getCfClasscontentkeywordPK().getKeywordref()));
            });
            attributcontentmap.put("keywords", listcontentmap);
        }
        
        return attributcontentmap;
    }
}
