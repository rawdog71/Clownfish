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

import de.destrukt.sapconnection.SAPConnection;
import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import io.clownfish.clownfish.beans.PropertyList;
import io.clownfish.clownfish.beans.QuartzList;
//import static io.clownfish.clownfish.beans.SiteTreeBean.SAPCONNECTION;
import io.clownfish.clownfish.constants.ClownfishConst;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.STAGING;
import io.clownfish.clownfish.dbentities.CfQuartz;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfSitedatasource;
import io.clownfish.clownfish.dbentities.CfSitesaprfc;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.sap.RFC_GET_FUNCTION_INTERFACE;
import io.clownfish.clownfish.sap.RPY_TABLE_READ;
import io.clownfish.clownfish.serviceimpl.CfTemplateLoaderImpl;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
import io.clownfish.clownfish.serviceinterface.CfAssetlistcontentService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import io.clownfish.clownfish.serviceinterface.CfListService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.serviceinterface.CfSitedatasourceService;
import io.clownfish.clownfish.serviceinterface.CfSitesaprfcService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
import io.clownfish.clownfish.templatebeans.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author sulzbachr
 */
public class QuartzJob implements Job {
    @Autowired QuartzList quartzlist;
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
    private @Getter @Setter BeanUtil beanUtil;
    private @Getter @Setter List<CfSitedatasource> sitedatasourcelist;
    private @Getter @Setter Map<String, String> propertymap = null;
    private boolean sapSupport = false;
    private freemarker.template.Configuration freemarkerCfg;
    private SAPTemplateBean sapbean;
    private static SAPConnection sapc = null;
    private RPY_TABLE_READ rpytableread = null;
    private RFC_GET_FUNCTION_INTERFACE rfc_get_function_interface = null;
    private ClownfishConst.ViewModus modus = STAGING;
    final transient Logger LOGGER = LoggerFactory.getLogger(QuartzJob.class);
    @Value("${sapconnection.file}") String SAPCONNECTION;
    @Value("${websocket.port:9001}") int websocketPort;
    @Value("${hibernate.use:0}") int useHibernate;
    
