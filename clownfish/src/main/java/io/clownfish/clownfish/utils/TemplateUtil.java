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

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import io.clownfish.clownfish.constants.ClownfishConst;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.DEVELOPMENT;
import io.clownfish.clownfish.datamodels.CfDiv;
import io.clownfish.clownfish.datamodels.CfLayout;
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfKeywordlist;
import io.clownfish.clownfish.dbentities.CfLayoutcontent;
import io.clownfish.clownfish.dbentities.CfList;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.dbentities.CfTemplateversion;
import io.clownfish.clownfish.dbentities.CfTemplateversionPK;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfKeywordlistService;
import io.clownfish.clownfish.serviceinterface.CfLayoutcontentService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import javax.persistence.NoResultException;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Scope("singleton")
@Component
@Accessors(chain = true)
public class TemplateUtil implements IVersioningInterface, Serializable {
    @Autowired transient CfTemplateService cftemplateService;
    @Autowired transient CfTemplateversionService cftemplateversionService;
    @Autowired transient CfKeywordlistService cfkeywordlistService;
    @Autowired transient CfClasscontentService cfclasscontentService;
    @Autowired transient CfListService cflistService;
    @Autowired transient CfAssetService cfassetService;
    @Autowired transient CfAssetlistService cfassetlistService;
    @Autowired transient CfLayoutcontentService cflayoutcontentService;
    
    private @Getter @Setter long currentVersion;
    private @Getter @Setter String templateContent = "";
    private transient @Getter @Setter Patch<String> patch = null;
    private transient @Getter @Setter List<String> source = null;
    private transient @Getter @Setter List<String> target = null;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(TemplateUtil.class);

    public TemplateUtil() {
    }

    @Override
    public String getVersion(long templateref, long version) {
        try {
            CfTemplateversion template = cftemplateversionService.findByPK(templateref, version);
            byte[] decompress = CompressionUtils.decompress(template.getContent());
            return new String(decompress, StandardCharsets.UTF_8);
        } catch (IOException | DataFormatException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }
    
    @Override
    public void writeVersion(long templateref, long version, byte[] content, long currentuserid) {
        CfTemplateversionPK templateversionpk = new CfTemplateversionPK();
        templateversionpk.setTemplateref(templateref);
        templateversionpk.setVersion(version);

        CfTemplateversion cftemplateversion = new CfTemplateversion();
        cftemplateversion.setCfTemplateversionPK(templateversionpk);
        cftemplateversion.setContent(content);
        cftemplateversion.setTstamp(new Date());
        cftemplateversion.setCommitedby(BigInteger.valueOf(currentuserid));
        cftemplateversionService.create(cftemplateversion);
    }
    
    @Override
    public boolean hasDifference(Object object) {
        boolean diff = false;
        try {
            currentVersion = (long) cftemplateversionService.findMaxVersion(((CfTemplate)object).getId());
        } catch (NullPointerException ex) {
            currentVersion = 0;
        }
        if (currentVersion > 0) {
            templateContent = ((CfTemplate)object).getContent();
            String contentVersion = getVersion(((CfTemplate)object).getId(), currentVersion);
            source = Arrays.asList(templateContent.split("\\r?\\n"));
            target = Arrays.asList(contentVersion.split("\\r?\\n"));
            patch = DiffUtils.diff(source, target);
            if (!patch.getDeltas().isEmpty()) {
                diff = true;
            }
        } else {
            diff = true;
        }
        return diff;
    }
    
    public String fetchIncludes(String content, ClownfishConst.ViewModus modus) {
        Pattern pattern = Pattern.compile("(\\[\\[\\*).+(\\*\\]\\])");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String templatename = content.substring((matcher.start()+3), (matcher.end()-3));
            String lastmatch = content.substring(matcher.start(), matcher.end());
            try {
                // Hole das Template über den Namen
                CfTemplate cftemplate = cftemplateService.findByName(templatename);
                if (DEVELOPMENT == modus) {
                    content = content.replace(lastmatch, cftemplate.getContent());
                } else {
                    long currentTemplateVersion;
                    try {
                        currentTemplateVersion = (long) cftemplateversionService.findMaxVersion(cftemplate.getId());
                    } catch (NullPointerException ex) {
                        currentTemplateVersion = 0;
                    }
                    content = content.replace(lastmatch, getVersion(cftemplate.getId(), currentTemplateVersion));
                }
                matcher = pattern.matcher(content);
            } catch (NoResultException ex) {
                content = matcher.replaceFirst("");
            }                 
        }
        return content;
    }

    @Override
    public long getCurrentVersionNumber(String name) {
        CfTemplate cftemplate = cftemplateService.findByName(name);
        return cftemplateversionService.findMaxVersion((cftemplate).getId());
    }
    
