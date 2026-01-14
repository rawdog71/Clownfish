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
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
import io.clownfish.clownfish.utils.BeanUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import io.clownfish.clownfish.utils.TemplateUtil;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 *
 * @author SulzbachR
 */
@Service
class VelocityService {
    private final BeanUtil beanUtil;
    private final CfClassCompiler cfclassCompiler;
    private final PropertyUtil propertyUtil;
    private final CfTemplateversionService versionService;
    private final TemplateUtil templateUtil;
    private final AuthTokenList authtokenlist;
    private ViewModus modus;
    private final LayoutService layoutService;
    
    
    //@Autowired CfLayoutcontentService cflayoutcontentService;
    //@Autowired CfTemplateService cftemplateService;
    //@Autowired CfTemplateversionService cftemplateversionService;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(VelocityService.class);

    // Constructor Injection: Diese Abhängigkeiten verschwinden aus der Main-Klasse!
    public VelocityService(CfTemplateversionService versionService, 
                            TemplateUtil templateUtil,
                            BeanUtil beanUtil,
                            PropertyUtil propertyUtil,
                            CfClassCompiler cfclassCompiler,
                            AuthTokenList authtokenlist,
                            @Lazy LayoutService layoutService) {
        this.versionService = versionService;
        this.beanUtil = beanUtil;
        this.cfclassCompiler = cfclassCompiler;
        this.propertyUtil = propertyUtil;
        this.templateUtil = templateUtil;
        this.authtokenlist = authtokenlist;
        this.layoutService = layoutService;
    }

