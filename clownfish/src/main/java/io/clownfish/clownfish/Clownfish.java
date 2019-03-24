package io.clownfish.clownfish;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.clownfish.clownfish.beans.JsonFormParameter;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfUser;
import io.clownfish.clownfish.mail.EmailProperties;
import io.clownfish.clownfish.serviceinterface.CfUserService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
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

    @RequestMapping("/")
    String home() {
        //CfUser cfUser = cfuserService.findById(1L);
        
        return "Hello Clownfish ";
    }
    
    @PostConstruct
    public void init() {
        // read all System Properties of the property table
        propertymap = new PropertyList().init(em);
        
        String sapSupportProp = propertymap.get("sap.support");
        if (sapSupportProp.compareToIgnoreCase("true") == 0) {
            sapSupport = true;
        }
        if (sapSupport) {
            rfc_get_function_interface = new RFC_GET_FUNCTION_INTERFACE(sapc);
            rpytableread = new RPY_TABLE_READ(sapc);
        }
    }
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

    /**
     * PUT method for updating or creating an instance of GenericResource
     * @param content representation for the resource
     */
    @PUT
    @Consumes(MediaType.TEXT_HTML)
    public void putHtml(String content) {
    }
    
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
    
    private String makeHTML(String name, List<JsonFormParameter> postmap) {
        try {
            // Freemarker Template
            freemarker.template.Template fmTemplate = null;
            Map fmRoot = null;
            
            // Velocity Template
            org.apache.velocity.VelocityContext velContext = null;
            org.apache.velocity.Template velTemplate = null;
            
            // Hole die Seite über den Namen
            CfSite knsite = (CfSite) em.createNamedQuery("Knsite.findByName").setParameter("name", name).getSingleResult();
            try {
                Kntemplate kntemplate = (Kntemplate) em.createNamedQuery("Kntemplate.findById").setParameter("id", knsite.getTemplateref()).getSingleResult();
                if (kntemplate.getScriptlanguage() == 0) {  // Freemarker Template
                    KNTemplateLoaderImpl loader = new KNTemplateLoaderImpl(em);
                    fmRoot = new LinkedHashMap();
                    
                    // Hole das zugehörige Template über den name
                    freemarkerCfg = new freemarker.template.Configuration();
                    freemarkerCfg.setDefaultEncoding("UTF-8");
                    freemarkerCfg.setTemplateLoader(loader);
                    freemarkerCfg.setLocalizedLookup(false);
                    freemarkerCfg.setLocale(Locale.GERMANY);
                    
                    fmTemplate = freemarkerCfg.getTemplate(kntemplate.getName());
                } else {                                    // Velocity Template
                    velContext = new org.apache.velocity.VelocityContext();
                    
                    velTemplate = new org.apache.velocity.Template();
                    org.apache.velocity.runtime.RuntimeServices runtimeServices = org.apache.velocity.runtime.RuntimeSingleton.getRuntimeServices();
                    String templatecontent = new TemplateUtil(em).fetchIncludes(kntemplate.getContent());
                    StringReader reader = new StringReader(templatecontent);
                    velTemplate.setRuntimeServices(runtimeServices);
                    velTemplate.setData(runtimeServices.parse(reader, kntemplate.getName()));
                    velTemplate.initDocument();
                }
                
                // Hole das Stylesheet, falls vorhanden
                String knstylesheet = "";
                if (knsite.getStylesheetref() != null) {
                    knstylesheet = ((Knstylesheet) em.createNamedQuery("Knstylesheet.findById").setParameter("id", knsite.getStylesheetref()).getSingleResult()).getContent();
                }

                // Hole das Javascript, falls vorhanden
                String knjavascript = "";
                if (knsite.getJavascriptref()!= null) {
                    knjavascript = ((Knjavascript) em.createNamedQuery("Knjavascript.findById").setParameter("id", knsite.getJavascriptref()).getSingleResult()).getContent();
                }

                // Hole die Parameter Liste
                Map parametermap = getParametermap(postmap);

                // Hole sämtlichen Content, der zu dieser Seite referenziert ist
                List<Knsitecontent> sitecontentlist = new ArrayList<>();
                sitecontentlist.addAll(em.createNamedQuery("Knsitecontent.findBySiteref").setParameter("siteref", knsite.getId()).getResultList());
                Map sitecontentmap = getSitecontentmap(sitecontentlist);
                
                // Hole sämtliche Listen, die zu dieser Seite referenziert sind
                List<Knsitelist> sitelist_list = getSitelist_list(knsite, sitecontentmap);

                // Manage Parameters 
                HashMap<String, DatatableProperties> datatableproperties = getDatatableproperties(postmap);
                EmailProperties emailproperties = getEmailproperties(postmap);
                HashMap<String, DatatableNewProperties> datatablenewproperties = getDatatablenewproperties(postmap);
                HashMap<String, DatatableDeleteProperties> datatabledeleteproperties = getDatatabledeleteproperties(postmap);
                HashMap<String, DatatableUpdateProperties> datatableupdateproperties = getDatatableupdateproperties(postmap);
                manageSessionVariables(postmap);
                writeSessionVariables(parametermap);
                
                // Hole die Datenquellen zu dieser Seite
                List<Knsitedatasource> sitedatasourcelist = new ArrayList<>();
                sitedatasourcelist.addAll(em.createNamedQuery("Knsitedatasource.findBySiteref").setParameter("siteref", knsite.getId()).getResultList());

                HashMap<String, HashMap> dbexport = getDbexport(sitedatasourcelist, datatableproperties, datatablenewproperties, datatabledeleteproperties, datatableupdateproperties, sitecontentmap);

                // Hole die SAP RFCs zu dieser Seite
                if (sapSupport) {
                    List<Knsitesaprfc> sitesaprfclist = new ArrayList<>();
                    sitesaprfclist.addAll(em.createNamedQuery("Knsitesaprfc.findBySiteref").setParameter("siteref", knsite.getId()).getResultList());
                    HashMap<String, List> saprfcfunctionparamMap = getSaprfcfunctionparamMap(sitesaprfclist);
                    HashMap<String, HashMap> sapexport = getSapExport(sitesaprfclist, saprfcfunctionparamMap, postmap);
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
                String htmlcompression;
                htmlcompression = propertymap.get("html.compression");
                if (htmlcompression == null) {
                     htmlcompression = "off";
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
    
    private Map getattributmap (Knclasscontent classcontent) {
        List<Knattributcontent> attributcontentlist = new ArrayList<>();
        attributcontentlist.addAll(em.createNamedQuery("Knattributcontent.findByClasscontentref").setParameter("classcontentref", classcontent).getResultList());

        Map attributcontentmap = new LinkedHashMap();

        for (Knattributcontent attributcontent : attributcontentlist) {
            Knattribut knattribut = (Knattribut) em.createNamedQuery("Knattribut.findById").setParameter("id", attributcontent.getAttributref().getId()).getSingleResult();
            Knattributetype knattributtype = (Knattributetype) em.createNamedQuery("Knattributetype.findById").setParameter("id", knattribut.getAttributetype().getId()).getSingleResult();
            switch (knattributtype.getName()) {
                case "boolean":
                    attributcontentmap.put(knattribut.getName(), attributcontent.getContentBoolean());
                    break;
                case "string":
                    attributcontentmap.put(knattribut.getName(), attributcontent.getContentString());
                    break;
                case "hashstring":
                    attributcontentmap.put(knattribut.getName(), attributcontent.getContentString());
                    break;    
                case "integer":
                    attributcontentmap.put(knattribut.getName(), attributcontent.getContentInteger());
                    break;
                case "real":
                    attributcontentmap.put(knattribut.getName(), attributcontent.getContentReal());
                    break;
                case "text":
                    attributcontentmap.put(knattribut.getName(), attributcontent.getContentText());
                    break;
                case "datetime":
                    attributcontentmap.put(knattribut.getName(), attributcontent.getContentDate());
                    break;
                case "media":
                    attributcontentmap.put(knattribut.getName(), attributcontent.getContentInteger());
                    break;    
            }
        }
        return attributcontentmap;
    }
    
    private String getFieldType(ArrayList<TableField> tableFieldsList, String fieldname) {
        for (TableField tf : tableFieldsList) {
            if (tf.getName().compareToIgnoreCase(fieldname) == 0) {
                return tf.getType();
            }
        }
        return null;
    }
    
    private void manageTableRead(Connection con, DatabaseMetaData dmd, String tablename, HashMap<String, DatatableProperties> datatableproperties, HashMap<String, ArrayList> dbtables, HashMap<String, Object> dbvalues) {
        try {
            long low_limit = 1;
            long high_limit = 50;
            
            //System.out.println(con.getMetaData().getDriverName());

            String default_order = "";
            TableFieldStructure tfs = getTableFieldsList(dmd, tablename, default_order);
            default_order = tfs.getDefault_order();
            
            String default_direction = "ASC";
            DatatableProperties dtp = datatableproperties.get(tablename);
            if (dtp != null) {
                if (dtp.getOrderby() != null) {
                    default_order = datatableproperties.get(tablename).getOrderby();
                }
                if (dtp.getOrderdir() != null) {
                    default_direction = datatableproperties.get(tablename).getOrderdir();
                }
                low_limit = 1 + ((dtp.getPage()-1) * dtp.getPagination());
                if (con.getMetaData().getDriverName().contains("MS SQL")) {
                    high_limit = dtp.getPage() * dtp.getPagination();
                }
                if (con.getMetaData().getDriverName().contains("MySQL")) {
                    high_limit = dtp.getPagination();
                }
            }
            
            StringBuilder sql_count = new StringBuilder();
            StringBuilder sql_outer = new StringBuilder();
            StringBuilder sql_inner = new StringBuilder();
            
            if (con.getMetaData().getDriverName().contains("MS SQL")) {
                sql_outer.append("SELECT ");
                sql_inner.append("SELECT ");
                sql_count.append("SELECT COUNT(*) AS count FROM ");
                
                if ((dtp != null) && (dtp.getGroupbylist().size() > 0)) {
                    sql_outer.append("count(*) AS groupbycount, ");
                    for (TableField tf : tfs.getTableFieldsList()) {
                        if (dtp.getGroupbylist().contains(tf.getName())) {
                            sql_outer.append(tf.getName());
                            sql_outer.append(", ");
                            sql_inner.append(tf.getName());
                            sql_inner.append(", ");
                        }
                    }
                } else {
                    for (TableField tf : tfs.getTableFieldsList()) {
                        sql_outer.append(tf.getName());
                        sql_outer.append(", ");
                        sql_inner.append(tf.getName());
                        sql_inner.append(", ");
                    }
                }
                sql_count.append(tablename);
                sql_outer.delete(sql_outer.length()-2, sql_outer.length());
                sql_outer.append(" FROM (");
                sql_inner.append("ROW_NUMBER() OVER (ORDER BY ");
                sql_inner.append(default_order);
                sql_inner.append(" ");
                sql_inner.append(default_direction);
                sql_inner.append(" ) AS rownumber FROM ");
                sql_inner.append(tablename);
                StringBuilder sql_condition = null;
                if (dtp != null) {
                    sql_condition = buildCondition(dtp.getConditionlist(), tfs.getTableFieldsList());
                }
                sql_inner.append(sql_condition);
                sql_count.append(sql_condition);
                StringBuilder sql_groupby = null;
                if (dtp != null) {
                    sql_groupby = buildGroupBy(dtp.getGroupbylist());
                }
                sql_inner.append(sql_groupby);
                if (dtp != null) {
                    if (!dtp.getGroupbycount().isEmpty()) {
                        sql_inner.append(" ,");
                        sql_inner.append(dtp.getGroupbycount());
                    }
                }
                sql_count.append(sql_groupby);
                
                sql_outer.append(sql_inner);
                sql_outer.append(") orderedselection WHERE rownumber between ");
                sql_outer.append(low_limit);
                sql_outer.append(" AND ");
                sql_outer.append(high_limit);
                sql_outer.append(sql_groupby);
            } 
            if (con.getMetaData().getDriverName().contains("MySQL")) {
                sql_outer.append("SELECT ");
                sql_count.append("SELECT COUNT(*) AS count FROM ");
                
                if ((dtp != null) && (dtp.getGroupbylist().size() > 0)) {
                    sql_outer.append("count(*) AS groupbycount, ");
                    for (TableField tf : tfs.getTableFieldsList()) {
                        if (dtp.getGroupbylist().contains(tf.getName())) {
                            sql_outer.append(tf.getName());
                            sql_outer.append(", ");
                        }
                    }
                } else {
                    for (TableField tf : tfs.getTableFieldsList()) {
                        sql_outer.append(tf.getName());
                        sql_outer.append(", ");
                    }
                }
                
                sql_count.append(tablename);
                sql_outer.delete(sql_outer.length()-2, sql_outer.length());
                sql_outer.append(" FROM ");
                sql_outer.append(tablename);
                StringBuilder sql_condition = null;
                if (dtp != null) {
                    sql_condition = buildCondition(dtp.getConditionlist(), tfs.getTableFieldsList());
                }
                sql_outer.append(sql_condition);
                sql_count.append(sql_condition);
                
                StringBuilder sql_groupby = null;
                if (dtp != null) {
                    sql_groupby = buildGroupBy(dtp.getGroupbylist());
                }
                sql_outer.append(sql_groupby);
                if (dtp != null) {
                    if (!dtp.getGroupbycount().isEmpty()) {
                        sql_outer.append(" ,");
                        sql_outer.append(dtp.getGroupbycount());
                    }
                }
                sql_count.append(sql_groupby);
                
                sql_outer.append(" LIMIT ");
                sql_outer.append(low_limit-1);
                sql_outer.append(", ");
                sql_outer.append(high_limit);
                sql_outer.append(sql_groupby);
            }

            if (dtp != null) {
                if (!dtp.getGroupbycount().isEmpty()) {
                    TableField groupbycountfield = new TableField();
                    groupbycountfield.setName("groupbycount");
                    groupbycountfield.setType("INT");
                    tfs.getTableFieldsList().add(groupbycountfield);
                }
            }
            
            Statement stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(sql_outer.toString());
            ArrayList<HashMap> tablevalues = new ArrayList<>();
            while (result.next()) {
                HashMap<String, String> dbexportvalues = new HashMap<>();
                for (TableField tf : tfs.getTableFieldsList()) {
                    try {
                        String value = result.getString(tf.getName());
                        dbexportvalues.put(tf.getName(), value);
                    } catch (java.sql.SQLException ex) {
                        
                    }
                }
                tablevalues.add(dbexportvalues);
            }
            dbtables.put(tablename, tablevalues);
            result = stmt.executeQuery(sql_count.toString());
            HashMap<String, String> dbexportvalues = new HashMap<>();
            while (result.next()) {
                String value = result.getString("count");
                dbexportvalues.put("count", value);
            }
            dbvalues.put(tablename, dbexportvalues);
        } catch (SQLException ex) {
            Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean manageTableInsert(Connection con, DatabaseMetaData dmd, String tablename, HashMap<String, DatatableNewProperties> datatablenewproperties, HashMap<String, ArrayList> dbtables, HashMap<String, Object> dbvalues) {
        try {
            TableFieldStructure tfs = getTableFieldsList(dmd, tablename, "");
            DatatableNewProperties dtnp = datatablenewproperties.get(tablename);
            
            StringBuilder sql_insert_fields = new StringBuilder();
            StringBuilder sql_insert_values = new StringBuilder();
            for (DatatableNewValue dtnv : dtnp.getValuelist()) {
                sql_insert_fields.append(dtnv.getField());
                sql_insert_fields.append(", ");
                String fieldType = getFieldType(tfs.getTableFieldsList(), dtnv.getField());
                if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                    sql_insert_values.append("'");
                }
                sql_insert_values.append(dtnv.getValue());
                if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                    sql_insert_values.append("'");
                }
                sql_insert_values.append(", ");
            }
            sql_insert_fields.delete(sql_insert_fields.length()-2, sql_insert_fields.length());
            sql_insert_values.delete(sql_insert_values.length()-2, sql_insert_values.length());
            
            StringBuilder sql_insert = new StringBuilder();
            sql_insert.append("INSERT INTO ");
            sql_insert.append(tablename);
            sql_insert.append(" (");
            sql_insert.append(sql_insert_fields);
            sql_insert.append(") VALUES (");
            sql_insert.append(sql_insert_values);
            sql_insert.append(")");
            
            Statement stmt = con.createStatement();
            int count = stmt.executeUpdate(sql_insert.toString());
            boolean ok = false;
            if (count > 0 ) {
                ok = true;
            }
            return ok;
        } catch (SQLException ex) {
            Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    private boolean manageTableDelete(Connection con, DatabaseMetaData dmd, String tablename, HashMap<String, DatatableDeleteProperties> datatabledeleteproperties, HashMap<String, ArrayList> dbtables, HashMap<String, Object> dbvalues) {
        try {
            TableFieldStructure tfs = getTableFieldsList(dmd, tablename, "");
            DatatableDeleteProperties dtdp = datatabledeleteproperties.get(tablename);
            
            StringBuilder sql_condition = new StringBuilder();
            if (dtdp != null) {
                sql_condition.append(" WHERE ");
                for (DatatableDeleteValue dtdv : dtdp.getValuelist()) {
                    sql_condition.append("(");
                    sql_condition.append(dtdv.getField());
                    String fieldType = getFieldType(tfs.getTableFieldsList(), dtdv.getField());
                    sql_condition.append(" = ");
                    if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                        sql_condition.append("'");
                    }
                    sql_condition.append(dtdv.getValue());
                    if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                        sql_condition.append("'");
                    }
                    sql_condition.append(") AND ");
                }
                sql_condition.delete(sql_condition.length()-4, sql_condition.length());
            }
            
            StringBuilder sql_delete = new StringBuilder();
            sql_delete.append("DELETE FROM ");
            sql_delete.append(tablename);
            sql_delete.append(sql_condition);
            
            Statement stmt = con.createStatement();
            int count = stmt.executeUpdate(sql_delete.toString());
            boolean ok = false;
            if (count > 0 ) {
                ok = true;
            }
            return ok;
        } catch (SQLException ex) {
            Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    private boolean manageTableUpdate(Connection con, DatabaseMetaData dmd, String tablename, HashMap<String, DatatableUpdateProperties> datatableproperties, HashMap<String, ArrayList> dbtables, HashMap<String, Object> dbvalues) {
        try {
            TableFieldStructure tfs = getTableFieldsList(dmd, tablename, "");
            DatatableUpdateProperties dtup = datatableproperties.get(tablename);
            
            StringBuilder sql_update_values = new StringBuilder();
            for (DatatableNewValue dtuv : dtup.getValuelist()) {
                sql_update_values.append(dtuv.getField());
                sql_update_values.append(" = ");
                String fieldType = getFieldType(tfs.getTableFieldsList(), dtuv.getField());
                if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                    sql_update_values.append("'");
                }
                sql_update_values.append(dtuv.getValue());
                if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                    sql_update_values.append("'");
                }
                sql_update_values.append(", ");
            }
            sql_update_values.delete(sql_update_values.length()-2, sql_update_values.length());
            
            StringBuilder sql_update = new StringBuilder();
            sql_update.append("UPDATE ");
            sql_update.append(tablename);
            sql_update.append(" SET ");
            sql_update.append(sql_update_values);
            StringBuilder sql_condition = new StringBuilder();
            if (dtup != null) {
                sql_condition = buildCondition(dtup.getConditionlist(), tfs.getTableFieldsList());
            }
            sql_update.append(sql_condition);
            
            Statement stmt = con.createStatement();
            int count = stmt.executeUpdate(sql_update.toString());
            boolean ok = false;
            if (count > 0 ) {
                ok = true;
            }
            return ok;
        } catch (SQLException ex) {
            Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    private void sendRespondMail(String mailto, String subject, String mailbody) throws Exception {
        String mailsmtphost = propertymap.get("mail.smtp.host");
        String mailtransportprotocol = propertymap.get("mail.transport.protocol");
        String mailuser = propertymap.get("mail.user");
        String mailpassword = propertymap.get("mail.password");
        String sendfrom = propertymap.get("mail.sendfrom");
        MailUtil mailutil = new MailUtil(mailsmtphost, mailtransportprotocol, mailuser, mailpassword, sendfrom);
        mailutil.sendRespondMail(mailto, subject, mailbody);
    }
    
    private TableFieldStructure getTableFieldsList(DatabaseMetaData dmd, String tablename, String default_order) {
        try {
            TableFieldStructure tfs = new TableFieldStructure();
            List<String> pkList = new ArrayList<>();
            ResultSet resultSetPK = dmd.getPrimaryKeys(null, null, tablename);
            int counter = 0;
            while(resultSetPK.next())
            {
                pkList.add(resultSetPK.getString("COLUMN_NAME"));
                if (counter == 0) {
                    default_order = resultSetPK.getString("COLUMN_NAME");
                }
                counter++;
            }
            
            ArrayList<TableField> tableFieldsList = new ArrayList<>();
            ResultSet columns = dmd.getColumns(null, null, tablename, null);
            while(columns.next())
            {
                String columnName = columns.getString("COLUMN_NAME");
                String datatype = columns.getString("DATA_TYPE");
                String columnsize = columns.getString("COLUMN_SIZE");
                String decimaldigits = columns.getString("DECIMAL_DIGITS");
                if (decimaldigits == null) {
                    decimaldigits = "0";
                }
                String isNullable = columns.getString("IS_NULLABLE");
                //String is_autoIncrment = columns.getString("IS_AUTOINCREMENT");
                String is_autoIncrment = "";
                
                if ((default_order.isEmpty()) && (counter == 0)) {
                    default_order = columnName;
                }
                TableField tf = null;
                switch (datatype) {
                    case "1":      // varchar -> String
                        tf = new TableField(columnName, "STRING", pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                        tableFieldsList.add(tf);
                        break;
                    case "2":       // int
                        tf = new TableField(columnName, "INT", pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                        tableFieldsList.add(tf);
                        break;
                    case "4":       // int
                        tf = new TableField(columnName, "INT", pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                        tableFieldsList.add(tf);
                        break;
                    case "5":       // smallint
                        tf = new TableField(columnName, "INT", pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                        tableFieldsList.add(tf);
                        break;    
                    case "8":       // float
                        tf = new TableField(columnName, "FLOAT", pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                        tableFieldsList.add(tf);
                        break;    
                    case "12":      // varchar -> String
                        tf = new TableField(columnName, "STRING", pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                        tableFieldsList.add(tf);
                        break;
                    case "-5":      // long
                        tf = new TableField(columnName, "LONG", pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                        tableFieldsList.add(tf);
                        break;
                    case "2005":    // text -> String
                        tf = new TableField(columnName, "STRING", pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                        tableFieldsList.add(tf);
                        break;
                    case "93":      // Date
                        tf = new TableField(columnName, "DATE", pkList.contains(columnName), Integer.parseInt(columnsize), Integer.parseInt(decimaldigits), isNullable);
                        tableFieldsList.add(tf);
                        break;
                }
            }
            tfs.setDefault_order(default_order);
            tfs.setTableFieldsList(tableFieldsList);
            return tfs;
        } catch (SQLException ex) {
            Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private StringBuilder buildGroupBy(ArrayList<String> groupbylist) {
        StringBuilder sql_groupby = new StringBuilder();
        if (!groupbylist.isEmpty()) {
            sql_groupby.append(" GROUP BY ");
            for (String groupby : groupbylist) {
                sql_groupby.append(groupby);
                sql_groupby.append(", ");
            }
            sql_groupby.delete(sql_groupby.length()-2, sql_groupby.length());
        }
        return sql_groupby;
    }
    
    private StringBuilder buildCondition(ArrayList<DatatableCondition> conditionlist, ArrayList<TableField> tableFieldsList) {
        StringBuilder sql_condition = new StringBuilder();
        if (!conditionlist.isEmpty()) {
            sql_condition.append(" WHERE ");
            for (DatatableCondition dtc : conditionlist) {
                sql_condition.append("(");
                sql_condition.append(dtc.getField());
                String fieldType = getFieldType(tableFieldsList, dtc.getField());
                switch (dtc.getOperand()) {
                    case "eq" : 
                        sql_condition.append(" = ");
                        if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                            sql_condition.append("'");
                        }
                        sql_condition.append(dtc.getValue());
                        if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                            sql_condition.append("'");
                        }
                        break;
                    case "lk" : 
                        sql_condition.append(" LIKE ");
                        if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0) || (fieldType.compareToIgnoreCase("int") == 0) || (fieldType.compareToIgnoreCase("float") == 0)) {
                            sql_condition.append("'%");
                        }
                        sql_condition.append(dtc.getValue());
                        if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0) || (fieldType.compareToIgnoreCase("int") == 0) || (fieldType.compareToIgnoreCase("float") == 0)) {
                            sql_condition.append("%'");
                        }
                        break;
                    case "gt" : 
                        sql_condition.append(" > ");
                        if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                            sql_condition.append("'");
                        }
                        sql_condition.append(dtc.getValue());
                        if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                            sql_condition.append("'");
                        }
                        break;
                    case "ge" : 
                        sql_condition.append(" >= ");
                        if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                            sql_condition.append("'");
                        }
                        sql_condition.append(dtc.getValue());
                        if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                            sql_condition.append("'");
                        }
                        break;    
                    case "lt" : 
                        sql_condition.append(" < ");
                        if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                            sql_condition.append("'");
                        }
                        sql_condition.append(dtc.getValue());
                        if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                            sql_condition.append("'");
                        }
                        break;
                    case "le" : 
                        sql_condition.append(" <= ");
                        if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                            sql_condition.append("'");
                        }
                        sql_condition.append(dtc.getValue());
                        if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                            sql_condition.append("'");
                        }
                        break;    
                    case "neq" : 
                        sql_condition.append(" <> ");
                        if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                            sql_condition.append("'");
                        }
                        sql_condition.append(dtc.getValue());
                        if ((fieldType.compareToIgnoreCase("string") == 0) || (fieldType.compareToIgnoreCase("date") == 0)) {
                            sql_condition.append("'");
                        }
                        break;
                }

                sql_condition.append(") AND ");
            }
            sql_condition.delete(sql_condition.length()-4, sql_condition.length());
        }
        return sql_condition;
    }
    
    private List<Knsitelist> getSitelist_list(Knsite knsite, Map sitecontentmap) {
        List<Knsitelist> sitelist_list = new ArrayList<>();
        sitelist_list.addAll(em.createNamedQuery("Knsitelist.findBySiteref").setParameter("siteref", knsite.getId()).getResultList());
        for (Knsitelist sitelist : sitelist_list) {
            Knlist knlist = (Knlist) em.createNamedQuery("Knlist.findById").setParameter("id", sitelist.getKnsitelistPK().getListref()).getSingleResult();
            Map listcontentmap = new LinkedHashMap();

            List<Knlistcontent> contentlist = em.createNamedQuery("Knlistcontent.findByListref").setParameter("listref", knlist.getId()).getResultList();
            for (Knlistcontent listcontent : contentlist) {
                Knclasscontent classcontent = (Knclasscontent) em.createNamedQuery("Knclasscontent.findById").setParameter("id", listcontent.getKnlistcontentPK().getClasscontentref()).getSingleResult();
                Knclass knclass = (Knclass) em.createNamedQuery("Knclass.findById").setParameter("id", classcontent.getClassref().getId()).getSingleResult();
                List<Knattributcontent> attributcontentlist = new ArrayList<>();
                attributcontentlist.addAll(em.createNamedQuery("Knattributcontent.findByClasscontentref").setParameter("classcontentref", classcontent).getResultList());
                listcontentmap.put(classcontent.getName(), getattributmap(classcontent));
            }
            sitecontentmap.put(knlist.getName(), listcontentmap);
        }
        return sitelist_list;
    }
    
    private HashMap<String, HashMap> getDbexport(List<Knsitedatasource> sitedatasourcelist, HashMap<String, DatatableProperties> datatableproperties, HashMap<String, DatatableNewProperties> datatablenewproperties, HashMap<String, DatatableDeleteProperties> datatabledeleteproperties, HashMap<String, DatatableUpdateProperties> datatableupdateproperties, Map sitecontentmap) {
        HashMap<String, HashMap> dbexport = new HashMap<>();
        for (Knsitedatasource sitedatasource : sitedatasourcelist) {
            Kndatasource kndatasource = (Kndatasource) em.createNamedQuery("Kndatasource.findById").setParameter("id", sitedatasource.getKnsitedatasourcePK().getDatasourceref()).getSingleResult();

            JDBCUtil jdbcutil = new JDBCUtil(kndatasource.getDriverclass(), kndatasource.getUrl(), kndatasource.getUser(), kndatasource.getPassword());
            Connection con = jdbcutil.getConnection();
            try {
                DatabaseMetaData dmd = con.getMetaData();

                ResultSet resultSetTables = dmd.getTables(null, null, null, new String[]{"TABLE"});

                HashMap<String, ArrayList> dbtables = new HashMap<>();
                HashMap<String, Object> dbvalues = new HashMap<>();
                while(resultSetTables.next())
                {
                    String tablename = resultSetTables.getString("TABLE_NAME");
                    if (datatableproperties.get(tablename) != null) {
                        manageTableRead(con, dmd, tablename, datatableproperties, dbtables, dbvalues);
                    }
                    if (datatablenewproperties.get(tablename) != null) {
                        boolean ok = manageTableInsert(con, dmd, tablename, datatablenewproperties, dbtables, dbvalues);
                        if (ok) {
                            dbvalues.put("INSERT", "true");
                        } else {
                            dbvalues.put("INSERT", "false");
                        }
                    }
                    if (datatabledeleteproperties.get(tablename) != null) {
                        boolean ok = manageTableDelete(con, dmd, tablename, datatabledeleteproperties, dbtables, dbvalues);
                        if (ok) {
                            dbvalues.put("DELETE", "true");
                        } else {
                            dbvalues.put("DELETE", "false");
                        }
                    }
                    if (datatableupdateproperties.get(tablename) != null) {
                        boolean ok = manageTableUpdate(con, dmd, tablename, datatableupdateproperties, dbtables, dbvalues);
                        if (ok) {
                            dbvalues.put("UPDATE", "true");
                        } else {
                            dbvalues.put("UPDATE", "false");
                        }
                    }
                }
                dbvalues.put("table", dbtables);
                dbexport.put(kndatasource.getDatabasename(), dbvalues);
            } catch (SQLException ex) {
                Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, ex);
            }
            sitecontentmap.put("db", dbexport);
        }
        return dbexport;
    }
    
    private Map getSitecontentmap(List<Knsitecontent> sitecontentlist) {
        Map sitecontentmap = new LinkedHashMap();
        for (Knsitecontent sitecontent : sitecontentlist) {
            Knclasscontent classcontent = em.find(Knclasscontent.class, sitecontent.getKnsitecontentPK().getClasscontentref());
            List<Knattributcontent> attributcontentlist = new ArrayList<>();
            Knclasscontent knclasscontent = em.find(Knclasscontent.class, classcontent.getId());
            attributcontentlist.addAll(em.createNamedQuery("Knattributcontent.findByClasscontentref").setParameter("classcontentref", knclasscontent).getResultList());
            sitecontentmap.put(classcontent.getName(), getattributmap(classcontent));
            
        }
        return sitecontentmap;
    }
    
    /*
        getParametermap
        Übergibt die POST Parameter in eine Hashmap um
    */
    private Map getParametermap(List<JsonFormParameter> postmap) {
        Map parametermap = new HashMap<>();
        if (postmap != null) {
            for (JsonFormParameter jfp : postmap) {
                parametermap.put(jfp.getName(), jfp.getValue());
            }
        }
        return parametermap;
    }
            
    /*
        getSaprfcfunctionparamMap
    */
    private HashMap<String, List> getSaprfcfunctionparamMap(List<Knsitesaprfc> sitesaprfclist) {
        HashMap<String, List> saprfcfunctionparamMap = new HashMap<>();
        for (Knsitesaprfc knsitesaprfc : sitesaprfclist) {
            List<RfcFunctionParam> rfcfunctionparamlist = new ArrayList<>();
            rfcfunctionparamlist.addAll(rfc_get_function_interface.getRfcFunctionsParamList(knsitesaprfc.getKnsitesaprfcPK().getRfcfunction()));
            saprfcfunctionparamMap.put(knsitesaprfc.getKnsitesaprfcPK().getRfcfunction(), rfcfunctionparamlist);
        }
        return saprfcfunctionparamMap;
    }
    
    /*
        getSapExport
        Übergibt die POST Parameter und ruft SAP RFC auf
        Setzt die Ergebnisse in eine Hashmap zur Ausgabe in Freemarker
    */
    private HashMap<String, HashMap> getSapExport(List<Knsitesaprfc> sitesaprfclist, HashMap<String, List> saprfcfunctionparamMap, List<JsonFormParameter> postmap) {
        HashMap<String, HashMap> sapexport = new HashMap<>();
        for (Knsitesaprfc knsitesaprfc : sitesaprfclist) {
            try {
                HashMap<String, Object> sapvalues = new HashMap<>();
                List<RfcFunctionParam> paramlist = saprfcfunctionparamMap.get(knsitesaprfc.getKnsitesaprfcPK().getRfcfunction());

                // Setze die Import Parameter des SAP RFC mit den Werten aus den POST Parametern
                JCoFunction function = sapc.getDestination().getRepository().getFunction(knsitesaprfc.getKnsitesaprfcPK().getRfcfunction());
                for (RfcFunctionParam rfcfunctionparam : paramlist) {
                    if (rfcfunctionparam.getParamclass().compareToIgnoreCase("I") == 0) {
                        if (postmap != null) {
                            for (JsonFormParameter jfp : postmap) {
                                if (jfp.getName().compareToIgnoreCase(rfcfunctionparam.getParameter()) == 0) {
                                    function.getImportParameterList().setValue(rfcfunctionparam.getParameter(), jfp.getValue());
                                }
                            }
                        }
                    }
                }
                // SAP RFC ausführen
                function.execute(sapc.getDestination());

                HashMap<String, ArrayList> saptables = new HashMap<>();
                for (RfcFunctionParam rfcfunctionparam : paramlist) {    
                    String tablename = rfcfunctionparam.getTabname();
                    String paramname = rfcfunctionparam.getParameter();
                    if (rfcfunctionparam.getParamclass().compareToIgnoreCase("E") == 0) {
                        sapvalues.put(rfcfunctionparam.getParameter(), function.getExportParameterList().getString(rfcfunctionparam.getParameter()));
                    }
                    if (rfcfunctionparam.getParamclass().compareToIgnoreCase("T") == 0) {
                        ArrayList<HashMap> tablevalues = new ArrayList<>();
                        functions_table = function.getTableParameterList().getTable(paramname);
                        List<RpyTableRead> rpytablereadlist = rpytableread.getRpyTableReadList(tablename);
                        for (int i = 0; i < functions_table.getNumRows(); i++) {
                            HashMap<String, String> sapexportvalues = new HashMap<>();
                            functions_table.setRow(i);
                            for (RpyTableRead rpytablereadentry : rpytablereadlist) {
                                if ((rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.CHAR) == 0) || 
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.NUMC) == 0) ||
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.UNIT) == 0)) {
                                    String value = functions_table.getString(rpytablereadentry.getFieldname());
                                    sapexportvalues.put(rpytablereadentry.getFieldname(), value);
                                    continue;
                                }
                                if ((rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.DATS) == 0) || 
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.TIMS) == 0)) {
                                    Date value = functions_table.getDate(rpytablereadentry.getFieldname());
                                    String datum = "";
                                    if (value != null) {
                                        if (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.DATS) == 0) {
                                            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                                            datum = sdf.format(value);
                                        } else {
                                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                                            datum = sdf.format(value);
                                        }
                                    }
                                    sapexportvalues.put(rpytablereadentry.getFieldname(), datum);
                                    continue;
                                }
                                if (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.QUAN) == 0) {
                                    double value = functions_table.getDouble(rpytablereadentry.getFieldname());
                                    sapexportvalues.put(rpytablereadentry.getFieldname(), String.valueOf(value));
                                    continue;
                                }
                                if ((rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.INT1) == 0) || 
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.INT2) == 0) || 
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.INT4) == 0) || 
                                    (rpytablereadentry.getDatatype().compareToIgnoreCase(SAPDATATYPE.INT8) == 0)) {
                                    int value = functions_table.getInt(rpytablereadentry.getFieldname());
                                    sapexportvalues.put(rpytablereadentry.getFieldname(), String.valueOf(value));
                                }
                            }
                            tablevalues.add(sapexportvalues);
                        }
                        saptables.put(paramname, tablevalues);
                    }
                }
                sapvalues.put("table", saptables);
                sapexport.put(knsitesaprfc.getKnsitesaprfcPK().getRfcfunction(), sapvalues);
            } catch(Exception ex) {
                Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return sapexport;
    }
    
    /*
        getDatatableproperties
        Setzt die Properties für ein DB READ Aufruf
    */
    private HashMap<String, DatatableProperties> getDatatableproperties(List<JsonFormParameter> postmap) {
        HashMap<String, DatatableProperties> datatableproperties = new HashMap<>();
        if (postmap != null) {
            for (JsonFormParameter jfp : postmap) {
                // Datenbank READ Parameter
                if (jfp.getName().compareToIgnoreCase("db$table") == 0) {
                    if (datatableproperties.get(jfp.getValue()) == null ) {
                        DatatableProperties dtp = new DatatableProperties();
                        dtp.setTablename(jfp.getValue());
                        datatableproperties.put(jfp.getValue(), dtp);
                    }
                }
                if (jfp.getName().startsWith("db$table$")) {
                    String rest = jfp.getName().substring(9);
                    String[] values = rest.split("\\$");
                    if (values[1].compareToIgnoreCase("orderby") == 0) {
                        if (datatableproperties.isEmpty()) {
                            DatatableProperties dtp = new DatatableProperties();
                            dtp.setTablename(values[0]);
                            datatableproperties.put(values[0], dtp);
                        }
                        datatableproperties.get(values[0]).setOrderby(jfp.getValue());
                    }
                    if (values[1].compareToIgnoreCase("orderdir") == 0) {
                        if (datatableproperties.isEmpty()) {
                            DatatableProperties dtp = new DatatableProperties();
                            dtp.setTablename(values[0]);
                            datatableproperties.put(values[0], dtp);
                        }
                        datatableproperties.get(values[0]).setOrderdir(jfp.getValue());
                    }
                    if (values[1].compareToIgnoreCase("pagination") == 0) {
                        if (datatableproperties.isEmpty()) {
                            DatatableProperties dtp = new DatatableProperties();
                            dtp.setTablename(values[0]);
                            datatableproperties.put(values[0], dtp);
                        }
                        datatableproperties.get(values[0]).setPagination(Integer.parseInt(jfp.getValue()));
                    }
                    if (values[1].compareToIgnoreCase("page") == 0) {
                        if (datatableproperties.isEmpty()) {
                            DatatableProperties dtp = new DatatableProperties();
                            dtp.setTablename(values[0]);
                            datatableproperties.put(values[0], dtp);
                        }
                        datatableproperties.get(values[0]).setPage(Integer.parseInt(jfp.getValue()));
                    }
                    if (values[1].compareToIgnoreCase("groupbycount") == 0) {
                        if (datatableproperties.isEmpty()) {
                            DatatableProperties dtp = new DatatableProperties();
                            dtp.setTablename(values[0]);
                            datatableproperties.put(values[0], dtp);
                        }
                        datatableproperties.get(values[0]).setGroupbycount(jfp.getValue());
                    }
                    if (values[1].compareToIgnoreCase("groupby") == 0) {
                        if (datatableproperties.isEmpty()) {
                            DatatableProperties dtp = new DatatableProperties();
                            dtp.setTablename(values[0]);
                            datatableproperties.put(values[0], dtp);
                        }
                        datatableproperties.get(values[0]).getGroupbylist().add(jfp.getValue());
                    }
                    if (values[1].compareToIgnoreCase("condition") == 0) {
                        DatatableCondition dtc = new DatatableCondition();
                        dtc.setField(values[2]);
                        dtc.setOperand(values[3]);
                        dtc.setValue(jfp.getValue());
                        if (datatableproperties.isEmpty()) {
                            DatatableProperties dtp = new DatatableProperties();
                            dtp.setTablename(values[0]);
                            datatableproperties.put(values[0], dtp);
                        }
                        datatableproperties.get(values[0]).getConditionlist().add(dtc);
                    }
                }
            }
        }
        return datatableproperties;
    }
    
    /*
        getDatatablenewproperties
        Setzt die Properties für ein DB INSERT Aufruf
    */
    private HashMap<String, DatatableNewProperties> getDatatablenewproperties(List<JsonFormParameter> postmap) {  
        HashMap<String, DatatableNewProperties> datatablenewproperties = new HashMap<>();
        if (postmap != null) {
            for (JsonFormParameter jfp : postmap) {
                // Datenbank INSERT Parameter
                if (jfp.getName().compareToIgnoreCase("db$tablenew") == 0) {
                    DatatableNewProperties dtnp = new DatatableNewProperties();
                    dtnp.setTablename(jfp.getValue());
                    datatablenewproperties.put(jfp.getValue(), dtnp);
                }
                if (jfp.getName().startsWith("db$tablenew$")) {
                    String rest = jfp.getName().substring(12);
                    String[] values = rest.split("\\$");
                    DatatableNewValue dtnv = new DatatableNewValue();
                    dtnv.setField(values[1]);
                    dtnv.setValue(jfp.getValue());
                    datatablenewproperties.get(values[0]).getValuelist().add(dtnv);
                }
            }
        }
        return datatablenewproperties;
    }
    
    /*
        getDatatabledeleteproperties
        Setzt die Properties für ein DB DELETE Aufruf
    */
    private HashMap<String, DatatableDeleteProperties> getDatatabledeleteproperties(List<JsonFormParameter> postmap) {  
        HashMap<String, DatatableDeleteProperties> datatabledeleteproperties = new HashMap<>();
        if (postmap != null) {
            for (JsonFormParameter jfp : postmap) {
                // Datenbank DELETE Parameter
                if (jfp.getName().compareToIgnoreCase("db$tabledelete") == 0) {
                    DatatableDeleteProperties dtdp = new DatatableDeleteProperties();
                    dtdp.setTablename(jfp.getValue());
                    datatabledeleteproperties.put(jfp.getValue(), dtdp);
                }
                if (jfp.getName().startsWith("db$tabledelete$")) {
                    String rest = jfp.getName().substring(15);
                    String[] values = rest.split("\\$");
                    DatatableDeleteValue dtdv = new DatatableDeleteValue();
                    dtdv.setField(values[1]);
                    dtdv.setValue(jfp.getValue());
                    datatabledeleteproperties.get(values[0]).getValuelist().add(dtdv);
                }
            }
        }
        return datatabledeleteproperties;
    }
    
    /*
        getDatatableupdateproperties
        Setzt die Properties für ein DB UPDATE Aufruf
    */
    private HashMap<String, DatatableUpdateProperties> getDatatableupdateproperties(List<JsonFormParameter> postmap) {  
        HashMap<String, DatatableUpdateProperties> datatableupdateproperties = new HashMap<>();
        if (postmap != null) {
            for (JsonFormParameter jfp : postmap) {
                // Datenbank UPDATE Parameter
                if (jfp.getName().compareToIgnoreCase("db$tableupdate") == 0) {
                    DatatableUpdateProperties dtup = new DatatableUpdateProperties();
                    dtup.setTablename(jfp.getValue());
                    datatableupdateproperties.put(jfp.getValue(), dtup);
                }
                if (jfp.getName().startsWith("db$tableupdate$")) {
                    String rest = jfp.getName().substring(15);
                    String[] values = rest.split("\\$");
                    if (values[1].compareToIgnoreCase("condition") == 0) {
                        DatatableCondition dtc = new DatatableCondition();
                        dtc.setField(values[2]);
                        dtc.setOperand(values[3]);
                        dtc.setValue(jfp.getValue());
                        datatableupdateproperties.get(values[0]).getConditionlist().add(dtc);
                    } else {
                        DatatableNewValue dtnv = new DatatableNewValue();
                        dtnv.setField(values[1]);
                        dtnv.setValue(jfp.getValue());
                        datatableupdateproperties.get(values[0]).getValuelist().add(dtnv);
                    }
                }
            }
        }
        return datatableupdateproperties;
    }
                
    /*
        getEmailproperties
        Setzt die Properties für ein EMAIL send Aufruf
    */
    private EmailProperties getEmailproperties(List<JsonFormParameter> postmap) {  
        EmailProperties emailproperties = null;
        if (postmap != null) {
            for (JsonFormParameter jfp : postmap) {
                // EMAIL Parameter
                if (jfp.getName().compareToIgnoreCase("email$to") == 0) {
                    if (emailproperties == null) {
                        emailproperties = new EmailProperties();
                    }
                    emailproperties.setSendto(jfp.getValue());
                }
                if (jfp.getName().compareToIgnoreCase("email$subject") == 0) {
                    if (emailproperties == null) {
                        emailproperties = new EmailProperties();
                    }
                    emailproperties.setSubject(jfp.getValue());
                }
                if (jfp.getName().compareToIgnoreCase("email$body") == 0) {
                    if (emailproperties == null) {
                        emailproperties = new EmailProperties();
                    }
                    emailproperties.setBody(jfp.getValue());
                }
            }
        }
        return emailproperties;
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