    public String replacePlaceholders(String content, CfDiv cfdiv, List<CfLayoutcontent> layoutcontent, boolean preview) {
        // replace Content
        for (String c : cfdiv.getContentArray()) {
            List<CfLayoutcontent> contentlist = layoutcontent.stream().filter(lc -> lc.getCfLayoutcontentPK().getContenttype().compareToIgnoreCase("C") == 0).collect(Collectors.toList());
            for (CfLayoutcontent lc : contentlist) {
                CfClasscontent cfcontent = null;
                if (preview) {
                    if (lc.getPreview_contentref().longValue() > 0) {
                        cfcontent = cfclasscontentService.findById(lc.getPreview_contentref().longValue());
                    }
                } else {
                    if ((null != lc.getContentref()) && (lc.getContentref().longValue() > 0)) {
                        cfcontent = cfclasscontentService.findById(lc.getContentref().longValue());
                    }
                }
                String[] cs = c.split(":");
                if (lc.getCfLayoutcontentPK().getLfdnr() == Integer.parseInt(cs[1])) {
                    String replacefilter = "#C:" + c + "#";
                    if (null != cfcontent) {
                        content = content.replaceAll(replacefilter, cfcontent.getName());
                    }
                }
            }
        }
        // replace Datalists
        for (String c : cfdiv.getContentlistArray()) {
            List<CfLayoutcontent> datalist = layoutcontent.stream().filter(lc -> lc.getCfLayoutcontentPK().getContenttype().compareToIgnoreCase("DL") == 0).collect(Collectors.toList());
            for (CfLayoutcontent lc : datalist) {
                CfList cflist = null;
                if (preview) {
                    if (lc.getPreview_contentref().longValue() > 0) {
                        cflist = cflistService.findById(lc.getPreview_contentref().longValue());
                    }
                } else {
                    if ((null != lc.getContentref()) && (lc.getContentref().longValue() > 0)) {
                        cflist = cflistService.findById(lc.getContentref().longValue());
                    }
                }
                String[] cs = c.split(":");
                if (lc.getCfLayoutcontentPK().getLfdnr() == Integer.parseInt(cs[1])) {
                    String replacefilter = "#DL:" + c + "#";
                    if (null != cflist) {
                        content = content.replaceAll(replacefilter, cflist.getName());
                    }
                }
            }
        }
        // replace Assets
        for (String c : cfdiv.getAssetArray()) {
            int lfdnr = Integer.parseInt(c.split(":")[1]);
            List<CfLayoutcontent> assets = layoutcontent.stream().filter(lc -> (lc.getCfLayoutcontentPK().getContenttype().compareToIgnoreCase("A") == 0) && (lc.getCfLayoutcontentPK().getLfdnr() == lfdnr)).collect(Collectors.toList());
            for (CfLayoutcontent lc : assets) {
                CfAsset cfasset = null;
                if (preview) {
                    if (lc.getPreview_contentref().longValue() > 0) {
                        cfasset = cfassetService.findById(lc.getPreview_contentref().longValue());
                    }
                } else {
                    if ((null != lc.getContentref()) && (lc.getContentref().longValue() > 0)) {
                        cfasset = cfassetService.findById(lc.getContentref().longValue());
                    }
                }
                String replacefilter = "#A:" + c + "#";
                if (null != cfasset) {
                    content = content.replaceAll(replacefilter, cfasset.getId().toString());
                }
            }
        }
        // replace Assetlists
        for (String c : cfdiv.getAssetlistArray()) {
            List<CfLayoutcontent> assetlist = layoutcontent.stream().filter(lc -> lc.getCfLayoutcontentPK().getContenttype().compareToIgnoreCase("AL") == 0).collect(Collectors.toList());
            for (CfLayoutcontent lc : assetlist) {
                CfAssetlist cfassetlist = null;
                if (preview) {
                    if (lc.getPreview_contentref().longValue() > 0) {
                        cfassetlist = cfassetlistService.findById(lc.getPreview_contentref().longValue());
                    }
                } else {
                    if ((null != lc.getContentref()) && (lc.getContentref().longValue() > 0)) {
                        cfassetlist = cfassetlistService.findById(lc.getContentref().longValue());
                    }
                }
                String[] cs = c.split(":");
                if (lc.getCfLayoutcontentPK().getLfdnr() == Integer.parseInt(cs[1])) {
                    String replacefilter = "#AL:" + c + "#";
                    if (null != cfassetlist) {
                        content = content.replaceAll(replacefilter, cfassetlist.getName());
                    }
                }
            }
        }
        // replace Keywordlists
        for (String c : cfdiv.getKeywordlistArray()) {
            List<CfLayoutcontent> keywordlist = layoutcontent.stream().filter(lc -> lc.getCfLayoutcontentPK().getContenttype().compareToIgnoreCase("KL") == 0).collect(Collectors.toList());
            for (CfLayoutcontent lc : keywordlist) {
                CfKeywordlist cfkeywordlist = null;
                if (preview) {
                    if (lc.getPreview_contentref().longValue() > 0) {
                        cfkeywordlist = cfkeywordlistService.findById(lc.getPreview_contentref().longValue());
                    }
                } else {
                    if ((null != lc.getContentref()) && (lc.getContentref().longValue() > 0)) {
                        cfkeywordlist = cfkeywordlistService.findById(lc.getContentref().longValue());
                    }
                }
                String[] cs = c.split(":");
                if (lc.getCfLayoutcontentPK().getLfdnr() == Integer.parseInt(cs[1])) {
                    String replacefilter = "#KL:" + c + "#";
                    if (null != cfkeywordlist) {
                        content = content.replaceAll(replacefilter, cfkeywordlist.getName());
                    }
                }
            }
        }
        return content;
    }
}
