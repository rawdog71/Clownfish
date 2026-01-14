/*
 * Copyright 2026 SulzbachR.
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
package io.clownfish.clownfish.service;

import freemarker.template.TemplateException;
import io.clownfish.clownfish.beans.SiteTreeBean;
import io.clownfish.clownfish.constants.ClownfishConst.ViewModus;
import io.clownfish.clownfish.datamodels.AuthTokenList;
import io.clownfish.clownfish.datamodels.BeanContext;
import io.clownfish.clownfish.datamodels.CfDiv;
import io.clownfish.clownfish.datamodels.CfLayout;
import io.clownfish.clownfish.datamodels.RenderContext;
import io.clownfish.clownfish.dbentities.CfLayoutcontent;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.exceptions.ClownfishTemplateException;
import io.clownfish.clownfish.serviceinterface.CfLayoutcontentService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
import io.clownfish.clownfish.utils.ClownfishUtil;
import io.clownfish.clownfish.utils.TemplateUtil;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author SulzbachR
 */
@Service
public class LayoutService {
    @Autowired CfLayoutcontentService cflayoutcontentService;
    @Autowired CfTemplateService cftemplateService;
    @Autowired CfTemplateversionService cftemplateversionService;
    
    private final TemplateUtil templateUtil;
    private final AuthTokenList authtokenlist;
    private final SiteTreeBean sitetree;
    private final FreemarkerService freemarkerService;
    private final VelocityService velocityService;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(LayoutService.class);
    
    public LayoutService(TemplateUtil templateUtil, 
                                AuthTokenList authtokenlist,
                                SiteTreeBean sitetree,
                                FreemarkerService freemarkerService,
                                VelocityService velocityService) {
        this.templateUtil = templateUtil;
        this.authtokenlist = authtokenlist;
        this.sitetree = sitetree;
        this.freemarkerService = freemarkerService;
        this.velocityService = velocityService;
    }
    
