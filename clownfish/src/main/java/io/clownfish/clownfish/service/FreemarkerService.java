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

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.clownfish.clownfish.compiler.CfClassCompiler;
import io.clownfish.clownfish.constants.ClownfishConst.ViewModus;
import io.clownfish.clownfish.datamodels.AuthTokenList;
import io.clownfish.clownfish.datamodels.BeanContext;
import io.clownfish.clownfish.datamodels.ClownfishResponse;
import io.clownfish.clownfish.datamodels.RenderContext;
import io.clownfish.clownfish.datamodels.TemplateEngineResult;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.exceptions.ClownfishTemplateException;
import io.clownfish.clownfish.serviceimpl.CfStringTemplateLoaderImpl;
import io.clownfish.clownfish.serviceimpl.CfTemplateLoaderImpl;
import io.clownfish.clownfish.utils.BeanUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 *
 * @author SulzbachR
 */
@Service
class FreemarkerService {
    private final CfTemplateLoaderImpl freemarkerTemplateloader;
    private final BeanUtil beanUtil;
    private final CfClassCompiler cfclassCompiler;
    private final PropertyUtil propertyUtil;
    //private final TemplateUtil templateUtil;
    private ViewModus modus;
    private final AuthTokenList authtokenlist;
    private final Configuration freemarkerCfg = new Configuration();
    private final LayoutService layoutService;
    
    @Autowired CfStringTemplateLoaderImpl freemarkerStringTemplateloader;
    //@Autowired CfLayoutcontentService cflayoutcontentService;
    //@Autowired CfTemplateService cftemplateService;
    //@Autowired CfTemplateversionService cftemplateversionService;
    

    final transient Logger LOGGER = LoggerFactory.getLogger(FreemarkerService.class);
   
    // Constructor Injection
    public FreemarkerService(CfTemplateLoaderImpl freemarkerTemplateloader, 
                                BeanUtil beanUtil,
                                CfClassCompiler cfclassCompiler,
                                PropertyUtil propertyUtil,
                                //TemplateUtil templateUtil,
                                AuthTokenList authtokenlist,
                                @Lazy LayoutService layoutService) {
        this.freemarkerTemplateloader = freemarkerTemplateloader;
        this.beanUtil = beanUtil;
        this.cfclassCompiler = cfclassCompiler;
        this.propertyUtil = propertyUtil;
        this.authtokenlist = authtokenlist;
        //this.templateUtil = templateUtil;
        this.layoutService = null;
    }

    public TemplateEngineResult init(CfTemplate cftemplate, ViewModus modus) {
        this.modus = modus;
        TemplateEngineResult result = new TemplateEngineResult();
        
        result.setFmRoot(new LinkedHashMap());
        freemarkerTemplateloader.setModus(modus);
        
        freemarkerCfg.setDefaultEncoding("UTF-8");
        freemarkerCfg.setTemplateLoader(freemarkerTemplateloader);
        freemarkerCfg.setLocalizedLookup(false);
        freemarkerCfg.setLocale(Locale.GERMANY);
        freemarkerCfg.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);

        result.setFreemarkerCfg(freemarkerCfg);
        try {
            result.setFmTemplate(freemarkerCfg.getTemplate(cftemplate.getName()));
            result.setScripted(true);
        } catch (IOException e) {
            // Logging hier einfügen
        }
        
