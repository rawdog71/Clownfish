package io.clownfish.clownfish;

import KNSAPTools.SAPConnection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import io.clownfish.clownfish.beans.JsonFormParameter;
import static io.clownfish.clownfish.beans.SiteTreeBean.SAPCONNECTION;
import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.dbentities.CfUser;
import io.clownfish.clownfish.interceptor.GzipSwitch;
import io.clownfish.clownfish.mail.EmailProperties;
import io.clownfish.clownfish.sap.RFC_GET_FUNCTION_INTERFACE;
import io.clownfish.clownfish.sap.RPY_TABLE_READ;
import io.clownfish.clownfish.serviceimpl.CfTemplateLoaderImpl;
import io.clownfish.clownfish.serviceinterface.CfJavascriptService;
import io.clownfish.clownfish.serviceinterface.CfJavascriptversionService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetversionService;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
import io.clownfish.clownfish.serviceinterface.CfUserService;
import io.clownfish.clownfish.utils.ClownfishUtil;
import io.clownfish.clownfish.utils.MailUtil;
import io.clownfish.clownfish.utils.TemplateUtil;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author rawdog71
 */
@RestController
@EnableAutoConfiguration(exclude = HibernateJpaAutoConfiguration.class)
public class Clownfish {
    //@Autowired CfUserService cfuserService;
    @Autowired CfSiteService cfsiteService;
    @Autowired CfTemplateService cftemplateService;
    @Autowired CfTemplateversionService cftemplateversionService;
    @Autowired CfStylesheetService cfstylesheetService;
    @Autowired CfStylesheetversionService cfstylesheetversionService;
    @Autowired CfJavascriptService cfjavascriptService;
    @Autowired CfJavascriptversionService cfjavascriptversionService;
    @Autowired TemplateUtil templateUtil;
    
    
    
    @Context
    private UriInfo context;
    @Context
    protected HttpServletResponse response;
    @Context 
    protected HttpServletRequest request;
    
    private GzipSwitch gzipswitch;
    private freemarker.template.Configuration freemarkerCfg;
    private RFC_GET_FUNCTION_INTERFACE rfc_get_function_interface = null;
    private RPY_TABLE_READ rpytableread = null;
    private static final SAPConnection sapc = new SAPConnection(SAPCONNECTION, "Gemini1");
    private boolean sapSupport = false;
    private Map<String, String> propertymap = null;
    private HttpSession userSession;
    private int modus = 1;
    private ClownfishUtil clownfishutil;
    private String characterEncoding;
    private String contentType;
    private Locale locale;

    @RequestMapping("/")
    String home() {
        //CfUser cfUser = cfuserService.findById(1L);
        
        return "Hello Clownfish ";
    }
    
    /*
    @PostConstruct
    public void init() {
        // Set default values
        modus = 1;  // 1 = Staging mode (fetch sourcecode from commited repository) <= default
                    // 0 = Development mode (fetch sourcecode from database)
        characterEncoding = "UTF-8";
        contentType = "text/html";
        locale = new Locale("de");

        // read all System Properties of the property table
        propertymap = new PropertyList().init(em);
        geminiutil = new GeminiUtil();
        String sapSupportProp = propertymap.get("sap.support");
        if (sapSupportProp.compareToIgnoreCase("true") == 0) {
            sapSupport = true;
        }
        if (sapSupport) {
            //Class<?> clazz = Class.forName("KNSAPTools.SAPConnection");
            //Object sapcinstance = clazz.newInstance();
            
            rfc_get_function_interface = new RFC_GET_FUNCTION_INTERFACE(sapc);
            rpytableread = new RPY_TABLE_READ(sapc);
        }
        // Override default values with system properties
        String systemContentType = propertymap.get("response.contenttype");
        String systemCharacterEncoding = propertymap.get("response.characterencoding");
        String systemLocale = propertymap.get("response.locale");
        if (!systemCharacterEncoding.isEmpty()) {
            characterEncoding = systemCharacterEncoding;
        }
        if (!systemContentType.isEmpty()) {
            contentType = systemContentType;
        }
        if (!systemLocale.isEmpty()) {
            locale = new Locale(systemLocale);
        }
        this.gzipswitch = new GzipSwitch();
    }
    */
    
    /**
     * Creates a new instance of GenericResource
     */
    public Clownfish() {
    }

