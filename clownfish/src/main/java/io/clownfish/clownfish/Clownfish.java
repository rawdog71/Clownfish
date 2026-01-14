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
package io.clownfish.clownfish;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.destrukt.sapconnection.SAPConnection;
import io.clownfish.clownfish.beans.MavenList;
import io.clownfish.clownfish.beans.PropertyList;
import io.clownfish.clownfish.beans.QuartzList;
import io.clownfish.clownfish.beans.ServiceStatus;
import io.clownfish.clownfish.beans.SiteTreeBean;
import io.clownfish.clownfish.compiler.CfClassCompiler;
import io.clownfish.clownfish.compiler.CfClassLoader;
import io.clownfish.clownfish.constants.ClownfishConst;
import static io.clownfish.clownfish.constants.ClownfishConst.ViewModus.STAGING;
import io.clownfish.clownfish.datamodels.AuthTokenList;
import io.clownfish.clownfish.datamodels.AuthTokenListClasscontent;
import io.clownfish.clownfish.datamodels.ClientInformation;
import io.clownfish.clownfish.datamodels.ClownfishResponse;
import io.clownfish.clownfish.datamodels.HibernateInit;
import io.clownfish.clownfish.datamodels.JsonFormParameter;
import io.clownfish.clownfish.datamodels.RenderContext;
import io.clownfish.clownfish.datamodels.SearchContext;
import io.clownfish.clownfish.dbentities.*;
import io.clownfish.clownfish.exceptions.PageNotFoundException;
import io.clownfish.clownfish.interceptor.GzipSwitch;
import io.clownfish.clownfish.lucene.LuceneConstants;
import io.clownfish.clownfish.lucene.SearchResult;
import io.clownfish.clownfish.lucene.Searcher;
import io.clownfish.clownfish.odata.GenericEdmProvider;
import io.clownfish.clownfish.sap.RFC_FUNCTION_SEARCH;
import io.clownfish.clownfish.sap.RFC_GET_FUNCTION_INTERFACE;
import io.clownfish.clownfish.sap.RFC_GROUP_SEARCH;
import io.clownfish.clownfish.sap.RPY_TABLE_READ;
import io.clownfish.clownfish.service.PageRenderService;
import io.clownfish.clownfish.serviceimpl.CfStringTemplateLoaderImpl;
import io.clownfish.clownfish.serviceinterface.*;
import io.clownfish.clownfish.templatebeans.*;
import io.clownfish.clownfish.utils.*;
import io.clownfish.clownfish.websocket.WebSocketServer;
import lombok.Getter;
import lombok.Setter;
import org.apache.catalina.util.ServerInfo;
import org.apache.commons.fileupload.FileItem;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;
import org.fusesource.jansi.AnsiConsole;
import org.primefaces.webapp.MultipartRequest;
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
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.stream.Collectors;


/**
 *
 * @author sulzbachr
 * Central class of the Clownfish server
 */
@RestController
@EnableAutoConfiguration(exclude = HibernateJpaAutoConfiguration.class)
@Component
@Configuration
@PropertySources({
    @PropertySource("file:application.properties")
})
public class Clownfish {
    @Autowired CfSiteService cfsiteService;
    @Autowired CfListcontentService cflistcontentService;
    @Autowired @Getter @Setter CfAssetService cfassetService;
    @Autowired @Getter @Setter CfAttributcontentService cfattributcontentService;
    @Autowired CfSitedatasourceService cfsitedatasourceService;
    @Autowired CfTemplateService cftemplateService;
    @Autowired CfTemplateversionService cftemplateversionService;
    @Autowired CfJavaService cfjavaService;
    @Autowired TemplateUtil templateUtil;
    @Autowired PropertyList propertylist;
    @Autowired QuartzList quartzlist;
    @Autowired CfDatasourceService cfdatasourceService;
    @Autowired CfClassService cfclassService;
    @Autowired CfAttributService cfattributService;
    @Autowired CfClasscontentService cfclasscontentService;
    @Autowired CfClasscontentKeywordService cfclasscontentkeywordService;
    @Autowired CfKeywordService cfkeywordService;
    @Autowired private Scheduler scheduler;
    @Autowired private FolderUtil folderUtil;
    @Autowired Searcher searcher;
    @Autowired ConsistencyUtil consistencyUtil;
    @Autowired HibernateUtil hibernateUtil;
    @Autowired ServiceStatus servicestatus;
    @Autowired SearchUtil searchUtil;
    @Autowired GenericEdmProvider edmprovider;
    @Autowired PageRenderService pagerenderservice;
    