        return result;
    }
    
    public ClownfishResponse populateAndRender(TemplateEngineResult engineResult,
                                               RenderContext renderCtx,
                                               CfTemplate cftemplate,
                                               Map<String, Object> templateBeans,
                                               String cfstylesheet,
                                               String cfjavascript,
                                               BeanContext btx,
                                               CfSite site) {
        
        ClownfishResponse cfResponse = new ClownfishResponse();
        StringWriter out = new StringWriter();
        Map fmRoot = engineResult.getFmRoot();
        Template fmTemplate = engineResult.getFmTemplate();

        if (null != fmRoot) {
            // 1. Statische Ressourcen & Content Maps
            fmRoot.put("css", cfstylesheet);
            fmRoot.put("js", cfjavascript);
            fmRoot.put("sitecontent", renderCtx.getSitecontentmap()); // Achtung: Namen prüfen (sitecontentmap vs searchcontentmap im Context)
            fmRoot.put("metainfo", renderCtx.getMetainfomap()); // Annahme: im Context vorhanden
            fmRoot.put("property", propertyUtil.getPropertymap());

            // 2. Standard Template Beans aus der Map übertragen
            // (emailBean, databaseBean, etc. sollten vom PageRenderingService in diese Map gepackt werden)
            if (templateBeans != null) {
                fmRoot.putAll(templateBeans);
            }

            // 3. Parameter Map
            fmRoot.put("parameter", renderCtx.getParametermap());

            // 4. Suchergebnisse (Maps) aus dem RenderContext
            if (!renderCtx.getSearchassetmetadatamap().isEmpty()) {
                fmRoot.put("searchmetadata", renderCtx.getSearchassetmetadatamap());
            }
            if (!renderCtx.getSearchcontentmap().isEmpty()) {
                fmRoot.put("searchcontentlist", renderCtx.getSearchcontentmap());
            }
            if (!renderCtx.getSearchassetmap().isEmpty()) {
                fmRoot.put("searchassetlist", renderCtx.getSearchassetmap());
            }
            if (!renderCtx.getSearchassetmetadatamap().isEmpty()) {
                fmRoot.put("searchassetmetadatalist", renderCtx.getSearchassetmetadatamap());
            }
            if (!renderCtx.getSearchclasscontentmap().isEmpty()) {
                fmRoot.put("searchclasscontentlist", renderCtx.getSearchclasscontentmap());
            }

            // 5. Dynamische Beans via Reflection (BeanUtil)
            for (Class<?> tpbc : beanUtil.getLoadabletemplatebeans()) {
                try {
                    Constructor<?> ctor = tpbc.getConstructor();
                    Object object = ctor.newInstance();
                    fmRoot.put(tpbc.getName().replaceAll("\\.", "_"), object);
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }

            // 6. Dynamische Klassen via Compiler (CfClassCompiler)
            Map finalFmRoot = fmRoot;
            cfclassCompiler.getClassMethodMap().forEach((k, v) -> {
                try {
                    Constructor<?> ctor = k.getConstructor();
                    Object object = ctor.newInstance();
                    finalFmRoot.put(k.getName().replaceAll("\\.", "_"), object);
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    LOGGER.error(ex.getMessage());
                }
            });

            // 7. Template Verarbeitung & Layout
            try {
                if (null != fmTemplate) {
                    if (1 == cftemplate.getType()) {
                        // LAYOUT MODUS
                        try {
                            // manageLayout und interpretscript müssen nun über den layoutService aufgerufen werden
                            String output = layoutService.manageLayout(site, 
                                                            cftemplate.getName(), 
                                                            cftemplate.getContent(), 
                                                            cfstylesheet, 
                                                            cfjavascript, 
                                                            renderCtx.getParametermap(), modus, renderCtx, btx);
                                                                     
                            // Hier müsste interpretscript idealerweise auch refactored werden, 
                            // da es wieder Template-Logik enthält. Fürs Erste nehmen wir an, es ist im LayoutService.
                            output = interpretscript(output, cftemplate, cfstylesheet, cfjavascript, renderCtx.getParametermap(), modus, renderCtx, btx);
                            
                            out.write(output);
                        } catch (ClownfishTemplateException ex) {
                            cfResponse.setErrorcode(5);
                            cfResponse.setOutput("ClownfishTemplateException");
                            cfResponse.setRelocation(propertyUtil.getPropertyValue("site_error"));
                            return cfResponse;
                        }
                    } else {
                        // NORMALER MODUS
                        freemarker.core.Environment env = fmTemplate.createProcessingEnvironment(fmRoot, out);
                        env.process();
                    }
                }
                
                // Erfolg
                cfResponse.setErrorcode(0);
                cfResponse.setOutput(out.toString());

            } catch (freemarker.template.TemplateException ex) {
                // Erweitertes Logging aus dem Originalcode
                System.out.println(renderCtx.getName());
                // ... Parameter Logging ...
                LOGGER.error(ex.getMessage());
                
                cfResponse.setErrorcode(1);
                cfResponse.setOutput(ex.getMessage());
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
                cfResponse.setErrorcode(1);
                cfResponse.setOutput(ex.getMessage());
            }
        }
        
        return cfResponse;
    }
    
    public String interpretscript(String templatecontent, CfTemplate cftemplate, String cfstylesheet, String cfjavascript, Map parametermap, ViewModus modus, RenderContext renderCtx, BeanContext btx) throws TemplateException {
        StringWriter out = new StringWriter();
        try {
            Template fmTemplate = null;
            Map fmRoot = null;
            
            freemarkerStringTemplateloader.setContent(templatecontent);
            freemarkerStringTemplateloader.putTemplate(cftemplate.getName(), templatecontent);
            
            freemarkerStringTemplateloader.setModus(modus);
            
            //freemarkerCfg = new freemarker.template.Configuration();
            freemarkerCfg.setDefaultEncoding("UTF-8");
            freemarkerCfg.setTemplateLoader(freemarkerStringTemplateloader);
            freemarkerCfg.setLocalizedLookup(false);
            freemarkerCfg.setLocale(Locale.GERMANY);
            freemarkerCfg.setTagSyntax(freemarker.template.Configuration.AUTO_DETECT_TAG_SYNTAX);
            
            fmTemplate = freemarkerCfg.getTemplate(cftemplate.getName());
            
            fmRoot = new LinkedHashMap();
            if (null != fmRoot) {
                fmRoot.put("css", cfstylesheet);
                fmRoot.put("js", cfjavascript);
                fmRoot.put("sitecontent", renderCtx.getSitecontentmap());
                fmRoot.put("metainfo", renderCtx.getMetainfomap());
                fmRoot.put("property", propertyUtil.getPropertymap());
                
                fmRoot.put("emailBean", btx.getEmailbean());
                boolean sapSupport = propertyUtil.getPropertyBoolean("sap_support", false);
                if (sapSupport) {
                    fmRoot.put("sapBean", btx.getSapbean());
                }
                fmRoot.put("databaseBean", btx.getDatabasebean());
                fmRoot.put("importBean", btx.getImportbean());
                fmRoot.put("networkBean", btx.getNetworkbean());
                fmRoot.put("webserviceBean", btx.getWebservicebean());
                fmRoot.put("pdfBean", btx.getPdfbean());
                fmRoot.put("contentBean", btx.getContentbean());
                fmRoot.put("classBean", btx.getExternalclassproviderbean());
                
                fmRoot.put("parameter", parametermap);
                if (!renderCtx.getSearchmetadata().isEmpty()) {
                    fmRoot.put("searchmetadata", renderCtx.getSearchmetadata());
                }
                if (!renderCtx.getSearchcontentmap().isEmpty()) {
                    fmRoot.put("searchcontentlist", renderCtx.getSearchcontentmap());
                }
                if (!renderCtx.getSearchassetmap().isEmpty()) {
                    fmRoot.put("searchassetlist", renderCtx.getSearchassetmap());
                }
                if (!renderCtx.getSearchassetmetadatamap().isEmpty()) {
                    fmRoot.put("searchassetmetadatalist", renderCtx.getSearchassetmetadatamap());
                }
                if (!renderCtx.getSearchclasscontentmap().isEmpty()) {
                    fmRoot.put("searchclasscontentlist", renderCtx.getSearchclasscontentmap());
                }
                
                for (Class<?> tpbc : beanUtil.getLoadabletemplatebeans()) {
                    Constructor<?> ctor;
                    try {
                        ctor = tpbc.getConstructor();
                        Object object = ctor.newInstance();
                        fmRoot.put(tpbc.getName().replaceAll("\\.", "_"), object);
                    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                }
                
                Map finalFmRoot = fmRoot;
                cfclassCompiler.getClassMethodMap().forEach((k, v) ->
                {
                    Constructor<?> ctor;
                    try
                    {
                        ctor = k.getConstructor();
                        Object object = ctor.newInstance();
                        finalFmRoot.put(k.getName().replaceAll("\\.", "_"), object);
                    }
                    catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
                    {
                        LOGGER.error(ex.getMessage());
                    }
                });
                
                freemarker.core.Environment env = fmTemplate.createProcessingEnvironment(fmRoot, out);
                try {
                    if (null != fmTemplate) {
                        env.process();
                    }
                } catch (freemarker.template.TemplateException ex) {
                    //LOGGER.error(ex.getMessage());
                    System.out.println(templatecontent);
                    System.out.println(cftemplate.getName());
                    System.out.println(cfstylesheet);
                    System.out.println(cfjavascript);
                    System.out.println(parametermap.toString());
                    throw new TemplateException(ex.getMessage(), env);
                } catch (IOException ex) {
                    System.out.println(templatecontent);
                    System.out.println(cftemplate.getName());
                    System.out.println(cfstylesheet);
                    System.out.println(cfjavascript);
                    System.out.println(parametermap.toString());
                    LOGGER.error(ex.getMessage());
                }
            }
        } catch (MalformedTemplateNameException ex) {
            java.util.logging.Logger.getLogger(FreemarkerService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(FreemarkerService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(FreemarkerService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return out.toString();
    }   
}