    @PostConstruct
    public void init() {
        try {
            String libloaderpath = propertyUtil.getPropertyValue("folder_libs");
            if ((null == beanUtil) && (null != libloaderpath)) {
                beanUtil = new BeanUtil();
                if ((!libloaderpath.isBlank()) && (null != libloaderpath)) 
                    beanUtil.init(libloaderpath);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        List<CfQuartz> joblist = quartzlist.getQuartzlist();
        joblist.stream().filter((quartz) -> (quartz.getName().compareToIgnoreCase(jec.getJobDetail().getKey().getName()) == 0)).map((quartz) -> {
            LOGGER.info("JOB CLOWNFISH CMS: " + quartz.getName() + " - " + quartz.getSiteRef());
            return quartz;
        }).forEach((quartz) -> callJob(quartz, quartz.getSiteRef().longValue()));
    }

    private Map<String, String> makeParamMap(String params) {
        Map<String, String> paramMap = new HashMap<>();
        if (!params.isBlank()) {
            for (String param : params.split("&")) {
                String[] val = param.split("=");
                paramMap.put(val[0], val[1]);
            }
        }
        return paramMap;
    }
    
    private void callJob(CfQuartz quartz, long siteref) {
        boolean canExecute = false;
        modus = STAGING;    // 1 = Staging mode (fetch sourcecode from commited repository) <= default
        // read all System Properties of the property table
        propertymap = propertylist.fillPropertyMap();
        Map<String, String> paramMap = makeParamMap(quartz.getParameter());
        //clownfishutil = new ClownfishUtil();
        
        // Freemarker Template
        freemarker.template.Template fmTemplate = null;
        Map fmRoot = null;

        // Velocity Template
        org.apache.velocity.VelocityContext velContext = null;
        org.apache.velocity.Template velTemplate = null;

        // fetch site by name or aliasname
        CfSite cfsite;
        cfsite = cfsiteService.findById(siteref);

        CfTemplate cftemplate = cftemplateService.findById(cfsite.getTemplateref().getId());
        
        String sapSupportProp = propertymap.get("sap_support");
        if (sapSupportProp.compareToIgnoreCase("true") == 0) {
            sapSupport = true;
        }
        if (sapSupport) {
            sapc = new SAPConnection(SAPCONNECTION, "Clownfish5_" + quartz.getName());
            rpytableread = new RPY_TABLE_READ(sapc);
            rfc_get_function_interface = new RFC_GET_FUNCTION_INTERFACE(sapc);
        }
        
        // fetch the dependend template 
        switch (cftemplate.getScriptlanguage()) {
            case 0:
                try {
                    // Freemarker Template
                    fmRoot = new LinkedHashMap();

                    freemarkerCfg = new freemarker.template.Configuration();
                    freemarkerCfg.setDefaultEncoding("UTF-8");
                    freemarkerCfg.setTemplateLoader(freemarkerTemplateloader);
                    freemarkerCfg.setLocalizedLookup(false);
                    freemarkerCfg.setLocale(Locale.GERMANY);
                    freemarkerCfg.setTagSyntax(freemarker.template.Configuration.AUTO_DETECT_TAG_SYNTAX);

                    fmTemplate = freemarkerCfg.getTemplate(cftemplate.getName());
                    canExecute = true;
                } catch (MalformedTemplateNameException ex) {
                    LOGGER.error(ex.getMessage());
                } catch (ParseException ex) {
                    LOGGER.error(ex.getMessage());
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage());
                }
                break;
            case 1:
                try {
                    // Velocity Template
                    velContext = new org.apache.velocity.VelocityContext();
                    velTemplate = new org.apache.velocity.Template();
                    org.apache.velocity.runtime.RuntimeServices runtimeServices = org.apache.velocity.runtime.RuntimeSingleton.getRuntimeServices();
                    String templateContent;
                    long currentTemplateVersion;
                    try {
                        currentTemplateVersion = cftemplateversionService.findMaxVersion(cftemplate.getId());
                    } catch (NullPointerException ex) {
                        currentTemplateVersion = 0;
                    }
                    templateContent = templateUtil.getVersion(cftemplate.getId(), currentTemplateVersion);
                    templateContent = templateUtil.fetchIncludes(templateContent, modus);
                    StringReader reader = new StringReader(templateContent);
                    velTemplate.setRuntimeServices(runtimeServices);
                    velTemplate.setData(runtimeServices.parse(reader, velTemplate));
                    velTemplate.initDocument();
                    canExecute = true;
                } catch (org.apache.velocity.runtime.parser.ParseException ex) {
                    LOGGER.error(ex.getMessage());
                }
                break;
            default:
                canExecute = false;
                break;
        }

        if (canExecute) {
            // fetch the dependend datasources
            sitedatasourcelist = new ArrayList<>();
            sitedatasourcelist.addAll(cfsitedatasourceService.findBySiteref(cfsite.getId()));
            
            // Instantiate Template Beans
            
            EmailTemplateBean emailbean = new EmailTemplateBean();
            emailbean.init(propertymap, mailUtil, propertyUtil);
            if (sapSupport) {
                List<CfSitesaprfc> sitesaprfclist = new ArrayList<>();
                sitesaprfclist.addAll(cfsitesaprfcService.findBySiteref(cfsite.getId()));
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
            PDFTemplateBean pdfBean = new PDFTemplateBean();
            pdfBean.initjob(pdfUtil);

            // write the output
            Writer out = new StringWriter();
            if (0 == cftemplate.getScriptlanguage()) {  // Freemarker template
                if (null != fmRoot) {
                    fmRoot.put("emailBean", emailbean);

                    if (sapSupport) {
                        List<CfSitesaprfc> sitesaprfclist = new ArrayList<>();
                        sitesaprfclist.addAll(cfsitesaprfcService.findBySiteref(cfsite.getId()));
                        sapbean = new SAPTemplateBean();
                        sapbean.init(sapc, sitesaprfclist, rpytableread, null);
                        fmRoot.put("sapBean", sapbean);
                    }

                    fmRoot.put("databaseBean", databasebean);
                    fmRoot.put("downloadBean", downloadbean);
                    fmRoot.put("networkBean", networkbean);
                    fmRoot.put("importBean", importBean);
                    fmRoot.put("pdfBean", pdfBean);
                    fmRoot.put("contentBean", contentbean);
                    fmRoot.put("webserviceBean", webServiceBean);
                    fmRoot.put("websocketBean", webSocketBean);
                    fmRoot.put("uploadBean", uploadBean);
                    fmRoot.put("property", propertymap);
                    fmRoot.put("parameter", paramMap);
                    
                    for (Class tpbc : beanUtil.getLoadabletemplatebeans()) {
                        Constructor<?> ctor;
                        try {
                            ctor = tpbc.getConstructor();
                            Object object = ctor.newInstance(new Object[] { });
                            fmRoot.put(tpbc.getName().replaceAll("\\.", "_"), object);
                        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            LOGGER.error(ex.getMessage());
                        }
                    }
                    
                    try {
                        if (null != fmTemplate) {
                            freemarker.core.Environment env = fmTemplate.createProcessingEnvironment(fmRoot, out);
                            env.process();
                        }
                    } catch (freemarker.template.TemplateException ex) {
                        LOGGER.error(ex.getMessage());
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                }
            } else {                                    // Velocity template
                if (null != velContext) {
                    velContext.put("emailBean", emailbean);
                    if (sapSupport) {
                        List<CfSitesaprfc> sitesaprfclist = new ArrayList<>();
                        sitesaprfclist.addAll(cfsitesaprfcService.findBySiteref(cfsite.getId()));
                        sapbean = new SAPTemplateBean();
                        velContext.put("sapBean", sapbean);
                    }
                    velContext.put("databaseBean", databasebean);
                    velContext.put("downloadBean", downloadbean);
                    velContext.put("networkBean", networkbean);
                    velContext.put("importBean", importBean);
                    velContext.put("webserviceBean", webServiceBean);
                    velContext.put("websocketBean", webSocketBean);
                    velContext.put("uploadBean", uploadBean);
                    velContext.put("pdfBean", pdfBean);
                    velContext.put("contentBean", contentbean);
                    velContext.put("parameter", paramMap);
                    
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

                    velContext.put("property", propertymap);
                    if (null != velTemplate) {
                        velTemplate.merge(velContext, out);
                    }
                }
            }
            LOGGER.info(out.toString());
        } else {
            LOGGER.info("CANNOT EXECUTE HTML TEMPLATE");
        }
    }
    
}
