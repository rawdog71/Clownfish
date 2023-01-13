package io.clownfish.clownfish.serviceimpl;

import freemarker.cache.StringTemplateLoader;
import io.clownfish.clownfish.constants.ClownfishConst;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.DEVELOPMENT;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
import io.clownfish.clownfish.utils.TemplateUtil;
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
public class CfStringTemplateLoaderImpl extends StringTemplateLoader {
    @Autowired CfTemplateService cftemplateService;
    @Autowired CfTemplateversionService cftemplateversionService;
    @Autowired TemplateUtil templateUtil;
    private @Getter @Setter String content;
    
    private @Getter @Setter ClownfishConst.ViewModus modus = DEVELOPMENT;

    public CfStringTemplateLoaderImpl() {
    }

    @Override
    public Object findTemplateSource(String name) {
        try {
            if ((name.endsWith(".ftl")) || (name.endsWith(".vm"))) {
                name = name.substring(0, name.lastIndexOf("."));
            }
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
    public Reader getReader(Object templateObject, String encoding) {
        /*
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
        }*/
        return new StringReader(content);
    }
    
    @Override
    public void closeTemplateSource(Object o) {
    }
}
