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
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import io.clownfish.clownfish.constants.ClownfishConst;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.DEVELOPMENT;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.dbentities.CfTemplateversion;
import io.clownfish.clownfish.dbentities.CfTemplateversionPK;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import javax.faces.bean.ViewScoped;
import javax.persistence.NoResultException;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@ViewScoped
@Component
@Accessors(chain = true)
public class TemplateUtil implements Serializable {
    @Autowired transient CfTemplateService cftemplateService;
    @Autowired transient CfTemplateversionService cftemplateversionService;
    
    private @Getter @Setter long currentVersion;
    private @Getter @Setter String templateContent = "";
    private transient @Getter @Setter Patch<String> patch = null;
    private transient @Getter @Setter List<String> source = null;
    private transient @Getter @Setter List<String> target = null;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(TemplateUtil.class);

    public TemplateUtil() {
    }

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
    
    public boolean hasDifference(CfTemplate selectedTemplate) {
        boolean diff = false;
        try {
            try {
                currentVersion = (long) cftemplateversionService.findMaxVersion(selectedTemplate.getId());
            } catch (NullPointerException ex) {
                currentVersion = 0;
            }
            if (currentVersion > 0) {
                templateContent = selectedTemplate.getContent();
                String contentVersion = getVersion(selectedTemplate.getId(), currentVersion);
                source = Arrays.asList(templateContent.split("\\r?\\n"));
                target = Arrays.asList(contentVersion.split("\\r?\\n"));
                patch = DiffUtils.diff(source, target);
                if (!patch.getDeltas().isEmpty()) {
                    diff = true;
                }
            } else {
                diff = true;
            }
        } catch (DiffException ex) {
            LOGGER.error(ex.getMessage());
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
    
}