    UploadTemplateBean uploadbean;
    CfClassCompiler cfclassCompiler;
    CfClassLoader cfclassLoader;
    @Autowired AuthTokenList authtokenlist;
    @Autowired AuthTokenListClasscontent confirmtokenlist;
    AuthTokenListClasscontent authtokenlistclasscontent = null;

    private String contenttype;
    private String characterencoding;
    private String locale;

    private GzipSwitch gzipswitch;
    private @Getter @Setter RPY_TABLE_READ rpytableread = null;
    private @Getter @Setter RFC_GET_FUNCTION_INTERFACE rfcfunctioninterface = null;
    private @Getter @Setter RFC_GROUP_SEARCH rfcgroupsearch = null;
    private @Getter @Setter RFC_FUNCTION_SEARCH rfcfunctionsearch = null;
    private static SAPConnection sapc = null;
    private boolean sapSupport = false;
    private boolean jobSupport = false;
    private HttpSession userSession;
    private ClownfishConst.ViewModus modus = STAGING;
    private boolean preview = false;
    private boolean cf_job = false;
    private ClownfishUtil clownfishutil;
    private PropertyUtil propertyUtil;
    private MailUtil mailUtil;
    private DefaultUtil defaultUtil;
    private PDFUtil pdfUtil;
    private BeanUtil beanUtil;
    private ClassPathUtil classpathUtil;
    private MavenList mavenlist;
    private StaticSiteUtil staticSiteUtil;
    private @Getter @Setter MarkdownUtil markdownUtil;
    private @Getter @Setter int searchlimit;
    private @Getter @Setter Map<String, String> metainfomap;
    private static HibernateInit hibernateInitializer = null;
        
    private WebSocketServer wss;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(Clownfish.class);
    @Value("${bootstrap}") int bootstrap;
    @Value("${app.datasource.url}") String dburl;
    @Value("${check.consistency:0}") int checkConsistency;
    @Value("${hibernate.init:0}") int hibernateInit;
    @Value("${sapconnection.file}") String SAPCONNECTION;
    @Value("${websocket.use:0}") int websocketUse;
    @Value("${websocket.port:9001}") int websocketPort;
    String libloaderpath;
    String mavenpath;
    private @Getter @Setter boolean initmessage = false;
    private @Getter @Setter SiteTreeBean sitetree;
    @Value("${server.name:Clownfish Server Open Source}") String servername;
    @Value("${server.x-powered:Clownfish Server Open Source by Rainer Sulzbach}") String serverxpowered;
    
