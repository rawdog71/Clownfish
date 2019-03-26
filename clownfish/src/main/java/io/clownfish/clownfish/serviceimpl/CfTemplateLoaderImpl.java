package io.clownfish.clownfish.serviceimpl;

import freemarker.cache.TemplateLoader;
import io.clownfish.clownfish.constants.ClownfishConst;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.DEVELOPMENT;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
import io.clownfish.clownfish.utils.TemplateUtil;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Component
public class CfTemplateLoaderImpl implements TemplateLoader {
    @Autowired CfTemplateService cftemplateService;
    @Autowired CfTemplateversionService cftemplateversionService;
    @Autowired TemplateUtil templateUtil;
    
    private @Getter @Setter ClownfishConst.ViewModus modus = DEVELOPMENT;

    public CfTemplateLoaderImpl() {
    }

    @Override
    public Object findTemplateSource(String name) throws IOException {
        try {
            CfTemplate cftemplate = cftemplateService.findByName(name);
            return cftemplate;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public long getLastModified(Object templateObject) {
        return Long.MAX_VALUE;
    }

    @Override
    public Reader getReader(Object templateObject, String encoding) throws IOException {
        if (DEVELOPMENT == modus) {
            String content = ((CfTemplate) templateObject).getContent();
            content = templateUtil.fetchIncludes(content, modus);
            return new StringReader(content);
        } else {
            long currentTemplateVersion = 0;
            try {
                currentTemplateVersion = cftemplateversionService.findMaxVersion(((CfTemplate) templateObject).getId());
            } catch (NullPointerException ex) {
                currentTemplateVersion = 0;
            }
            String content = templateUtil.getVersion(((CfTemplate) templateObject).getId(), currentTemplateVersion);
            content = templateUtil.fetchIncludes(content, modus);
            return new StringReader(content);
        }
    }

    @Override
    public void closeTemplateSource(Object o) throws IOException {
    }
}
