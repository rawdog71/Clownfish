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
package io.clownfish.clownfish;

import de.destrukt.sapconnection.SAPConnection;
import io.clownfish.clownfish.beans.MavenList;
import io.clownfish.clownfish.beans.PropertyList;
import io.clownfish.clownfish.beans.QuartzList;
import io.clownfish.clownfish.beans.ServiceStatus;
import io.clownfish.clownfish.compiler.CfClassCompiler;
import io.clownfish.clownfish.compiler.CfClassLoader;
import io.clownfish.clownfish.constants.ClownfishConst;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.STAGING;
import io.clownfish.clownfish.datamodels.AuthTokenList;
import io.clownfish.clownfish.datamodels.AuthTokenListClasscontent;
import io.clownfish.clownfish.datamodels.HibernateInit;
import io.clownfish.clownfish.dbentities.CfQuartz;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.interceptor.GzipSwitch;
import io.clownfish.clownfish.lucene.LuceneConstants;
import io.clownfish.clownfish.odata.GenericEdmProvider;
import io.clownfish.clownfish.sap.RFC_FUNCTION_SEARCH;
import io.clownfish.clownfish.sap.RFC_GET_FUNCTION_INTERFACE;
import io.clownfish.clownfish.sap.RFC_GROUP_SEARCH;
import io.clownfish.clownfish.sap.RPY_TABLE_READ;
import io.clownfish.clownfish.service.PageRenderService;
import io.clownfish.clownfish.serviceinterface.CfAttributService;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentKeywordService;
import io.clownfish.clownfish.serviceinterface.CfClasscontentService;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import io.clownfish.clownfish.serviceinterface.CfJavaService;
import io.clownfish.clownfish.serviceinterface.CfKeywordService;
import io.clownfish.clownfish.serviceinterface.CfListcontentService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.serviceinterface.CfSitedatasourceService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
import io.clownfish.clownfish.utils.BeanUtil;
import io.clownfish.clownfish.utils.ClassPathUtil;
import io.clownfish.clownfish.utils.ClownfishUtil;
import io.clownfish.clownfish.utils.CompressionUtils;
import io.clownfish.clownfish.utils.ConsistencyUtil;
import io.clownfish.clownfish.utils.DefaultUtil;
import io.clownfish.clownfish.utils.FolderUtil;
import io.clownfish.clownfish.utils.HibernateUtil;
import io.clownfish.clownfish.utils.MailUtil;
import io.clownfish.clownfish.utils.MarkdownUtil;
import io.clownfish.clownfish.utils.PDFUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import io.clownfish.clownfish.utils.QuartzJob;
import io.clownfish.clownfish.utils.TemplateUtil;
import io.clownfish.clownfish.websocket.WebSocketServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.apache.catalina.util.ServerInfo;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;
import org.fusesource.jansi.AnsiConsole;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.DefaultPropertiesPersister;

/**
 *
 * @author SulzbachR
 */
