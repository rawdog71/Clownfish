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
package io.clownfish.clownfish.templatebeans;

import io.clownfish.clownfish.dbentities.CfDatasource;
import io.clownfish.clownfish.dbentities.CfSitedatasource;
import io.clownfish.clownfish.jdbc.JDBCUtil;
import io.clownfish.clownfish.jdbc.TableField;
import io.clownfish.clownfish.jdbc.TableFieldStructure;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;

import org.springframework.util.ReflectionUtils;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;

import java.awt.event.InvocationEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author philip, sulzbachr
 */
@Scope("prototype")
@Component
public class ImportTemplateBean implements Serializable
{
    private CfDatasourceService cfdatasourceService;
    private transient @Getter @Setter Map contentmap;
    private List<CfSitedatasource> sitedatasourcelist;
    private File fileIn;

    final transient Logger LOGGER = LoggerFactory.getLogger(ImportTemplateBean.class);

    public ImportTemplateBean()
    {
        contentmap = new HashMap<>();
    }

    public void init(List<CfSitedatasource> sitedatasourcelist, CfDatasourceService cfdatasourceService)
    {
        this.sitedatasourcelist = sitedatasourcelist;
        this.cfdatasourceService = cfdatasourceService;
        contentmap.clear();
    }

    public void initjob(List<CfSitedatasource> sitedatasourcelist, CfDatasourceService cfdatasourceService)
    {
        this.sitedatasourcelist = sitedatasourcelist;
        this.cfdatasourceService = cfdatasourceService;
        contentmap.clear();
    }

    static private class Model
    {
        static class Personenstamm extends Model
        {
            private String Mandant;
            private String Werk;
            private String Persnr;
            private String Kostenstelle;
            private String Abteilung;
            private String Name;
            private String Vorname;
            private String Geburtstag;
            private String Eintritt;
            private String Austritt;
            private String Ausweis;
            private String Stammarb;
            private String Salden;
            private String Vorgesetzter;
            private String Mitarbeitergruppe;
            private String Kennzeichen;
            private String EmailVorg;
            private String Vorstand;

            public Personenstamm() {}

            public Personenstamm(String mandant, String werk, String persnr, String feld5, String feld6, String name, String vorname, String geburtstag, String eintritt, String austritt, String ausweis, String stammarb, String salden, String vorgesetzter, String mitarbeitergruppe, String kennzeichen, String emailVorg, String vorstand)
            {
                this.Mandant = mandant;
                this.Werk = werk;
                this.Persnr = persnr;
                this.Kostenstelle = feld5;
                this.Abteilung = feld6;
                this.Name = name;
                this.Vorname = vorname;
                this.Geburtstag = geburtstag;
                this.Eintritt = eintritt;
                this.Austritt = austritt;
                this.Ausweis = ausweis;
                this.Stammarb = stammarb;
                this.Salden = salden;
                this.Vorgesetzter = vorgesetzter;
                this.Mitarbeitergruppe = mitarbeitergruppe;
                this.Kennzeichen = kennzeichen;
                this.EmailVorg = emailVorg;
                this.Vorstand = vorstand;
            }

            public String getMandant()
            {
                return Mandant;
            }

            public void setMandant(String mandant)
            {
                Mandant = mandant;
            }

            public String getWerk()
            {
                return Werk;
            }

            public void setWerk(String werk)
            {
                Werk = werk;
            }

            public String getPersnr()
            {
                return Persnr;
            }

            public void setPersnr(String persnr)
            {
                Persnr = persnr;
            }

            public String getKostenstelle()
            {
                return Kostenstelle;
            }

            public void setKostenstelle(String kostenstelle)
            {
                Kostenstelle = kostenstelle;
            }

            public String getAbteilung()
            {
                return Abteilung;
            }

            public void setAbteilung(String abteilung)
            {
                Abteilung = abteilung;
            }

            public String getName()
            {
                return Name;
            }

            public void setName(String name)
            {
                Name = name;
            }

            public String getVorname()
            {
                return Vorname;
            }

            public void setVorname(String vorname)
            {
                Vorname = vorname;
            }

            public String getGeburtstag()
            {
                return Geburtstag;
            }

            public void setGeburtstag(String geburtstag)
            {
                Geburtstag = geburtstag;
            }

            public String getEintritt()
            {
                return Eintritt;
            }

            public void setEintritt(String eintritt)
            {
                Eintritt = eintritt;
            }

            public String getAustritt()
            {
                return Austritt;
            }

            public void setAustritt(String austritt)
            {
                Austritt = austritt;
            }

            public String getAusweis() {
                return Ausweis;
            }

            public void setAusweis(String ausweis)
            {
                Ausweis = ausweis;
            }