    /**
     * Retrieves representation of an instance of de.koenigneurath.freemarkertest.GenericResource
     * @param name
     * @param request
     * @return an instance of java.lang.String
     */
    /*
    @GET
    @Compress
    @Path("{name}")
    @Produces(MediaType.TEXT_HTML)
    public String getHtml(@PathParam("name") String name, @Context HttpServletRequest request) {
        userSession = request.getSession();
        MultivaluedMap querymap = context.getQueryParameters();
        ArrayList queryParams = new ArrayList();
        for (Object key : querymap.keySet()) {
            JsonFormParameter jfp = new JsonFormParameter();
            jfp.setName((String) key);
            List values = (List) querymap.get(key);
            jfp.setValue((String) values.get(0));
            queryParams.add(jfp);
        }
        return makeHTML(name, queryParams);
    }
    */
    /**
     * PUT method for updating or creating an instance of GenericResource
     * @param content representation for the resource
     */
    /*
    @PUT
    @Consumes(MediaType.TEXT_HTML)
    public void putHtml(String content) {
    }
    */
    
    /*
    @POST
    @Compress
    @Path("{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public String postHtml(@PathParam("name") String name, String content, @Context HttpServletRequest request) {
        userSession = request.getSession();
        Gson gson = new Gson(); 
        List<JsonFormParameter> map;
        map = (List<JsonFormParameter>) gson.fromJson(content, new TypeToken<List<JsonFormParameter>>() {}.getType());
        
        return makeHTML(name, map);
    }
    */
    
