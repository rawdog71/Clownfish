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

import de.destrukt.sapconnection.SAPConnection;
import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import io.clownfish.clownfish.beans.PropertyList;
import io.clownfish.clownfish.compiler.CfClassCompiler;
import io.clownfish.clownfish.constants.ClownfishConst;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.STAGING;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfSitedatasource;
import io.clownfish.clownfish.dbentities.CfSitesaprfc;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.sap.RFC_GET_FUNCTION_INTERFACE;
import io.clownfish.clownfish.sap.RPY_TABLE_READ;
import io.clownfish.clownfish.serviceimpl.CfTemplateLoaderImpl;
import io.clownfish.clownfish.serviceinterface.*;
import io.clownfish.clownfish.templatebeans.*;
import io.clownfish.clownfish.utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.*;

@Service
public class TemplateExecutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateExecutionService.class);

    @Autowired CfSiteService cfsiteService;
    @Autowired CfTemplateService cftemplateService;
    @Autowired CfTemplateLoaderImpl freemarkerTemplateloader;
    @Autowired CfTemplateversionService cftemplateversionService;
    @Autowired TemplateUtil templateUtil;
    @Autowired CfSitedatasourceService cfsitedatasourceService;
    @Autowired CfDatasourceService cfdatasourceService;
    @Autowired PropertyList propertylist;
    @Autowired CfSitesaprfcService cfsitesaprfcService;
    @Autowired CfAttributcontentService cfattributcontentService;
    @Autowired CfClasscontentService cfclasscontentService;
    @Autowired CfClassService cfclassService;
    @Autowired CfListService cflistService;
    @Autowired CfAssetlistService cfassetlistService;
    @Autowired CfAssetlistcontentService cfassetlistcontentService;
    @Autowired CfAssetService cfassetService;
    @Autowired CfListcontentService cflistcontentService;
    @Autowired PropertyUtil propertyUtil;
    @Autowired ContentUtil contentUtil;
    @Autowired MailUtil mailUtil;
    @Autowired PDFUtil pdfUtil;
    @Autowired CfClassCompiler cfclassCompiler;
    @Autowired BeanUtil beanUtil; // Sicherstellen, dass dieser über Spring verfügbar ist oder init() aufgerufen wird

    @Value("${sapconnection.file}") String SAPCONNECTION;
    @Value("${websocket.port:9001}") int websocketPort;
    @Value("${hibernate.use:0}") int useHibernate;

    /**
     * Führt ein Template basierend auf einer Site-Referenz aus.
     * @param siteref Die ID der Site
     * @param paramMap Die Parameter als Map
     * @param callerName Name des Aufrufers (z.B. Quartz-Job-Name oder Trigger-Name) für SAP
     */
    public void executeTemplate(long siteref, Map<String, String> paramMap, String callerName) {
        boolean canExecute = false;
        ClownfishConst.ViewModus modus = STAGING;
        
        Map<String, String> propertymap = propertylist.fillPropertyMap();

        freemarker.template.Template fmTemplate = null;
        Map<String, Object> fmRoot = null;
        org.apache.velocity.VelocityContext velContext = null;
        org.apache.velocity.Template velTemplate = null;

        CfSite cfsite = cfsiteService.findById(siteref);
        if (cfsite == null) {
            LOGGER.error("Site mit ID {} nicht gefunden.", siteref);
            return;
        }

        CfTemplate cftemplate = cftemplateService.findById(cfsite.getTemplateref().getId());
        if (cftemplate == null) {
            LOGGER.error("Template für Site {} nicht gefunden.", cfsite.getName());
            return;
        }

        boolean sapSupport = "true".equalsIgnoreCase(propertymap.get("sap_support"));
        SAPConnection sapc = null;
        RPY_TABLE_READ rpytableread = null;
        RFC_GET_FUNCTION_INTERFACE rfc_get_function_interface = null;
        SAPTemplateBean sapbean = null;

        if (sapSupport) {
            sapc = new SAPConnection(SAPCONNECTION, "Clownfish5_" + callerName);
            rpytableread = new RPY_TABLE_READ(sapc);
            rfc_get_function_interface = new RFC_GET_FUNCTION_INTERFACE(sapc);
        }

        // --- Template Setup ---
        switch (cftemplate.getScriptlanguage()) {
            case 0: // Freemarker
                try {
                    fmRoot = new LinkedHashMap<>();
                    freemarker.template.Configuration freemarkerCfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_29);
                    freemarkerCfg.setDefaultEncoding("UTF-8");
                    freemarkerCfg.setTemplateLoader(freemarkerTemplateloader);
                    freemarkerCfg.setLocalizedLookup(false);
                    freemarkerCfg.setLocale(Locale.GERMANY);
                    freemarkerCfg.setTagSyntax(freemarker.template.Configuration.AUTO_DETECT_TAG_SYNTAX);

                    fmTemplate = freemarkerCfg.getTemplate(cftemplate.getName());
                    canExecute = true;
                } catch (IOException ex) {
                    LOGGER.error("Freemarker Error: {}", ex.getMessage());
                }
                break;
            case 1: // Velocity
                try {
                    velContext = new org.apache.velocity.VelocityContext();
                    velTemplate = new org.apache.velocity.Template();
                    org.apache.velocity.runtime.RuntimeServices runtimeServices = org.apache.velocity.runtime.RuntimeSingleton.getRuntimeServices();
                    
                    long currentTemplateVersion;
                    try {
                        currentTemplateVersion = cftemplateversionService.findMaxVersion(cftemplate.getId());
                    } catch (NullPointerException ex) {
                        currentTemplateVersion = 0;
                    }
                    
                    String templateContent = templateUtil.getVersion(cftemplate.getId(), currentTemplateVersion);
                    templateContent = templateUtil.fetchIncludes(templateContent, modus);
                    StringReader reader = new StringReader(templateContent);
                    velTemplate.setRuntimeServices(runtimeServices);
                    velTemplate.setData(runtimeServices.parse(reader, velTemplate));
                    velTemplate.initDocument();
                    canExecute = true;
                } catch (org.apache.velocity.runtime.parser.ParseException ex) {
                    LOGGER.error("Velocity Error: {}", ex.getMessage());
                }
                break;
            default:
                canExecute = false;
                break;
        }

        // --- Ausführung ---
        if (canExecute) {
            List<CfSitedatasource> sitedatasourcelist = cfsitedatasourceService.findBySiteref(cfsite.getId());

            // --- Beans initialisieren ---
            EmailTemplateBean emailbean = new EmailTemplateBean();
            emailbean.init(propertymap, mailUtil, propertyUtil);
            
            if (sapSupport) {
                List<CfSitesaprfc> sitesaprfclist = cfsitesaprfcService.findBySiteref(cfsite.getId());
                sapbean = new SAPTemplateBean();
                sapbean.init(sapc, sitesaprfclist, rpytableread, null);
            }

            NetworkTemplateBean networkbean = new NetworkTemplateBean();
            DatabaseTemplateBean databasebean = new DatabaseTemplateBean(propertyUtil);
            databasebean.initjob(sitedatasourcelist, cfdatasourceService);
            DownloadTemplateBean downloadbean = new DownloadTemplateBean(propertyUtil);
            ContentTemplateBean contentbean = new ContentTemplateBean(propertyUtil, contentUtil);
            contentbean.init(cfclasscontentService, cfattributcontentService, cflistService, cflistcontentService, cfclassService, cfassetlistService, cfassetlistcontentService, cfassetService, useHibernate);
            ImportTemplateBean importBean = new ImportTemplateBean();
            importBean.initjob(sitedatasourcelist, cfdatasourceService);
            WebServiceTemplateBean webServiceBean = new WebServiceTemplateBean();
            WebSocketTemplateBean webSocketBean = new WebSocketTemplateBean();
            webSocketBean.setWebsocketPort(websocketPort);
            UploadTemplateBean uploadBean = new UploadTemplateBean();
            uploadBean.init(cftemplateService, propertyUtil, cfdatasourceService);
            PDFTemplateBean pdfBean = new PDFTemplateBean();
            pdfBean.initjob(pdfUtil);
            JSONatorBean jsonatorbean = new JSONatorBean();
            jsonatorbean.init(cftemplateService);
            ExternalClassProvider externalclassproviderbean = new ExternalClassProvider(cfclassCompiler);

            Writer out = new StringWriter();

            if (cftemplate.getScriptlanguage() == 0 && fmRoot != null) { // Freemarker
                fmRoot.put("emailBean", emailbean);
                if (sapSupport) fmRoot.put("sapBean", sapbean);
                fmRoot.put("databaseBean", databasebean);
                fmRoot.put("downloadBean", downloadbean);
                fmRoot.put("networkBean", networkbean);
                fmRoot.put("importBean", importBean);
                fmRoot.put("pdfBean", pdfBean);
                fmRoot.put("contentBean", contentbean);
                fmRoot.put("webserviceBean", webServiceBean);
                fmRoot.put("websocketBean", webSocketBean);
                fmRoot.put("uploadBean", uploadBean);
                fmRoot.put("jsonatorBean", jsonatorbean);
                fmRoot.put("classBean", externalclassproviderbean);
                fmRoot.put("property", propertymap);
                fmRoot.put("parameter", paramMap);

                injectDynamicBeans(fmRoot, null); // Eigene Hilfsmethode (siehe unten)

                try {
                    if (fmTemplate != null) {
                        freemarker.core.Environment env = fmTemplate.createProcessingEnvironment(fmRoot, out);
                        env.process();
                    }
                } catch (Exception ex) {
                    LOGGER.error("Fehler beim Freemarker Processing: {}", ex.getMessage());
                }

            } else if (velContext != null) { // Velocity
                velContext.put("emailBean", emailbean);
                if (sapSupport) velContext.put("sapBean", sapbean);
                velContext.put("databaseBean", databasebean);
                velContext.put("downloadBean", downloadbean);
                velContext.put("networkBean", networkbean);
                velContext.put("importBean", importBean);
                velContext.put("webserviceBean", webServiceBean);
                velContext.put("websocketBean", webSocketBean);
                velContext.put("uploadBean", uploadBean);
                velContext.put("pdfBean", pdfBean);
                velContext.put("jsonatorBean", jsonatorbean);
                velContext.put("classBean", externalclassproviderbean);
                velContext.put("contentBean", contentbean);
                velContext.put("parameter", paramMap);
                velContext.put("property", propertymap);

                injectDynamicBeans(null, velContext); // Eigene Hilfsmethode

                try {
                    if (velTemplate != null) {
                        velTemplate.merge(velContext, out);
                    }
                } catch (Exception ex) {
                    LOGGER.error("Fehler beim Velocity Processing: {}", ex.getMessage());
                }
            }
            LOGGER.info(out.toString());
        } else {
            LOGGER.info("CANNOT EXECUTE HTML TEMPLATE");
        }
    }

    /**
     * Hilfsmethode, um den Code für das dynamische Laden von Beans nicht doppelt schreiben zu müssen.
     */
    private void injectDynamicBeans(Map<String, Object> fmRoot, org.apache.velocity.VelocityContext velContext) {
        if (beanUtil != null && beanUtil.getLoadabletemplatebeans() != null) {
            for (Class<?> tpbc : beanUtil.getLoadabletemplatebeans()) {
                try {
                    Constructor<?> ctor = tpbc.getConstructor();
                    Object object = ctor.newInstance();
                    String key = tpbc.getName().replaceAll("\\.", "_");
                    if (fmRoot != null) fmRoot.put(key, object);
                    if (velContext != null) velContext.put(key, object);
                } catch (Exception ex) {
                    LOGGER.error("Fehler beim Laden von LoadableTemplateBean: {}", ex.getMessage());
                }
            }
        }

        if (cfclassCompiler != null && cfclassCompiler.getClassMethodMap() != null) {
            cfclassCompiler.getClassMethodMap().forEach((k, v) -> {
                try {
                    Constructor<?> ctor = k.getConstructor();
                    Object object = ctor.newInstance();
                    String key = k.getName().replaceAll("\\.", "_");
                    if (fmRoot != null) fmRoot.put(key, object);
                    if (velContext != null) velContext.put(key, object);
                } catch (Exception ex) {
                    LOGGER.error("Fehler beim Laden von ClassMethodMap Bean: {}", ex.getMessage());
                }
            });
        }
    }
}