            public String getStammarb() {
                return Stammarb;
            }

            public void setStammarb(String stammarb) {
                Stammarb = stammarb;
            }

            public String getSalden() {
                return Salden;
            }

            public void setSalden(String salden) {
                Salden = salden;
            }

            public String getVorgesetzter() {
                return Vorgesetzter;
            }

            public void setVorgesetzter(String vorgesetzter) {
                Vorgesetzter = vorgesetzter;
            }

            public String getMitarbeitergruppe() {
                return Mitarbeitergruppe;
            }

            public void setMitarbeitergruppe(String mitarbeitergruppe) {
                Mitarbeitergruppe = mitarbeitergruppe;
            }

            public String getKennzeichen() {
                return Kennzeichen;
            }

            public void setKennzeichen(String kennzeichen) {
                Kennzeichen = kennzeichen;
            }

            public String getEmailVorg() {
                return EmailVorg;
            }

            public void setEmailVorg(String emailVorg) {
                EmailVorg = emailVorg;
            }

            public String getVorstand() {
                return Vorstand;
            }

            public void setVorstand(String vorstand) {
                Vorstand = vorstand;
            }
        }

        static class Schicht extends Model
        {
            private int persnr;
            private String datum;
            private String zeit;
            private String werk;

            public Schicht() {}

            public Schicht(int persnr, String datum, String zeit, String werk)
            {
                this.persnr = persnr;
                this.datum = datum;
                this.zeit = zeit;
                this.werk = werk;
            }

            public int getPersnr()
            {
                return this.persnr;
            }

            public int setPersnr(int persnr)
            {
                return this.persnr = persnr;
            }

            public String getDatum()
            {
                return this.datum;
            }

            public String setDatum(String datum)
            {
                return this.datum = datum;
            }

            public String getZeit()
            {
                return this.zeit;
            }

            public String setZeit(String zeit)
            {
                return this.zeit = zeit;
            }

            public String getWerk()
            {
                return this.werk;
            }

            public String setWerk(String werk)
            {
                return this.werk = werk;
            }
        }

        static class Langzeit extends Model
        {
            private int Kennummer;
            private String Persnr;
            private String Zeitbeginn;
            private String Zeitende;
            private String Eingabekn;
            private String Datenart;
            private String Zeitlohnart;
            private String Firma;
            private String ZUSCH;

            public Langzeit() {}

            public Langzeit(int kennummer, String persnr, String zeitbeginn, String zeitende, String eingabekn, String datenart, String zeitlohnart, String firma, String ZUSCH)
            {
                this.Kennummer = kennummer;
                this.Persnr = persnr;
                this.Zeitbeginn = zeitbeginn;
                this.Zeitende = zeitende;
                this.Eingabekn = eingabekn;
                this.Datenart = datenart;
                this.Zeitlohnart = zeitlohnart;
                this.Firma = firma;
                this.ZUSCH = ZUSCH;
            }

            public int getKennummer()
            {
                return this.Kennummer;
            }

            public void setKennummer(int kennummer)
            {
                this.Kennummer = kennummer;
            }

            public String getPersnr()
            {
                return this.Persnr;
            }

            public void setPersnr(String persnr)
            {
                this.Persnr = persnr;
            }

            public String getZeitbeginn()
            {
                return this.Zeitbeginn;
            }

            public void setZeitbeginn(String zeitbeginn)
            {
                this.Zeitbeginn = zeitbeginn;
            }

            public String getZeitende()
            {
                return this.Zeitende;
            }

            public void setZeitende(String zeitende)
            {
                this.Zeitende = zeitende;
            }

            public String getEingabekn()
            {
                return this.Eingabekn;
            }

            public void setEingabekn(String eingabekn)
            {
                this.Eingabekn = eingabekn;
            }

            public String getDatenart()
            {
                return this.Datenart;
            }

            public void setDatenart(String datenart)
            {
                this.Datenart = datenart;
            }

            public String getZeitlohnart()
            {
                return this.Zeitlohnart;
            }

            public void setZeitlohnart(String zeitlohnart)
            {
                this.Zeitlohnart = zeitlohnart;
            }

            public String getFirma()
            {
                return this.Firma;
            }

            public void setFirma(String firma)
            {
                this.Firma = firma;
            }

            public String getZUSCH()
            {
                return this.ZUSCH;
            }

            public void setZUSCH(String ZUSCH)
            {
                this.ZUSCH = ZUSCH;
            }
        }
    }

    private static CellProcessor[] getProcessors()
    {
        final CellProcessor[] processors = new CellProcessor[]
        {
            new ParseInt(), // persnr
            new ParseDate("ddmmyyyy"), // datum
            new NotNull(), // zeit
            new NotNull() // werk
        };

        return processors;
    }