    private String makeResponse(String name, List<JsonFormParameter> postmap) {
        try {
            // Freemarker Template
            freemarker.template.Template fmTemplate = null;
            Map fmRoot = null;
            
            // Velocity Template
            org.apache.velocity.VelocityContext velContext = null;
            org.apache.velocity.Template velTemplate = null;

            // Hole die Parameter Liste
            Map parametermap = clownfishutil.getParametermap(postmap);
            if (parametermap.containsKey("modus")) {    // check mode for display (stageing or dev)
                if (parametermap.get("modus").toString().compareToIgnoreCase("dev") == 0) {
                    modus = 0;
                }
            }
            
            // Hole die Seite über den Namen
            //CfSite knsite = (Knsite) em.createNamedQuery("Knsite.findByName").setParameter("name", name).getSingleResult();
            CfSite cfsite = cfsiteService.findByName(name);
            if ((cfsite.getContenttype() != null)) {
                if (!cfsite.getContenttype().isEmpty()) response.setContentType(cfsite.getContenttype());
            }
            if ((cfsite.getCharacterencoding() != null)) {
                if (!cfsite.getCharacterencoding().isEmpty()) response.setCharacterEncoding(cfsite.getCharacterencoding());
            }
            if ((cfsite.getLocale() != null)) {
                if (!cfsite.getLocale().isEmpty()) response.setLocale(new Locale(cfsite.getLocale()));
            }
            
            try {
                //CfTemplate kntemplate = (Kntemplate) em.createNamedQuery("Kntemplate.findById").setParameter("id", knsite.getTemplateref()).getSingleResult();
                CfTemplate cftemplate = cftemplateService.findById(cfsite.getTemplateref());
                if (cftemplate.getScriptlanguage() == 0) {  // Freemarker Template
                    CfTemplateLoaderImpl loader = new CfTemplateLoaderImpl();
                    fmRoot = new LinkedHashMap();
                    
                    loader.setModus(modus);
                    
                    // Hole das zugehörige Template über den name
                    freemarkerCfg = new freemarker.template.Configuration();
                    freemarkerCfg.setDefaultEncoding("UTF-8");
                    freemarkerCfg.setTemplateLoader(loader);
                    freemarkerCfg.setLocalizedLookup(false);
                    freemarkerCfg.setLocale(Locale.GERMANY);
                    
                    fmTemplate = freemarkerCfg.getTemplate(cftemplate.getName());
                } else {                                    // Velocity Template
                    velContext = new org.apache.velocity.VelocityContext();
                    
                    velTemplate = new org.apache.velocity.Template();
                    org.apache.velocity.runtime.RuntimeServices runtimeServices = org.apache.velocity.runtime.RuntimeSingleton.getRuntimeServices();
                    String templateContent;
                    if (0 == modus) {
                        templateContent = cftemplate.getContent();
                    } else {
                        long currentTemplateVersion;
                        try {
                            //currentTemplateVersion = (long) em.createNamedQuery("Kntemplateversion.findMaxVersion").setParameter("templateref", kntemplate.getId()).getSingleResult();
                            currentTemplateVersion = cftemplateversionService.findMaxVersion(cftemplate.getId());
                        } catch (NullPointerException ex) {
                            currentTemplateVersion = 0;
                        }
                        //TemplateUtil templateUtility = new TemplateUtil(em);
                        templateContent = templateUtil.getVersion(cftemplate.getId(), currentTemplateVersion);
                    }
                    templateContent = templateUtil.fetchIncludes(templateContent, modus);
                    StringReader reader = new StringReader(templateContent);
                    velTemplate.setRuntimeServices(runtimeServices);
                    velTemplate.setData(runtimeServices.parse(reader, cftemplate.getName()));
                    velTemplate.initDocument();
                }
                
                // Hole das Stylesheet, falls vorhanden
                String cfstylesheet = "";
                if (cfsite.getStylesheetref() != null) {
                    //knstylesheet = ((Knstylesheet) em.createNamedQuery("Knstylesheet.findById").setParameter("id", knsite.getStylesheetref()).getSingleResult()).getContent();
                    cfstylesheet = ((CfStylesheet) cfstylesheetService.findById(cfsite.getStylesheetref().longValue())).getContent();
                }

                // Hole das Javascript, falls vorhanden
                String cfjavascript = "";
                if (cfsite.getJavascriptref()!= null) {
                    //knjavascript = ((Knjavascript) em.createNamedQuery("Knjavascript.findById").setParameter("id", knsite.getJavascriptref()).getSingleResult()).getContent();
                    cfjavascript = ((CfJavascript) cfjavascriptService.findById(cfsite.getJavascriptref().longValue())).getContent();
                }
                
                SiteUtil siteutil = new SiteUtil(em);
                
                // Hole sämtlichen Content, der zu dieser Seite referenziert ist
                List<Knsitecontent> sitecontentlist = new ArrayList<>();
                sitecontentlist.addAll(em.createNamedQuery("Knsitecontent.findBySiteref").setParameter("siteref", knsite.getId()).getResultList());
                Map sitecontentmap = siteutil.getSitecontentmap(sitecontentlist);
                
                // Hole sämtliche Listen, die zu dieser Seite referenziert sind
                siteutil.getSitelist_list(knsite, sitecontentmap);

                // Manage Parameters 
                HashMap<String, DatatableProperties> datatableproperties = geminiutil.getDatatableproperties(postmap);
                EmailProperties emailproperties = geminiutil.getEmailproperties(postmap);
                HashMap<String, DatatableNewProperties> datatablenewproperties = geminiutil.getDatatablenewproperties(postmap);
                HashMap<String, DatatableDeleteProperties> datatabledeleteproperties = geminiutil.getDatatabledeleteproperties(postmap);
                HashMap<String, DatatableUpdateProperties> datatableupdateproperties = geminiutil.getDatatableupdateproperties(postmap);
                manageSessionVariables(postmap);
                writeSessionVariables(parametermap);
                
                // Hole die Datenquellen zu dieser Seite
                List<Knsitedatasource> sitedatasourcelist = new ArrayList<>();
                sitedatasourcelist.addAll(em.createNamedQuery("Knsitedatasource.findBySiteref").setParameter("siteref", knsite.getId()).getResultList());

                DatabaseUtil databaseUtil = new DatabaseUtil(em);
                HashMap<String, HashMap> dbexport = databaseUtil.getDbexport(sitedatasourcelist, datatableproperties, datatablenewproperties, datatabledeleteproperties, datatableupdateproperties);
                sitecontentmap.put("db", dbexport);

                // Hole die SAP RFCs zu dieser Seite
                if (sapSupport) {
                    List<Knsitesaprfc> sitesaprfclist = new ArrayList<>();
                    sitesaprfclist.addAll(em.createNamedQuery("Knsitesaprfc.findBySiteref").setParameter("siteref", knsite.getId()).getResultList());
                    HashMap<String, List> saprfcfunctionparamMap = geminiutil.getSaprfcfunctionparamMap(sitesaprfclist, rfc_get_function_interface);
                    
                    //Class<?> clazz = Class.forName("KNSAPTools.SAPConnection");
                    //Object sapcinstance = clazz.newInstance();
                    
                    HashMap<String, HashMap> sapexport = new SAPUtility(sapc).getSapExport(sitesaprfclist, saprfcfunctionparamMap, postmap, rpytableread);
                    
                    //HashMap<String, HashMap> sapexport = getSapExport(sitesaprfclist, saprfcfunctionparamMap, postmap);
                    sitecontentmap.put("sap", sapexport);
                }
                
                // Send Email
                if (emailproperties != null) {
                    try {
                        sendRespondMail(emailproperties.getSendto(), emailproperties.getSubject(), emailproperties.getBody());
                    } catch (Exception ex) {
                        Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                Writer out = new StringWriter();
                if (kntemplate.getScriptlanguage() == 0) {  // Freemarker Template
                    DatabaseBean databasebean = new DatabaseBean(em, sitedatasourcelist, sitecontentmap);
                    fmRoot.put("databaseBean", databasebean);
                    fmRoot.put("css", knstylesheet);
                    fmRoot.put("js", knjavascript);
                    fmRoot.put("sitecontent", sitecontentmap); 
                    fmRoot.put("parameter", parametermap);
                    
                    freemarker.core.Environment env = fmTemplate.createProcessingEnvironment(fmRoot, out);
                    env.process();
                } else {                                    // Velocity Template
                    DatabaseBean databasebean = new DatabaseBean(em, sitedatasourcelist, sitecontentmap);
                    velContext.put("databaseBean", databasebean);
                    velContext.put("css", knstylesheet);
                    velContext.put("js", knjavascript);
                    velContext.put("sitecontent", sitecontentmap); 
                    velContext.put("parameter", parametermap);
                    
                    velTemplate.merge(velContext, out);
                }
                String gzip;
                gzip = propertymap.get("html.gzip");
                if (gzip == null) {
                     gzip = "off";
                }
                switch (knsite.getGzip()) {
                    case 1:
                        gzip = "on";
                        break;
                    case 2:
                        gzip = "off";
                        break;    
                }
                if (gzip.compareToIgnoreCase("on") == 0) {
                    gzipswitch.setGzipon(true);
                }
                
                String htmlcompression;
                htmlcompression = propertymap.get("html.compression");
                if (htmlcompression == null) {
                     htmlcompression = "off";
                }
                switch (knsite.getHtmlcompression()) {
                    case 1:
                        htmlcompression = "on";
                        break;
                    case 2:
                        htmlcompression = "off";
                        break;    
                }
                if (htmlcompression.compareToIgnoreCase("on") == 0) {
                    HtmlCompressor htmlcompressor = new HtmlCompressor();
                    htmlcompressor.setRemoveSurroundingSpaces(HtmlCompressor.ALL_TAGS);
                    htmlcompressor.setPreserveLineBreaks(false);
                    htmlcompressor.setCompressCss(false);

                    return htmlcompressor.compress(out.toString());
                } else {
                    return out.toString();
                }
            } catch (NoResultException ex) {
                return "No template";
            }     
        } catch (IOException | freemarker.template.TemplateException | org.apache.velocity.runtime.parser.ParseException ex) {
            //Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        } 
    }
    
    private void sendRespondMail(String mailto, String subject, String mailbody) throws Exception {
        MailUtil mailutil = new MailUtil(propertymap.get("mail.smtp.host"), propertymap.get("mail.transport.protocol"), propertymap.get("mail.user"), propertymap.get("mail.password"), propertymap.get("mail.sendfrom"));
        mailutil.sendRespondMail(mailto, subject, mailbody);
    }

    private void manageSessionVariables(List<JsonFormParameter> postmap) {
        if (postmap != null) {
            for (JsonFormParameter jfp : postmap) {
                if (jfp.getName().startsWith("session")) {
                    userSession.setAttribute(jfp.getName(), jfp.getValue());
                }
            }
        }
    }
    
    private void writeSessionVariables(Map parametermap) {
        for (String key : Collections.list(userSession.getAttributeNames())) {
            if (key.startsWith("session")) {
                String attributevalue = (String) userSession.getAttribute(key);
                parametermap.put(key, attributevalue);
            }
        }
    }
    
}
