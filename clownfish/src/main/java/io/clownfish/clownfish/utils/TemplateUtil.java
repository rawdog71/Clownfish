/*
 * Copyright Rainer Sulzbach
 */
package io.clownfish.clownfish.utils;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import io.clownfish.clownfish.constants.ClownfishConst;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.DEVELOPMENT;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.dbentities.CfTemplateversion;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import javax.faces.bean.ViewScoped;
import javax.persistence.NoResultException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@ViewScoped
@Component
public class TemplateUtil {
    @Autowired CfTemplateService cftemplateService;
    @Autowired CfTemplateversionService cftemplateversionService;
    
    private @Getter @Setter long currentVersion;
    private @Getter @Setter  String templateContent = "";
    private @Getter @Setter  Patch<String> patch = null;
    private @Getter @Setter  List<String> source = null;
    private @Getter @Setter  List<String> target = null;

    public TemplateUtil() {
    }

    public String getVersion(long templateref, long version) {
        try {
            CfTemplateversion template = cftemplateversionService.findByPK(templateref, version);
            byte[] decompress = CompressionUtils.decompress(template.getContent());
            return new String(decompress, StandardCharsets.UTF_8);
        } catch (IOException | DataFormatException ex) {
            Logger.getLogger(TemplateUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
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
            Logger.getLogger(TemplateUtil.class.getName()).log(Level.SEVERE, null, ex);
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
                    long currentTemplateVersion = 0;
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