    private String generateSqlStatement(Class<?> csvModel, String schemaName, String tblName)
    {
        Method[] methods = csvModel.getDeclaredMethods();

        StringBuilder strBuilder = new StringBuilder();
        String sql = "INSERT INTO " + tblName + " (";

        for (Method m : methods)
        {
            m.setAccessible(true);
            try
            {
                //result = m.invoke(csvModel).toString();
                sql += m.invoke(csvModel).toString() + ",";

                // ReflectionUtils.doWithFields(csvModel, FieldCallback);
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                LOGGER.error(e.getMessage());
            }
        }
        return sql;
    }

    public void fileRead(File fileIn, String schemaName, String tblName, boolean bHeader, boolean status)
    {
        fileIn = new File("import.csv");
        String sql = "INSERT INTO " + tblName + " (" +  ""+ ") VALUES (?, ?, ?, ?, ?)";

        //LOGGER.info("START dbexecute: " + sql);
        for (CfSitedatasource sitedatasource : sitedatasourcelist)
        {
            try
            {
                CfDatasource cfdatasource = cfdatasourceService.findById(sitedatasource.getCfSitedatasourcePK().getDatasourceref());
                JDBCUtil jdbcutil = new JDBCUtil(cfdatasource.getDriverclass(), cfdatasource.getUrl(), cfdatasource.getUser(), cfdatasource.getPassword());
                Connection connection = jdbcutil.getConnection();
                if (connection != null)
                {

                    ICsvBeanReader beanReader = new CsvBeanReader(new FileReader(fileIn), CsvPreference.STANDARD_PREFERENCE);
                    String[] header = beanReader.getHeader(bHeader);
                    Models.Schicht schichtBean;

                    while ((schichtBean = beanReader.read(Models.Schicht.class, header, getProcessors())) != null)
                    {

                    }

                    if (connection.getCatalog().compareToIgnoreCase(schemaName) == 0)
                    {
                        try (Statement stmt = connection.createStatement())
                        {
                            int count = stmt.executeUpdate(sql);
                            if (count > 0)
                            {
                                status = true;
                                //LOGGER.info("START dbexecute TRUE");
                            }
                            else
                            {
                                //LOGGER.info("START dbexecute FALSE");
                            }
                        }
                    }
                    connection.close();
                }
                else
                {
                    //LOGGER.warn("Connection to database not established");
                }
            }
            catch (SQLException | IOException ex)
            {
                LOGGER.error(ex.getMessage());
            }
        }

        // LOGGER.info("END dbexecute");

//        sitedatasourcelist.forEach((sitedatasource) ->
//        {
//            try
//            {
//                long start = System.currentTimeMillis();
//
//                // TODO WIP
//            }
//            catch (Exception e)
//            {
//                LOGGER.error(e.getMessage());
//            }
//        });
    }

//    public Map<String, ArrayList> fileIn(File fileIn, String catalog, String tablename, String sqlstatement)
//    {
//        HashMap<String, ArrayList> dbtables = new HashMap<>();
//
//        sitedatasourcelist.forEach((sitedatasource) ->
//        {
//            try
//            {
//                CfDatasource cfdatasource = cfdatasourceService.findById(sitedatasource.getCfSitedatasourcePK().getDatasourceref());
//                JDBCUtil jdbcutil = new JDBCUtil(cfdatasource.getDriverclass(), cfdatasource.getUrl(), cfdatasource.getUser(), cfdatasource.getPassword());
//                Connection connection = jdbcutil.getConnection();
//                if (connection != null)
//                {
//                    String catalogname;
//                    if (cfdatasource.getDriverclass().contains("oracle"))
//                    {     // Oracle driver
//                        catalogname = connection.getSchema();
//                    }
//                    else
//                    {                                                    // other drivers
//                        catalogname = connection.getCatalog();
//                    }
//
//                    if (catalogname.compareToIgnoreCase(catalog) == 0)
//                    {
//                        Statement stmt = connection.createStatement();
//                        ResultSet result = stmt.executeQuery(sqlstatement);
//                        ResultSetMetaData rmd = result.getMetaData();
//                        TableFieldStructure tfs = getTableFieldsList(rmd);
//                        ArrayList<HashMap> tablevalues = new ArrayList<>();
//                        while (result.next())
//                        {
//                            HashMap<String, String> dbexportvalues = new HashMap<>();
//                            tfs.getTableFieldsList().stream().forEach((tf) -> {
//                                try
//                                {
//                                    String value = result.getString(tf.getName());
//                                    dbexportvalues.put(tf.getName(), value);
//                                }
//                                catch (java.sql.SQLException ex)
//                                {
//                                    LOGGER.warn(ex.getMessage());
//                                }
//                            });
//                            tablevalues.add(dbexportvalues);
//                        }
//                        dbtables.put(tablename, tablevalues);
//                    }
//                    contentmap.put("db", dbtables);
//                    connection.close();
//                }
//                else
//                {
//                    LOGGER.warn("Connection to database not established");
//                }
//            }
//            catch (SQLException ex)
//            {
//                LOGGER.error(ex.getMessage());
//            } catch (Exception ex)
//            {
//                LOGGER.error(ex.getMessage());
//            }
//        });
//        return contentmap;
//    }

//    public boolean dbexecute(String catalog, String sqlstatement)
//    {
//        boolean ok = false;
//        LOGGER.info("START dbexecute: " + sqlstatement);
//        for (CfSitedatasource sitedatasource : sitedatasourcelist)
//        {
//            try
//            {
//                CfDatasource cfdatasource = cfdatasourceService.findById(sitedatasource.getCfSitedatasourcePK().getDatasourceref());
//                JDBCUtil jdbcutil = new JDBCUtil(cfdatasource.getDriverclass(), cfdatasource.getUrl(), cfdatasource.getUser(), cfdatasource.getPassword());
//                Connection con = jdbcutil.getConnection();
//                if (null != con)
//                {
//                    if (con.getCatalog().compareToIgnoreCase(catalog) == 0)
//                    {
//                        try (Statement stmt = con.createStatement())
//                        {
//                            int count = stmt.executeUpdate(sqlstatement);
//                            if (count > 0)
//                            {
//                                ok = true;
//                                LOGGER.info("START dbexecute TRUE");
//                            }
//                            else
//                            {
//                                LOGGER.info("START dbexecute FALSE");
//                            }
//                        }
//                    }
//                    con.close();
//                }
//                else
//                {
//                    LOGGER.warn("Connection to database not established");
//                }
//            }
//            catch (SQLException ex)
//            {
//                LOGGER.error(ex.getMessage());
//            }
//        };
//        LOGGER.info("END dbexecute");
//        return ok;
//    }

//    private TableFieldStructure getTableFieldsList(ResultSetMetaData dmd)
//    {
//        try
//        {
//            TableFieldStructure tfs = new TableFieldStructure();
//            ArrayList<TableField> tableFieldsList = new ArrayList<>();
//            int columncount = dmd.getColumnCount();
//            for (int i = 1; i <= columncount; i++)
//            {
//                String columnName = dmd.getColumnName(i);
//                int colomuntype = dmd.getColumnType(i);
//                String colomuntypename = dmd.getColumnTypeName(i);
//                int columnsize = dmd.getColumnDisplaySize(i);
//                int decimaldigits = dmd.getPrecision(i);
//                /*
//                if (decimaldigits == null)
//                {
//                    decimaldigits = "0";
//                }
//                 */
//                int isNullable = dmd.isNullable(i);
//                //String is_autoIncrment = columns.getString("IS_AUTOINCREMENT");
//                String is_autoIncrment = "";
//
//                switch (colomuntype)
//                {
//                    case 1:      // varchar -> String
//                        tableFieldsList.add(new TableField(columnName, "STRING", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
//                        break;
//                    case 2:       // int
//                        tableFieldsList.add(new TableField(columnName, "INT", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
//                        break;
//                    case 4:       // int
//                        tableFieldsList.add(new TableField(columnName, "INT", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
//                        break;
//                    case 5:       // smallint
//                        tableFieldsList.add(new TableField(columnName, "INT", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
//                        break;
//                    case 7:       // real
//                        tableFieldsList.add(new TableField(columnName, "REAL", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
//                        break;
//                    case 8:       // float
//                        tableFieldsList.add(new TableField(columnName, "FLOAT", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
//                        break;
//                    case 12:      // varchar -> String
//                        tableFieldsList.add(new TableField(columnName, "STRING", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
//                        break;
//                    case -5:      // long
//                        tableFieldsList.add(new TableField(columnName, "LONG", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
//                        break;
//                    case -7:      // bit
//                        tableFieldsList.add(new TableField(columnName, "BOOLEAN", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
//                        break;
//                    case 2005:    // text -> String
//                        tableFieldsList.add(new TableField(columnName, "STRING", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
//                        break;
//                    case 93:      // Date
//                        tableFieldsList.add(new TableField(columnName, "DATE", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
//                        break;
//                }
//            }
//            tfs.setDefault_order("");
//            tfs.setTableFieldsList(tableFieldsList);
//            return tfs;
//        }
//        catch (SQLException ex)
//        {
//            LOGGER.error(ex.getMessage());
//            return null;
//        }
//    }
}