    public String manageLayout(CfSite cfsite, String templatename, String templatecontent, String cfstylesheet, String cfjavascript, Map parametermap, ViewModus modus, RenderContext renderCtx, BeanContext btx) throws ClownfishTemplateException {
        boolean preview = false;        // is preview
        if (parametermap.containsKey("preview")) {    // check mode for display (preview or productive)
            if (parametermap.get("preview").toString().compareToIgnoreCase("true") == 0) {
                preview = true;
            }
        }
        
        String login_token = "";
        if (parametermap.containsKey("cf_login_token")) {    // check token for access manager
            login_token = parametermap.get("cf_login_token").toString();
        }
        
        CfLayout cflayout = new CfLayout(templatename);
        CfTemplate maintemplate = cftemplateService.findByName(templatename);
        templateUtil.fetchLayout(maintemplate);
        Document doc = Jsoup.parse(templatecontent);
        Elements divs = doc.getElementsByAttribute("template");
        for (Element div : divs) {
            String template = div.attr("template");
            String contents = div.attr("contents");
            String datalists = div.attr("datalists");
            String assets = div.attr("assets");
            String assetlists = div.attr("assetlists");
            String keywordlists = div.attr("keywordlists");

            CfDiv cfdiv = new CfDiv();
            cfdiv.setId(div.attr("id"));
            cfdiv.setName(div.attr("template"));
            if (!contents.isEmpty()) {
                cfdiv.getContentArray().addAll(ClownfishUtil.toList(contents.split(",")));
            }
            if (!datalists.isEmpty()) {
                cfdiv.getContentlistArray().addAll(ClownfishUtil.toList(datalists.split(",")));
            }
            if (!assets.isEmpty()) {
                cfdiv.getAssetArray().addAll(ClownfishUtil.toList(assets.split(",")));
            }
            if (!assetlists.isEmpty()) {
                cfdiv.getAssetlistArray().addAll(ClownfishUtil.toList(assetlists.split(",")));
            }
            if (!keywordlists.isEmpty()) {
                cfdiv.getKeywordlistArray().addAll(ClownfishUtil.toList(keywordlists.split(",")));
            }
            if (!template.isEmpty()) {
                CfTemplate cfdivtemplate = cftemplateService.findByName(template);
                List<CfLayoutcontent> layoutcontent = cflayoutcontentService.findBySiterefAndTemplateref(cfsite.getId(), cfdivtemplate.getId());
                long currentTemplateVersion;
                try {
                    currentTemplateVersion = cftemplateversionService.findMaxVersion((cfdivtemplate).getId());
                } catch (NullPointerException ex) {
                    currentTemplateVersion = 0;
                }
                String content = templateUtil.getVersion((cfdivtemplate).getId(), currentTemplateVersion);
                content = templateUtil.fetchIncludes(content, modus);
                content = templateUtil.replacePlaceholders(content, cfdiv, layoutcontent, preview);
                try {
                    content = interpretscript(content, cfdivtemplate, cfstylesheet, cfjavascript, parametermap, modus, renderCtx, btx);
                } catch (TemplateException ex) {
                    //LOGGER.error(ex.getMessage());
                    throw new ClownfishTemplateException(ex.getMessage());
                }
                //System.out.println(out);
                div.removeAttr("template");
                div.removeAttr("contents");
                div.removeAttr("datalists");
                div.removeAttr("assets");
                div.removeAttr("assetlists");
                div.removeAttr("keywordlists");
                
                if ((preview) && (authtokenlist.checkValidToken(login_token))) {          // ToDo check accessmanager
                    div.addClass("cf_div");
                }
                if ((preview) && (authtokenlist.checkValidToken(login_token))) {          // ToDo check accessmanager
                    //templateUtil.fetchLayout(maintemplate);
                    if ((null != btx.getSitetree()) && (null != btx.getSitetree().getLayout())) {
                        for (CfDiv comp_div : templateUtil.getLayout().getDivs()) {
                            if ((0 == cfdiv.getName().compareToIgnoreCase(comp_div.getName())) && (btx.getSitetree().getVisibleMap().get(comp_div.getId()))) {
                                div.html(content);
                            }
                        }
                    } else {
                        for (CfDiv comp_div : templateUtil.getLayout().getDivs()) {
                            if (0 == cfdiv.getName().compareToIgnoreCase(comp_div.getName())) {
                                div.html(content);
                            }
                        }
                    }
                } else {
                    div.html(content);
                }
            }
            cflayout.getDivArray().put(div.attr("id"), cfdiv);
        }
        if ((preview) && (authtokenlist.checkValidToken(login_token))) {          // ToDo check accessmanager
            doc.head().append("<script src=\"resources/js/axios.js\"></script>");
            doc.head().append("<link rel=\"stylesheet\" href=\"resources/css/cf_preview.css\">");
            doc.head().append("<link rel=\"stylesheet\" href=\"resources/css/preview_style.css\">");
            doc.head().append("<script src=\"resources/js/cf_preview.js\"></script>");
            doc.head().append("<script src=\"resources/js/preview_script.js\"></script>");
        }
        return doc.html();
    }
    
    public String interpretscript(String templatecontent, CfTemplate cftemplate, String cfstylesheet, String cfjavascript, Map parametermap, ViewModus modus, RenderContext rtx, BeanContext btx) throws TemplateException {
        StringWriter out = new StringWriter();
        
        switch (cftemplate.getScriptlanguage()) {
            case 0 -> // FREEMARKER
                freemarkerService.interpretscript(templatecontent, cftemplate, cfstylesheet, cfjavascript, parametermap, modus, rtx, btx);
            case 1 -> // VELOCITY
                velocityService.interpretscript(templatecontent, cftemplate, cfstylesheet, cfjavascript, parametermap, modus, rtx, btx);
            default -> // HTML
                out.append(templatecontent);
        }
        return out.toString();
    }
}