    /**
     * Call of the "root" site
     * Fetches the root site from the system property "site_root" and calls universalGet 
     * @param request
     * @param response
     */
    @RequestMapping("/")
    public void home(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        String root_site = propertyUtil.getPropertyValue("site_root");
        if (null == root_site) {
            root_site = "root";
        }
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, root_site);
        universalGet(root_site, request, response, null);
    }
    
    public void initClasspath() {
        cfclassLoader = null;
        cfclassCompiler = null;
        initmessage = true;
        init();
    }
    
    /**
     * Call of the "init" site
     * Init is called at the beginning of the Clownfish startup and after changing system properties
     * Checks the bootstrap flag of the application.properties and calls a bootstrap routine
     * Initializes several variables and starts Quartz job triggers
     */
    @PostConstruct
    public void init() {
        LOGGER.info("INIT CLOWNFISH START");
        servicestatus.setMessage("Clownfish is initializing");
        servicestatus.setOnline(false);
        
        if (null == confirmtokenlist) {
            confirmtokenlist = new AuthTokenListClasscontent();
            confirmtokenlist.setConfirmation(true);
        }
        
        if (null == authtokenlist) {
            authtokenlist = new AuthTokenList();
        }
        
        if (null == authtokenlistclasscontent) {
            authtokenlistclasscontent = new AuthTokenListClasscontent();
            authtokenlistclasscontent.setConfirmation(false);
        }
        
        // read all System Properties of the property table
        if (null == propertyUtil) {
            propertyUtil = new PropertyUtil(propertylist);
        }

        if (null == staticSiteUtil) {
            staticSiteUtil = new StaticSiteUtil(propertyUtil);
        }

        if (null == cfclassLoader)
        {
            cfclassLoader = new CfClassLoader();
        }
        
        if (null == cfclassCompiler)
        {
            cfclassCompiler = new CfClassCompiler();
            cfclassCompiler.setClownfish(this);
            cfclassCompiler.init(cfclassLoader, propertyUtil, cfjavaService);
        }
        
        libloaderpath = propertyUtil.getPropertyValue("folder_libs");
        
        if (null == beanUtil)
            beanUtil = new BeanUtil();
        if ((!libloaderpath.isBlank()) && (null != libloaderpath)) 
            beanUtil.init(libloaderpath);
        
        mavenpath = propertyUtil.getPropertyValue("folder_maven");
        
        if ((!mavenpath.isBlank()) && (null != mavenpath)) {
            if (null == classpathUtil) {
                classpathUtil = new ClassPathUtil();
                classpathUtil.init(cfclassLoader);
            }
            classpathUtil.addPath(mavenpath);
        }
        
        if (null == mavenlist) {
            mavenlist = new MavenList();
            mavenlist.setClasspathUtil(classpathUtil);
        }
        
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
        if (null == clownfishutil) {
            clownfishutil = new ClownfishUtil();
        }
        
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
            clownfishutil.setVersion(model.getVersion()).setVersionMojarra(p.getImplementationVersion()).setVersionTomcat(ServerInfo.getServerNumber());
        } else {
            clownfishutil.setVersion(getClass().getPackage().getImplementationVersion()).setVersionMojarra(p.getImplementationVersion()).setVersionTomcat(ServerInfo.getServerNumber());
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
            System.out.println("                            *@@@@@@@@%          [" + clownfishutil.getVersion() + "] on Tomcat " + clownfishutil.getVersionTomcat()+ " with Mojarra " + clownfishutil.getVersionMojarra());
            System.out.println(ansi().reset());
            
            // Check Consistence
            if (checkConsistency > 0) {
                consistencyUtil.checkConsistency();
            }
            
            // Generate Hibernate DOM Mapping
            if (null == hibernateInitializer) {
                hibernateInitializer = new HibernateInit(servicestatus, cfclassService, cfattributService, cfclasscontentService, cfattributcontentService, cflistcontentService, cfclasscontentkeywordService, cfkeywordService, dburl);
                hibernateUtil.init(hibernateInitializer);
                hibernateUtil.setHibernateInit(hibernateInit);
                
                Thread hibernateThread = new Thread(hibernateUtil);
                hibernateThread.start();
            }
            
            propertylist.setClownfish(this);
            if (null == defaultUtil) {
                defaultUtil = new DefaultUtil();
            }

            if (null == mailUtil)
                mailUtil = new MailUtil(propertyUtil);

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
            
            if (null == markdownUtil) {
                markdownUtil = new MarkdownUtil(propertylist);
                markdownUtil.initOptions();
            } else {
                markdownUtil.initOptions();
            }
           
            // Init Site Metadata Map
            if (null == metainfomap) {
                metainfomap = new HashMap();
            }
            metainfomap.put("version", clownfishutil.getVersion());
            metainfomap.put("versionMojarra", clownfishutil.getVersionMojarra());
            metainfomap.put("versionTomcat", clownfishutil.getVersionTomcat());
            
            
            searchlimit = propertyUtil.getPropertyInt("lucene_searchlimit", LuceneConstants.MAX_SEARCH);
            
            jobSupport = propertyUtil.getPropertyBoolean("job_support", jobSupport);
            if (jobSupport) {
                scheduler.clear();
                // Fetch the Quartz jobs
                quartzlist.init();
                quartzlist.setClownfish(this);
                List<CfQuartz> joblist = quartzlist.getQuartzlist();
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
        servicestatus.setMessage("Clownfish is online");
        servicestatus.setOnline(true);
        LOGGER.info("INIT CLOWNFISH END");
        
        if (1 == websocketUse) {
            if (null == wss) {
                wss = new WebSocketServer(pagerenderservice);
                wss.setPort(websocketPort);
                try {
                    Thread websocketserver_thread = new Thread(wss);
                    websocketserver_thread.start();
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(Clownfish.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public Clownfish() {
    }
    
    /**
     * GET
     * 
     * @param name
     * @param request
     * @param response
     * @param searchctx
     */
    @GetMapping(path = "/{name}/**")
    public void universalGet(@PathVariable("name") String name, @Context HttpServletRequest request, @Context HttpServletResponse response, SearchContext searchctx) {
        if (servicestatus.isOnline()) {
            Map searchcontentmap = null;
            Map searchassetmap = null;
            Map searchassetmetadatamap = null;
            Map searchclasscontentmap = null;
            Map searchmetadata = null;
            
            if (null == searchctx) {
                // Init Lucene Search Map
                if (null == searchcontentmap) {
                    searchcontentmap = new HashMap<>();
                }
                if (null == searchassetmap) {
                    searchassetmap = new HashMap<>();
                }
                if (null == searchassetmetadatamap) {
                    searchassetmetadatamap = new HashMap<>();
                }
                if (null == searchclasscontentmap) {
                    searchclasscontentmap = new HashMap<>();
                }
                if (null == searchmetadata) {
                    searchmetadata = new HashMap<>();
                }
            } else {
                searchcontentmap = searchctx.getSearchcontentmap();
                searchassetmap = searchctx.getSearchassetmap();
                searchassetmetadatamap = searchctx.getSearchassetmetadatamap();
                searchclasscontentmap = searchctx.getSearchclasscontentmap();
                searchmetadata = searchctx.getSearchmetadata();
            }
            
            Cookie[] cookies = request.getCookies();
            String referrer = getCookieVal(cookies, "cf_referrer");
            addHeader(response, clownfishutil.getVersion());
            ClientInformation clientinfo = getClientinformation(request.getRemoteAddr());
            String token = getCookieVal(cookies, "cf_token");
            String login_token = getCookieVal(cookies, "cf_login_token");
            boolean alias = false;
            String aliasname = "";
            try {
                ArrayList urlParams = new ArrayList();
                // fetch site by name or aliasname
                CfSite cfsite = null;
                cfsite = cfsiteService.findByName(name);
                if (null == cfsite) {
                    aliasname = name;
                    cfsite = cfsiteService.findByAliaspath(name);
                    if (null != cfsite) {
                        name = cfsite.getName();
                        alias = true;
                    } else {
                        aliasname = name;
                        cfsite = cfsiteService.findByShorturl(name);
                        if (null != cfsite) {
                            name = cfsite.getName();
                            alias = true;
                        } else {
                            throw new PageNotFoundException("PageNotFound Exception: " + name);
                        }
                    }
                }
                
                response.setContentType(cfsite.getContenttype());
                response.setCharacterEncoding(cfsite.getCharacterencoding());
                
                if (!cfsite.isSearchresult()) {
                    String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
                    if (alias) {
                        path = path.replaceFirst(aliasname, name);
                    } else {
                        path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
                    }

                    if (path.contains("/")) {
                        String[] params = path.split("/");
                        for (int i = 1; i < params.length; i++) {
                            if (1 == i) {
                                path = params[i];
                            } else {
                                urlParams.add(params[i]);
                            }
                        }
                    }

                    if (name.compareToIgnoreCase(path) != 0) {
                        if (path.startsWith("/")) {
                            name = path.substring(1);
                        } else {
                            name = path;
                        }
                        if (name.lastIndexOf("/")+1 == name.length()) {
                            name = name.substring(0, name.length()-1);
                        }
                    }
                } else {
                    searchmetadata.clear();
                    String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
                    String query = "";
                    if (path.contains("/")) {
                        String[] params = path.split("/");
                        for (int i = 1; i < params.length; i++) {
                            if (1 == i) {
                                path = params[i];
                            } else {
                                query += params[i];
                            }
                        }
                    }
                    if (query.isEmpty()) {
                        Map<String, String[]> parammap = request.getParameterMap();
                        if (parammap.containsKey("query")) {
                            query = parammap.get("query")[0];
                        } else {
                            query = "";
                        }
                    }

                    String[] searchexpressions = query.split(" ");
                    searchUtil.updateSearchhistory(searchexpressions);

                    searcher.setIndexPath(folderUtil.getIndex_folder());
                    long startTime = System.currentTimeMillis();
                    SearchResult searchresult = searcher.search(query, searchlimit);
                    long endTime = System.currentTimeMillis();

                    LOGGER.info("Search Time :" + (endTime - startTime));
                    
                    searchcontentmap.clear();
                    if (searchresult != null && searchresult.getFoundSites() != null) {
                        // putAll statt loop
                        searchcontentmap.putAll(
                            searchresult.getFoundSites().stream()
                                .filter(Objects::nonNull) // Null-Check aus dem Stream
                                .collect(Collectors.toMap(
                                    site -> site.getName(), // Key
                                    site -> site,           // Value
                                    (existing, replacement) -> replacement // Falls Keys doppelt sind: überschreiben (wie bei put)
                                ))
                        );
                    }

                    searchassetmap.clear();
                    if (searchresult != null && searchresult.getFoundAssets() != null) {
                        searchassetmap.putAll(
                            searchresult.getFoundAssets().stream()
                                .filter(Objects::nonNull)
                                .collect(Collectors.toMap(CfAsset::getName, asset -> asset, (v1, v2) -> v2))
                        );
                    }

                    searchassetmetadatamap.clear();
                    if (searchresult != null && searchresult.getFoundAssetsMetadata() != null) {
                        searchassetmetadatamap.putAll(searchresult.getFoundAssetsMetadata());
                    }
                    
                    searchclasscontentmap.clear();
                    if (searchresult != null && searchresult.getFoundClasscontent() != null) {
                        searchclasscontentmap.putAll(searchresult.getFoundClasscontent());
                    }
                }

                userSession = request.getSession();
                Map<String, String[]> querymap = request.getParameterMap();

                ArrayList queryParams = new ArrayList();
                if ((null != token) && (!token.isEmpty())) {
                    JsonFormParameter jfp = new JsonFormParameter();
                    jfp.setName("cf_token");
                    jfp.setValue(token);
                    queryParams.add(jfp);
                }
                if ((null != login_token) && (!login_token.isEmpty())) {
                    JsonFormParameter jfp = new JsonFormParameter();
                    jfp.setName("cf_login_token");
                    jfp.setValue(login_token);
                    queryParams.add(jfp);
                }
                querymap.keySet().stream().map((key) -> {
                    JsonFormParameter jfp = new JsonFormParameter();
                    jfp.setName((String) key);
                    String[] values = querymap.get((String) key);
                    jfp.setValue(values[0]);
                    return jfp;
                }).forEach((jfp) -> {
                    queryParams.add(jfp);
                });

                //addHeader(response, clownfishutil.getVersion());
                //LOGGER.info("MAKERESPONSE: " + name);
                
                RenderContext rc = new RenderContext();
                rc.setName(name);
                rc.setPostmap(queryParams);
                rc.setUrlParams(urlParams);
                rc.setMakestatic(false);
                rc.setFileitems(null);
                rc.setClientinfo(clientinfo);
                rc.setReferrer(referrer);
                rc.setSearchassetmap(searchassetmap);
                rc.setSearchassetmetadatamap(searchassetmetadatamap);
                rc.setSearchclasscontentmap(searchclasscontentmap);
                rc.setSearchcontentmap(searchcontentmap);
                rc.setSearchmetadata(searchmetadata);
                
                ClownfishResponse cfResponse = pagerenderservice.renderPage(rc);
                
                Cookie refcookie = new Cookie("cf_referrer", "");
                response.addCookie(refcookie);
                if (0 != cfResponse.getErrorcode()) {
                    switch (cfResponse.getErrorcode()) {
                        case 1, 2, 4 -> {
                            response.setContentType("text/html");
                            response.setCharacterEncoding("UTF-8");
                        }
                        case 3, 5 -> {
                            refcookie = new Cookie("cf_referrer", name);
                            response.addCookie(refcookie);
                            response.sendRedirect("/" + cfResponse.getRelocation());
                        }
                    }
                }
                ServletOutputStream out = response.getOutputStream();
                out.write(cfResponse.getOutput().getBytes(response.getCharacterEncoding()));
            } catch (IOException | ParseException ex) {
                LOGGER.error(ex.getMessage());
            } catch (PageNotFoundException ex) {
                String error_site = propertyUtil.getPropertyValue("site_error");
                if (null == error_site) {
                    error_site = "error";
                }
                request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, error_site);
                universalGet(error_site, request, response, null);
            }
        } else {
            PrintWriter outwriter = null;
            try {
                outwriter = response.getWriter();
                outwriter.println(servicestatus.getMessage());
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            } finally {
                if (null != outwriter) {
                    outwriter.close();
                }
            }
        }
    }

    /**
     * POST
     * 
     * @param name
     * @param request
     * @param response
     * @throws io.clownfish.clownfish.exceptions.PageNotFoundException
     */
    @PostMapping("/upload/{name}")
    public void universalPostMultipart(@PathVariable("name") String name, @Context MultipartRequest request, @Context HttpServletResponse response) throws PageNotFoundException {
        Map searchcontentmap = null;
        Map searchassetmap = null;
        Map searchassetmetadatamap = null;
        Map searchclasscontentmap = null;
        Map searchmetadata = null;
        
        boolean alias = false;
        try {
            ClientInformation clientinfo = getClientinformation(request.getRemoteAddr());
            ArrayList urlParams = new ArrayList();
            String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            
            userSession = request.getSession();
            if (request.getContentType().startsWith("multipart/form-data")) {
                Map<String, String[]> parameterMap = request.getParameterMap();
                List<FileItem> fis = request.getFileItems("file");
                List<JsonFormParameter> map = new ArrayList<>();
                for (String key : parameterMap.keySet()) {
                    map.add(new JsonFormParameter(key, parameterMap.get(key)[0]));
                }
                
                addHeader(response, clownfishutil.getVersion());
                
                RenderContext rc = new RenderContext();
                rc.setName(name);
                rc.setPostmap(map);
                rc.setUrlParams(urlParams);
                rc.setMakestatic(false);
                rc.setFileitems(null);
                rc.setClientinfo(clientinfo);
                rc.setReferrer("");
                rc.setSearchassetmap(searchassetmap);
                rc.setSearchassetmetadatamap(searchassetmetadatamap);
                rc.setSearchclasscontentmap(searchclasscontentmap);
                rc.setSearchcontentmap(searchcontentmap);
                rc.setSearchmetadata(searchmetadata);
                ClownfishResponse cfResponse = pagerenderservice.renderPage(rc);
                
                //ClownfishResponse cfResponse = makeResponse(name, map, urlParams, false, fis, clientinfo, "");
                if (cfResponse.getErrorcode() == 0) {
                    response.setContentType(this.contenttype);
                    response.setCharacterEncoding(this.characterencoding);
                    
                    if ((null != uploadbean) && (null != uploadbean.getUploadpath()) && (!uploadbean.getUploadpath().isEmpty())) {
                        for (FileItem fi : fis) {
                            if ((uploadbean.getFileitemmap().get(fi.getName())) || (uploadbean.getFileitemmap().isEmpty())) {
                                File result = new File(uploadbean.getUploadpath() + File.separator + fi.getName());
                                boolean fileexists = result.exists();
                                InputStream inputStream = fi.getInputStream();
                                if (!fileexists) {
                                    try (FileOutputStream fileOutputStream = new FileOutputStream(result)) {
                                        byte[] buffer = new byte[64535];
                                        int bulk;
                                        while (true) {
                                            bulk = inputStream.read(buffer);
                                            if (bulk < 0) {
                                                break;
                                            }
                                            fileOutputStream.write(buffer, 0, bulk);
                                            fileOutputStream.flush();
                                        }
                                        fileOutputStream.close();
                                    }
                                }
                                inputStream.close();
                            }
                        }
                    }
                    
                    ServletOutputStream out = response.getOutputStream();
                    out.write(cfResponse.getOutput().getBytes(this.characterencoding)); 
                } else {
                    response.setContentType("text/html");
                    response.setCharacterEncoding("UTF-8");
                    ServletOutputStream out = response.getOutputStream();
                    out.write(cfResponse.getOutput().getBytes(this.characterencoding)); 
                }
            } else {
                String content = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

                Gson gson = new Gson();
                List<JsonFormParameter> map;
                map = (List<JsonFormParameter>) gson.fromJson(content, new TypeToken<List<JsonFormParameter>>() {}.getType());
                
                // fetch site by name or aliasname
                CfSite cfsite = null;
                try {
                    cfsite = cfsiteService.findByName(name);
                } catch (Exception ex) {
                    try {
                        cfsite = cfsiteService.findByAliaspath(name);
                        name = cfsite.getName();
                        alias = true;
                    } catch (Exception e1) {
                        try {
                            cfsite = cfsiteService.findByShorturl(name);
                            name = cfsite.getName();
                            alias = true;
                        } catch (Exception ey) {
                            throw new PageNotFoundException("PageNotFound Exception: " + name);
                        }
                    }
                }
                if (cfsite.isSearchresult()) {
                    if (searchcontentmap.isEmpty()) {
                        String query = "";
                        for (JsonFormParameter jsp : map) {
                            if (0 == jsp.getName().compareToIgnoreCase("search")) {
                                query += jsp.getValue();
                            }
                        }
                        String[] searchexpressions = query.split(" ");
                        searchUtil.updateSearchhistory(searchexpressions);

                        searcher.setIndexPath(folderUtil.getIndex_folder());
                        long startTime = System.currentTimeMillis();
                        SearchResult searchresult = searcher.search(query, searchlimit);
                        long endTime = System.currentTimeMillis();

                        LOGGER.info("Search Time :" + (endTime - startTime));
                        searchmetadata.clear();
                        searchmetadata.put("cfSearchQuery", query);
                        searchmetadata.put("cfSearchTime", String.valueOf(endTime - startTime));
                        searchcontentmap.clear();
                        searchresult.getFoundSites().stream().forEach((site) -> {
                            searchcontentmap.put(site.getName(), site);
                        });
                        searchassetmap.clear();
                        searchresult.getFoundAssets().stream().forEach((asset) -> {
                            searchassetmap.put(asset.getName(), asset);
                        });
                        searchassetmetadatamap.clear();
                        searchresult.getFoundAssetsMetadata().keySet().stream().forEach((key) -> {
                            searchassetmetadatamap.put(key, searchresult.getFoundAssetsMetadata().get(key));
                        });
                        searchclasscontentmap.clear();
                        searchresult.getFoundClasscontent().keySet().stream().forEach((key) -> {
                            searchclasscontentmap.put(key, searchresult.getFoundClasscontent().get(key));
                        });
                    }
                }

                addHeader(response, clownfishutil.getVersion());
                
                RenderContext rc = new RenderContext();
                rc.setName(name);
                rc.setPostmap(map);
                rc.setUrlParams(urlParams);
                rc.setMakestatic(false);
                rc.setFileitems(null);
                rc.setClientinfo(clientinfo);
                rc.setReferrer("");
                rc.setSearchassetmap(searchassetmap);
                rc.setSearchassetmetadatamap(searchassetmetadatamap);
                rc.setSearchclasscontentmap(searchclasscontentmap);
                rc.setSearchcontentmap(searchcontentmap);
                rc.setSearchmetadata(searchmetadata);
                ClownfishResponse cfResponse = pagerenderservice.renderPage(rc);
                
                //ClownfishResponse cfResponse = makeResponse(name, map, urlParams, false, null, clientinfo, "");
                if (cfResponse.getErrorcode() == 0) {
                    response.setContentType(this.contenttype);
                    response.setCharacterEncoding(this.characterencoding);
                    ServletOutputStream out = response.getOutputStream();
                    out.write(cfResponse.getOutput().getBytes(this.characterencoding)); 
                } else {
                    response.setContentType("text/html");
                    response.setCharacterEncoding("UTF-8");
                    ServletOutputStream out = response.getOutputStream();
                    out.write(cfResponse.getOutput().getBytes(this.characterencoding)); 
                }
            }
        } catch (IOException | PageNotFoundException | IllegalStateException | ParseException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    /**
     * POST
     * 
     * @param name
     * @param request
     * @param response
     * @throws io.clownfish.clownfish.exceptions.PageNotFoundException
     */
    @PostMapping("/{name}/**")
    public void universalPostHttp(@PathVariable("name") String name, @Context HttpServletRequest request, @Context HttpServletResponse response) throws PageNotFoundException {
        Map searchcontentmap = null;
        Map searchassetmap = null;
        Map searchassetmetadatamap = null;
        Map searchclasscontentmap = null;
        Map searchmetadata = null;
        
        boolean alias = false;
        try {
            ClientInformation clientinfo = getClientinformation(request.getRemoteAddr());
            ArrayList urlParams = new ArrayList();
            String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            if (path.contains("/")) {
                String[] params = path.split("/");
                for (int i = 1; i < params.length; i++) {
                    if (1 == i) {
                        path = params[i];
                    } else {
                        urlParams.add(params[i]);
                    }
                }
            }
            if (name.compareToIgnoreCase(path) != 0) {
                name = path.substring(1);
                if (name.lastIndexOf("/")+1 == name.length()) {
                    name = name.substring(0, name.length()-1);
                }
            }
            
            userSession = request.getSession();
            String content = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

            Gson gson = new Gson();
            List<JsonFormParameter> map;
            map = (List<JsonFormParameter>) gson.fromJson(content, new TypeToken<List<JsonFormParameter>>() {}.getType());

            // fetch site by name or aliasname
            CfSite cfsite = null;
            try {
                cfsite = cfsiteService.findByName(name);
            } catch (Exception ex) {
                try {
                    cfsite = cfsiteService.findByAliaspath(name);
                    name = cfsite.getName();
                    alias = true;
                } catch (Exception e1) {
                    try {
                        cfsite = cfsiteService.findByShorturl(name);
                        name = cfsite.getName();
                        alias = true;
                    } catch (Exception ey) {
                        throw new PageNotFoundException("PageNotFound Exception: " + name);
                    }
                }
            }
            if (cfsite.isSearchresult()) {
                if (searchcontentmap.isEmpty()) {
                    String query = "";
                    for (JsonFormParameter jsp : map) {
                        if (0 == jsp.getName().compareToIgnoreCase("search")) {
                            query += jsp.getValue();
                        }
                    }
                    String[] searchexpressions = query.split(" ");
                    searchUtil.updateSearchhistory(searchexpressions);

                    searcher.setIndexPath(folderUtil.getIndex_folder());
                    long startTime = System.currentTimeMillis();
                    SearchResult searchresult = searcher.search(query, searchlimit);
                    long endTime = System.currentTimeMillis();

                    LOGGER.info("Search Time :" + (endTime - startTime));
                    searchmetadata.clear();
                    searchmetadata.put("cfSearchQuery", query);
                    searchmetadata.put("cfSearchTime", String.valueOf(endTime - startTime));
                    searchcontentmap.clear();
                    searchresult.getFoundSites().stream().forEach((site) -> {
                        searchcontentmap.put(site.getName(), site);
                    });
                    searchassetmap.clear();
                    searchresult.getFoundAssets().stream().forEach((asset) -> {
                        searchassetmap.put(asset.getName(), asset);
                    });
                    searchassetmetadatamap.clear();
                    searchresult.getFoundAssetsMetadata().keySet().stream().forEach((key) -> {
                        searchassetmetadatamap.put(key, searchresult.getFoundAssetsMetadata().get(key));
                    });
                    searchclasscontentmap.clear();
                    searchresult.getFoundClasscontent().keySet().stream().forEach((key) -> {
                        searchclasscontentmap.put(key, searchresult.getFoundClasscontent().get(key));
                    });
                }
            }

            addHeader(response, clownfishutil.getVersion());
            
            RenderContext rc = new RenderContext();
            rc.setName(name);
            rc.setPostmap(map);
            rc.setUrlParams(urlParams);
            rc.setMakestatic(false);
            rc.setFileitems(null);
            rc.setClientinfo(clientinfo);
            rc.setReferrer("");
            rc.setSearchassetmap(searchassetmap);
            rc.setSearchassetmetadatamap(searchassetmetadatamap);
            rc.setSearchclasscontentmap(searchclasscontentmap);
            rc.setSearchcontentmap(searchcontentmap);
            rc.setSearchmetadata(searchmetadata);
            ClownfishResponse cfResponse = pagerenderservice.renderPage(rc);
            
            if (cfResponse.getErrorcode() == 0) {
                response.setContentType(this.contenttype);
                response.setCharacterEncoding(this.characterencoding);
                ServletOutputStream out = response.getOutputStream();
                out.write(cfResponse.getOutput().getBytes(this.characterencoding)); 
            } else {
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                ServletOutputStream out = response.getOutputStream();
                out.write(cfResponse.getOutput().getBytes(this.characterencoding)); 
            }
        } catch (IOException | PageNotFoundException | IllegalStateException | ParseException ex) {
            LOGGER.error(ex.getMessage());
        }
    }


    /**
     * addHeader
     * 
     */
    private void addHeader(HttpServletResponse response, String version) {
        String serverString = servername.replaceAll("#version#", version);
        String serverxpowerdedString = serverxpowered.replaceAll("#version#", version);
        response.addHeader("Server", serverString);
        response.addHeader("X-Powered-By", serverxpowerdedString);
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

    private ClientInformation getClientinformation(String ip) {
        ClientInformation ci = new ClientInformation();
        ci.setIpadress(ip);
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(ip);
            ci.setHostname(addr.getHostName());
        } catch (UnknownHostException ex) {
            java.util.logging.Logger.getLogger(Clownfish.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ci;
    }
    
    private String getCookieVal(Cookie[] cookies, String key) {
        if (null != cookies) {
            if (cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    if (0 == cookie.getName().compareToIgnoreCase(key)) {
                        String value = cookie.getValue();
                        cookie.setMaxAge(0);
                        cookie.setValue("");
                        return value;
                    }
                }
                return "";
            } else {
                return "";
            }
        } else {
            return "";
        }
    }
}