@Component
public class ClownfishInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClownfishInitializer.class);

    // --- 1. Services & Utils (via Constructor Injection) ---
    private final ServiceStatus serviceStatus;
    private final PropertyList propertyList;
    private final PropertyUtil propertyUtil;
    private final CfClassCompiler cfclassCompiler;
    private final CfClassLoader cfclassLoader;
    private final BeanUtil beanUtil;
    private final ClassPathUtil classpathUtil;
    private final MavenList mavenList;
    private final ConsistencyUtil consistencyUtil;
    private final HibernateUtil hibernateUtil;
    private final FolderUtil folderUtil;
    private final MarkdownUtil markdownUtil;
    private final Scheduler scheduler;
    private final QuartzList quartzList;
    private final ClownfishUtil clownfishUtil;
    private final DefaultUtil defaultUtil;
    private final PageRenderService pageRenderService;
    private AuthTokenListClasscontent authtokenlistclasscontent = null;
    String libloaderpath;
    String mavenpath;
    private static HibernateInit hibernateInitializer = null;
    private final MailUtil mailUtil;
    private PDFUtil pdfUtil;
    private boolean sapSupport = false;
    private static SAPConnection sapc = null;
    private GzipSwitch gzipswitch;
    private @Getter @Setter RPY_TABLE_READ rpytableread = null;
    private @Getter @Setter RFC_GET_FUNCTION_INTERFACE rfcfunctioninterface = null;
    private @Getter @Setter RFC_GROUP_SEARCH rfcgroupsearch = null;
    private @Getter @Setter RFC_FUNCTION_SEARCH rfcfunctionsearch = null;
    
    private ClownfishConst.ViewModus modus = STAGING;
    private @Getter @Setter boolean initmessage = false;
    private boolean jobSupport = false;
    private int searchlimit;
    private Map<String, String> metainfomap;
    private WebSocketServer wss;

    // --- 2. Konfigurationswerte (via @Value) ---
    @Value("${bootstrap}") 
    int bootstrap;
    @Value("${check.consistency:0}") 
    int checkConsistency;
    @Value("${hibernate.init:0}") 
    int hibernateInit;
    @Value("${websocket.use:0}") 
    int websocketUse;
    @Value("${websocket.port:9001}") 
    int websocketPort;
    @Value("${sapconnection.file}") 
    String sapConnectionFile;
    @Value("${app.datasource.url}") 
    String dburl;
    @Value("${sapconnection.file}") 
    String SAPCONNECTION;
    
    @Autowired AuthTokenList authtokenlist;
    @Autowired AuthTokenListClasscontent confirmtokenlist;
    @Autowired CfJavaService cfjavaService;
    @Autowired TemplateUtil templateUtil;
    @Autowired CfTemplateService cftemplateService;
    @Autowired GenericEdmProvider edmprovider;
    @Autowired CfClassService cfclassService;
    @Autowired CfAttributService cfattributService;
    @Autowired CfClasscontentService cfclasscontentService;
    @Autowired CfClasscontentKeywordService cfclasscontentkeywordService;
    @Autowired CfKeywordService cfkeywordService;
    @Autowired CfListcontentService cflistcontentService;
    @Autowired CfAttributcontentService cfattributcontentService;
    @Autowired CfTemplateversionService cftemplateversionService;
    @Autowired CfSitedatasourceService cfsitedatasourceService;
    @Autowired CfDatasourceService cfdatasourceService;
    @Autowired CfSiteService cfsiteService;
    
    // --- 3. Konstruktor ---
    public ClownfishInitializer(ServiceStatus serviceStatus, 
                                PropertyList propertyList, 
                                CfClassCompiler cfclassCompiler, 
                                CfClassLoader cfclassLoader, 
                                BeanUtil beanUtil, 
                                ClassPathUtil classpathUtil, 
                                MavenList mavenList, 
                                ConsistencyUtil consistencyUtil, 
                                HibernateUtil hibernateUtil, 
                                FolderUtil folderUtil, 
                                Scheduler scheduler, 
                                QuartzList quartzList, 
                                ClownfishUtil clownfishUtil,  
                                DefaultUtil defaultUtil,
                                MailUtil mailUtil,  
                                MarkdownUtil markdownUtil, 
                                PageRenderService pageRenderService) {
        this.serviceStatus = serviceStatus;
        this.propertyList = propertyList;
        this.propertyUtil = new PropertyUtil(propertyList);
        this.cfclassCompiler = cfclassCompiler;
        this.cfclassLoader = cfclassLoader;
        this.beanUtil = beanUtil;
        this.classpathUtil = classpathUtil;
        this.mavenList = mavenList;
        this.consistencyUtil = consistencyUtil;
        this.hibernateUtil = hibernateUtil;
        this.folderUtil = folderUtil;
        this.scheduler = scheduler;
        this.quartzList = quartzList;
        this.clownfishUtil = clownfishUtil;
        this.defaultUtil = defaultUtil;
        this.mailUtil = mailUtil;
        this.mailUtil.setPropertyUtil(propertyUtil);
        this.markdownUtil = markdownUtil;
        this.pageRenderService = pageRenderService;
    }
    
    
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        LOGGER.info("INIT CLOWNFISH START");
        serviceStatus.setMessage("Clownfish is initializing");
        serviceStatus.setOnline(false);
        
        if (null == confirmtokenlist) {
            confirmtokenlist = new AuthTokenListClasscontent();
            confirmtokenlist.setConfirmation(true);
        } else {
            confirmtokenlist.setConfirmation(true);
        }
        
        if (null == authtokenlist) {
            authtokenlist = new AuthTokenList();
        }
        
        if (null == authtokenlistclasscontent) {
            authtokenlistclasscontent = new AuthTokenListClasscontent();
            authtokenlistclasscontent.setConfirmation(false);
        } else {
            authtokenlistclasscontent.setConfirmation(false);
        }
        
        cfclassCompiler.setClownfish(this);
        cfclassCompiler.init(cfclassLoader, propertyUtil, cfjavaService);
        
        libloaderpath = propertyUtil.getPropertyValue("folder_libs");
        if ((!libloaderpath.isBlank()) && (null != libloaderpath)) 
            beanUtil.init(libloaderpath);
        
        mavenpath = propertyUtil.getPropertyValue("folder_maven");
        
        if ((!mavenpath.isBlank()) && (null != mavenpath)) {
            classpathUtil.init(cfclassLoader);
            classpathUtil.addPath(mavenpath);
        }
        
        mavenList.setClasspathUtil(classpathUtil);
        
        Thread compileThread = new Thread(cfclassCompiler);
        compileThread.start();
        
        if (1 == bootstrap) {
            bootstrap = 0;
            try {
                AnsiConsole.systemInstall();
                System.out.println(ansi().fg(GREEN));
                System.out.println("BOOTSTRAPPING II");
                System.out.println(ansi().reset());
                
                Properties props = new Properties();
                String propsfile = "application.properties";
                InputStream is = new FileInputStream(propsfile);
                if (null != is) {
                    props.load(is);
                    props.setProperty("bootstrap", String.valueOf(bootstrap));
                    File f = new File("application.properties");

                    bootstrap();

                    OutputStream out = new FileOutputStream(f);
                    DefaultPropertiesPersister p = new DefaultPropertiesPersister();
                    p.store(props, out, "Application properties");
                } else {
                    LOGGER.error("application.properties file not found");
                }
              } catch (IOException ex) {
                  LOGGER.error(ex.getMessage());
              }
        }
        
        Package p = FacesContext.class.getPackage();
        
        edmprovider.init();
        Thread edmprovider_thread = new Thread(edmprovider);
        edmprovider_thread.start();
        
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = null;
        if ((new File("pom.xml")).exists()) {
            try {
                model = reader.read(new FileReader("pom.xml"));
            } catch (FileNotFoundException ex) {
                LOGGER.error(ex.getMessage());
            } catch (IOException | XmlPullParserException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        if (null != model) {
            clownfishUtil.setVersion(model.getVersion()).setVersionMojarra(p.getImplementationVersion()).setVersionTomcat(ServerInfo.getServerNumber());
        } else {
            clownfishUtil.setVersion(getClass().getPackage().getImplementationVersion()).setVersionMojarra(p.getImplementationVersion()).setVersionTomcat(ServerInfo.getServerNumber());
        }
        try {
            AnsiConsole.systemInstall();
            System.out.println(ansi().fg(GREEN));
            System.out.println("INIT CLOWNFISH CMS");
            System.out.println(ansi().fg(RED));
            System.out.println("                               ...                                             ");
            System.out.println("                            &@@@@@@@                                           ");
            System.out.println("                          .&&%%%@%@@@@                                         ");
            System.out.println("                          %%%%%%%%&%%@                                         ");
            System.out.println("                         .%%%%%%%%#%%&                                         ");
            System.out.println("                         /%%%%%%%%%##%         &&@@.                           ");
            System.out.println("                    .,*//#############%        %#%#%@@                          ");
            System.out.println("                     #((###############       ###%#%%&@                         ");
            System.out.println("           //          #((((/(/(((((((/       (###(###&%                        ");
            System.out.println("        *((//((#/       #((((#(((((((#         (((##%##@                        ");
            System.out.println("      /(((((#(////#     .((((((((((((*         *((((###%                        ");
            System.out.println("     /(((((#((######     #(((((##((@@@@&        ((((((##                        ");
            System.out.println("    (((((((####@@&#(%     #((((####((((&@/      #(((((((*            (&@@&#     ");
            System.out.println("   /(((((((#######(((&    *((((##(((((((#@/     #(((((((/          &&%%%%&@@@,  ");
            System.out.println("  /((((((((((((((#((((&    (#((#(##((##((@@     #(#####(*       *########%%&@@@.");
            System.out.println("  /%#(((((((((((((/((((.   ###########(((#@*    ((#####(.      /#########%%%@@@@");
            System.out.println("   ,(((((((((((((((((((/   ###########((((@,   ((#######       ###########%%%@@@");
            System.out.println("    .(((((((((((((/((((*   (#########((((#@    ((######%      .##########%%%&@@@");
            System.out.println("       ,/(((((((((((((&   #(#########((((@/  .#((###((((       #########%%%%&@@@");
            System.out.println("           /((((((##(&   %(((((######(((&/  .#(((((((((.       *########%%%&&@@@");
            System.out.println("              ,//(#@   /(#(####(((##(((&.  (((((((((/           /######%%%&&@@@@");
            System.out.println("                ./* .#(########((#((*/.  *#(((((((###.           /%##%%%%@@@@@@ ");
            System.out.println("                /######((#######(    ./#######(###(#@              .&@@@@@@@%   ");
            System.out.println("               /#######%%#(((#((((@.  /@%#####(##((@#                           ");
            System.out.println("              /####%%#@& *#(((//(/(%@   *@@@@%#%&@@                             ");
            System.out.println("              %#%&&@&*    *((((///(@@&     .##/*                                ");
            System.out.println("                  ,,***,   *((/(#@@@@@,                                         ");
            System.out.println("                            *@@@@@@@@%          [" + clownfishUtil.getVersion() + "] on Tomcat " + clownfishUtil.getVersionTomcat()+ " with Mojarra " + clownfishUtil.getVersionMojarra());
            System.out.println(ansi().reset());
            
            // Check Consistence
            if (checkConsistency > 0) {
                consistencyUtil.checkConsistency();
            }
            
            // Generate Hibernate DOM Mapping
            if (null == hibernateInitializer) {
                hibernateInitializer = new HibernateInit(serviceStatus, cfclassService, cfattributService, cfclasscontentService, cfattributcontentService, cflistcontentService, cfclasscontentkeywordService, cfkeywordService, dburl);
                hibernateUtil.init(hibernateInitializer);
                hibernateUtil.setHibernateInit(hibernateInit);
                
                Thread hibernateThread = new Thread(hibernateUtil);
                hibernateThread.start();
            }
            
            propertyList.setClownfish(this);
 
            if (null == pdfUtil)
                pdfUtil = new PDFUtil(cftemplateService, cftemplateversionService, cfsitedatasourceService, cfdatasourceService, cfsiteService, propertyUtil, templateUtil);
            
            // Set default values
            modus = STAGING;    // 1 = Staging mode (fetch sourcecode from commited repository) <= default
                                // 0 = Development mode (fetch sourcecode from database)
            defaultUtil.setCharacterEncoding("UTF-8")
                       .setContentType("text/html")
                       .setLocale(new Locale("de"));
            
            sapSupport = propertyUtil.getPropertyBoolean("sap_support", sapSupport);
            if (sapSupport) {
                if (null == sapc) {
                    sapc = new SAPConnection(SAPCONNECTION, "Clownfish1");
                    rpytableread = new RPY_TABLE_READ(sapc);
                    rfcfunctioninterface = new RFC_GET_FUNCTION_INTERFACE(sapc);
                    rfcgroupsearch = new RFC_GROUP_SEARCH(sapc);
                    rfcfunctionsearch = new RFC_FUNCTION_SEARCH(sapc);
                }
            }
            // Override default values with system properties
            String systemContentType = propertyUtil.getPropertyValue("response_contenttype");
            String systemCharacterEncoding = propertyUtil.getPropertyValue("response_characterencoding");
            String systemLocale = propertyUtil.getPropertyValue("response_locale");
            if (!systemCharacterEncoding.isEmpty()) {
                defaultUtil.setCharacterEncoding(systemCharacterEncoding);
            }
            if (!systemContentType.isEmpty()) {
                defaultUtil.setContentType(systemContentType);
            }
            if (!systemLocale.isEmpty()) {
                defaultUtil.setLocale(new Locale(systemLocale));
            }
            if (null == gzipswitch) {
                gzipswitch = new GzipSwitch();
            }
            
            markdownUtil.initOptions();
           
            // Init Site Metadata Map
            if (null == metainfomap) {
                metainfomap = new HashMap();
            }
            metainfomap.put("version", clownfishUtil.getVersion());
            metainfomap.put("versionMojarra", clownfishUtil.getVersionMojarra());
            metainfomap.put("versionTomcat", clownfishUtil.getVersionTomcat());
            
            searchlimit = propertyUtil.getPropertyInt("lucene_searchlimit", LuceneConstants.MAX_SEARCH);
            
            jobSupport = propertyUtil.getPropertyBoolean("job_support", jobSupport);
            if (jobSupport) {
                scheduler.clear();
                // Fetch the Quartz jobs
                quartzList.init();
                quartzList.setClownfish(this);
                List<CfQuartz> joblist = quartzList.getQuartzlist();
                joblist.stream().forEach((quartz) -> {
                    try {
                        if (quartz.isActive()) {
                            JobDetail job = newJob(quartz.getName());
                            scheduler.scheduleJob(job, trigger(job, quartz.getSchedule()));
                            System.out.println(ansi().fg(GREEN));
                            System.out.println("JOB SCHEDULE: " + quartz.getName());
                            System.out.println(ansi().reset());

                        }
                    } catch (SchedulerException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                });
            }
            AnsiConsole.systemUninstall();
        } catch (SchedulerException ex) {
            LOGGER.error(ex.getMessage());
        }
        folderUtil.init();
        serviceStatus.setMessage("Clownfish is online");
        serviceStatus.setOnline(true);
        LOGGER.info("INIT CLOWNFISH END");
        
        if (1 == websocketUse) {
            if (null == wss) {
                wss = new WebSocketServer(pageRenderService);
                wss.setPort(websocketPort);
                try {
                    Thread websocketserver_thread = new Thread(wss);
                    websocketserver_thread.start();
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(ClownfishInitializer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    /**
     * bootstrap
     * 
     */
    private void bootstrap() throws FileNotFoundException {
        List<CfTemplate> cftemplatelist = cftemplateService.findAll();
        for (CfTemplate template : cftemplatelist) {
            try {
                templateUtil.setTemplateContent(template.getContent());

                String content = templateUtil.getTemplateContent();
                byte[] output = CompressionUtils.compress(content.getBytes("UTF-8"));

                templateUtil.setCurrentVersion(1);
                templateUtil.writeVersion(template.getId(), templateUtil.getCurrentVersion(), output, 0);
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }
    
    /**
     * newJob
     * 
     */
    private JobDetail newJob(String identity) {
        return JobBuilder.newJob().ofType(QuartzJob.class).storeDurably()
            .withIdentity(JobKey.jobKey(identity))
            .build();
    }

    /**
     * trigger
     * 
     */
    private CronTrigger trigger(JobDetail jobDetail, String schedule) {
        return TriggerBuilder.newTrigger().forJob(jobDetail)
            .withIdentity(jobDetail.getKey().getName(), jobDetail.getKey().getGroup())
            .withSchedule(cronSchedule(schedule))
            .build();
    }
    
    public void initClasspath() {
        //cfclassLoader = null;
        initmessage = true;
        init();
    }
}