    public TemplateEngineResult init(CfTemplate cftemplate, ViewModus modus) {
        TemplateEngineResult result = new TemplateEngineResult();
        this.modus = modus;
        
        result.setVelContext(new VelocityContext());
        Template vTemplate = new Template();
        RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
        
        // Content-Logik kapseln
        String templateContent = fetchTemplateContent(cftemplate, modus);
        
        // Includes auflösen
        templateContent = templateUtil.fetchIncludes(templateContent, modus);
        
        StringReader reader = new StringReader(templateContent);
        vTemplate.setRuntimeServices(runtimeServices);
        
        try {
            vTemplate.setData(runtimeServices.parse(reader, vTemplate));
            vTemplate.initDocument();
            result.setVelTemplate(vTemplate);
            result.setScripted(true);
        } catch (TemplateInitException | ParseException e) {
            // Logging
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
        VelocityContext velContext = new VelocityContext();
        StringWriter out = new StringWriter();
        
        Template velTemplate = new Template();
        RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
        
        // Content-Logik kapseln
        String templateContent = fetchTemplateContent(cftemplate, modus);
        
        // Includes auflösen
        templateContent = templateUtil.fetchIncludes(templateContent, modus);
        
        StringReader reader = new StringReader(templateContent);
        velTemplate.setRuntimeServices(runtimeServices);
        
        try {
            velTemplate.setData(runtimeServices.parse(reader, velTemplate));
            velTemplate.initDocument();
            //result.setVelTemplate(vTemplate);
            //result.setScripted(true);
        } catch (TemplateInitException | ParseException e) {
            // Logging
        }
        
        if (null != velContext) {
            velContext.put("css", cfstylesheet);
            velContext.put("js", cfjavascript);
            velContext.put("sitecontent", renderCtx.getSitecontentmap());
            velContext.put("metainfo", renderCtx.getMetainfomap());
            
            velContext.put("emailBean", btx.getEmailbean());
            boolean sapSupport = propertyUtil.getPropertyBoolean("sap_support", false);
            if (sapSupport) {
                velContext.put("sapBean", btx.getSapbean());
            }
            velContext.put("databaseBean", btx.getDatabasebean());
            velContext.put("downloadBean", btx.getDatabasebean());
            velContext.put("importBean", btx.getImportbean());
            velContext.put("networkBean", btx.getNetworkbean());
            velContext.put("webserviceBean", btx.getWebservicebean());
            velContext.put("websocketBean", btx.getWebsocketbean());
            velContext.put("uploadBean", btx.getUploadbean());
            velContext.put("pdfBean", btx.getPdfbean());
            velContext.put("classBean", btx.getExternalclassproviderbean());
            velContext.put("contentBean", btx.getContentbean());
            velContext.put("jsonatorBean", btx.getJsonatorbean());

            velContext.put("parameter", renderCtx.getParametermap());
            velContext.put("property", propertyUtil.getPropertymap());
            if (!renderCtx.getSearchmetadata().isEmpty()) {
                velContext.put("searchmetadata", renderCtx.getSearchmetadata());
            }
            if (!renderCtx.getSearchcontentmap().isEmpty()) {
                velContext.put("searchcontentlist", renderCtx.getSearchcontentmap());
            }
            if (!renderCtx.getSearchassetmap().isEmpty()) {
                velContext.put("searchassetlist", renderCtx.getSearchassetmap());
            }
            if (!renderCtx.getSearchassetmetadatamap().isEmpty()) {
                velContext.put("searchassetmetadatalist", renderCtx.getSearchassetmetadatamap());
            }
            if (!renderCtx.getSearchclasscontentmap().isEmpty()) {
                velContext.put("searchclasscontentlist", renderCtx.getSearchclasscontentmap());
            }

            for (Class tpbc : beanUtil.getLoadabletemplatebeans()) {
                Constructor<?> ctor;
                try {
                    ctor = tpbc.getConstructor();
                    Object object = ctor.newInstance(new Object[] { });
                    velContext.put(tpbc.getName().replaceAll("\\.", "_"), object);
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }

            org.apache.velocity.VelocityContext finalvelContext = velContext;
            cfclassCompiler.getClassMethodMap().forEach((k, v) ->
            {
                Constructor<?> ctor;
                try
                {
                    ctor = k.getConstructor();
                    Object object = ctor.newInstance();
                    finalvelContext.put(k.getName().replaceAll("\\.", "_"), object);
                }
                catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
                {
                    LOGGER.error(ex.getMessage());
                }
            });

            //Template velTemplate = new Template();
            if (null != velTemplate) {
                if (1 == cftemplate.getType()) {
                    try {
                        String output = layoutService.manageLayout(site, cftemplate.getName(), cftemplate.getContent(), cfstylesheet, cfjavascript, renderCtx.getParametermap(), modus, renderCtx, btx);
                        output = interpretscript(output, cftemplate, cfstylesheet, cfjavascript, renderCtx.getParametermap(), modus, renderCtx, btx);
                        out.write(output);
                    } catch (TemplateException | ClownfishTemplateException ex) {
                        //LOGGER.error(ex.getMessage());
                        cfResponse.setErrorcode(5);
                        cfResponse.setOutput("ClownfishTemplateException");
                        cfResponse.setRelocation(propertyUtil.getPropertyValue("site_error"));
                        return cfResponse;
                    }
                } else {
                    velTemplate.merge(velContext, out);
                    // Erfolg
                    cfResponse.setErrorcode(0);
                    cfResponse.setOutput(out.toString());
                }
            }
        }
        return cfResponse;
    }

    // Hilfsmethode (private), um den Code lesbar zu halten
    private String fetchTemplateContent(CfTemplate cftemplate, ViewModus modus) {
        if (ViewModus.DEVELOPMENT == modus) {
            return cftemplate.getContent();
        } else {
            long currentVersion = 0;
            try {
                currentVersion = versionService.findMaxVersion(cftemplate.getId());
            } catch (NullPointerException ex) {
                // ignore
            }
            return templateUtil.getVersion(cftemplate.getId(), currentVersion);
        }
    }
    
    public String interpretscript(String templatecontent, CfTemplate cftemplate, String cfstylesheet, String cfjavascript, Map parametermap, ViewModus modus, RenderContext renderCtx, BeanContext btx) throws TemplateException {
        StringWriter out = new StringWriter();
        try {
            VelocityContext velContext = null;
            Template velTemplate = null;
            
            velContext = new VelocityContext();
            
            velTemplate = new Template();
            RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
            templatecontent = templateUtil.fetchIncludes(templatecontent, modus);
            StringReader reader = new StringReader(templatecontent);
            velTemplate.setRuntimeServices(runtimeServices);
            velTemplate.setData(runtimeServices.parse(reader, velTemplate));
            velTemplate.initDocument();
            
            if (null != velContext) {
                velContext.put("css", cfstylesheet);
                velContext.put("js", cfjavascript);
                velContext.put("sitecontent", renderCtx.getSitecontentmap());
                velContext.put("metainfo", renderCtx.getMetainfomap());
                
                velContext.put("emailBean", btx.getEmailbean());
                boolean sapSupport = propertyUtil.getPropertyBoolean("sap_support", false);
                if (sapSupport) {
                    velContext.put("sapBean", btx.getSapbean());
                }
                velContext.put("databaseBean", btx.getDatabasebean());
                velContext.put("importBean", btx.getImportbean());
                velContext.put("networkBean", btx.getNetworkbean());
                velContext.put("webserviceBean", btx.getWebservicebean());
                velContext.put("contentBean", btx.getContentbean());
                velContext.put("pdfBean", btx.getPdfbean());
                velContext.put("classBean", btx.getExternalclassproviderbean());
                
                velContext.put("parameter", parametermap);
                velContext.put("property", propertyUtil.getPropertymap());
                if (!renderCtx.getSearchmetadata().isEmpty()) {
                    velContext.put("searchmetadata", renderCtx.getSearchmetadata());
                }
                if (!renderCtx.getSearchcontentmap().isEmpty()) {
                    velContext.put("searchcontentlist", renderCtx.getSearchcontentmap());
                }
                if (!renderCtx.getSearchassetmap().isEmpty()) {
                    velContext.put("searchassetlist", renderCtx.getSearchassetmap());
                }
                if (!renderCtx.getSearchassetmetadatamap().isEmpty()) {
                    velContext.put("searchassetmetadatalist", renderCtx.getSearchassetmetadatamap());
                }
                if (!renderCtx.getSearchclasscontentmap().isEmpty()) {
                    velContext.put("searchclasscontentlist", renderCtx.getSearchclasscontentmap());
                }
                
                for (Class tpbc : beanUtil.getLoadabletemplatebeans()) {
                    Constructor<?> ctor;
                    try {
                        ctor = tpbc.getConstructor();
                        Object object = ctor.newInstance(new Object[] { });
                        velContext.put(tpbc.getName().replaceAll("\\.", "_"), object);
                    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                }
                
                org.apache.velocity.VelocityContext finalvelContext = velContext;
                cfclassCompiler.getClassMethodMap().forEach((k, v) ->
                {
                    Constructor<?> ctor;
                    try
                    {
                        ctor = k.getConstructor();
                        Object object = ctor.newInstance();
                        finalvelContext.put(k.getName().replaceAll("\\.", "_"), object);
                    }
                    catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
                    {
                        LOGGER.error(ex.getMessage());
                    }
                });
                
                if (null != velTemplate) {
                    velTemplate.merge(velContext, out);
                }
            }
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(VelocityService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return out.toString();
    }
}