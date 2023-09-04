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
import io.clownfish.clownfish.utils.PropertyUtil;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sulzbachr
 */
@Scope("request")
@Component
public class DatabaseTemplateBean implements Serializable {
    private CfDatasourceService cfdatasourceService;
    private PropertyUtil propertyUtil;
    private transient @Getter @Setter Map contentmap;
    private List<CfSitedatasource> sitedatasourcelist;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(DatabaseTemplateBean.class);
    
    public DatabaseTemplateBean(PropertyUtil propertyUtil) {
        this.propertyUtil = propertyUtil;
        contentmap = new HashMap<>();
    }
    
    public void init(List<CfSitedatasource> sitedatasourcelist, CfDatasourceService cfdatasourceService) {
        this.sitedatasourcelist = sitedatasourcelist;
        this.cfdatasourceService = cfdatasourceService;
        contentmap.clear();
    }
    
    public void initjob(List<CfSitedatasource> sitedatasourcelist, CfDatasourceService cfdatasourceService) {
        this.sitedatasourcelist = sitedatasourcelist;
        this.cfdatasourceService = cfdatasourceService;
        contentmap.clear();
    }
    
    public Map dbread(String catalog, String tablename, String sqlstatement) {
        if (sitedatasourcelist == null) {
            LOGGER.error("ERROR: Template has no datasources!");
            return null;
        }
        if (propertyUtil.getPropertyBoolean("sql_debug", true)) {
            LOGGER.info("START dbread\nQuery: " + sqlstatement);
        }
        HashMap<String, ArrayList> dbtables = new HashMap<>();
        sitedatasourcelist.stream().forEach((sitedatasource) -> {
            try {
                CfDatasource cfdatasource = cfdatasourceService.findById(sitedatasource.getCfSitedatasourcePK().getDatasourceref());
                JDBCUtil jdbcutil = new JDBCUtil(cfdatasource.getDriverclass(), cfdatasource.getUrl(), cfdatasource.getUser(), cfdatasource.getPassword());
                Connection con = jdbcutil.getConnection();
                if (null != con) {
                    try {
                        String catalogname;
                        if (cfdatasource.getDriverclass().contains("oracle")) {     // Oracle driver 
                            catalogname = con.getSchema();
                        } else {                                                    // other drivers
                            catalogname = con.getCatalog();
                        }
                        if (catalogname.compareToIgnoreCase(catalog) == 0) {
                            Statement stmt = con.createStatement();
                            ResultSet result = stmt.executeQuery(sqlstatement);
                            ResultSetMetaData rmd = result.getMetaData();
                            TableFieldStructure tfs = getTableFieldsList(rmd);
                            ArrayList<HashMap> tablevalues = new ArrayList<>();
                            while (result.next()) {
                                HashMap<String, String> dbexportvalues = new HashMap<>();
                                tfs.getTableFieldsList().stream().forEach((tf) -> {
                                    try {
                                        String value = result.getString(tf.getName());
                                        dbexportvalues.put(tf.getName(), value);
                                    } catch (java.sql.SQLException ex) {
                                        LOGGER.warn(ex.getMessage());
                                    }
                                });
                                tablevalues.add(dbexportvalues);
                            }
                            dbtables.put(tablename, tablevalues);
                        }
                        contentmap.put("db", dbtables);
                        con.close();
                    } catch (SQLException ex1) {
                        LOGGER.error("CATALOG: "+ catalog + " TABLE: " + tablename + " STATEMENT: " + sqlstatement);
                        LOGGER.error(ex1.getMessage());
                        con.close();
                    }
                    finally {
                        try {
                            con.close();
                        }
                        catch (SQLException e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                } else {
                    LOGGER.warn("Connection to database not established");
                }
            } catch (SQLException ex) {
                LOGGER.error(ex.getMessage());
                LOGGER.error(catalog + " - " + tablename + " - " + sqlstatement);
            }
        });
        //LOGGER.info("END dbread");
        return contentmap;
    }

    public Map<String, HashMap<String, ArrayList<HashMap<String, String>>>> dbread(CfDatasource cfdatasource, String catalog, String tablename, String sqlstatement) throws SQLException {
        if (sitedatasourcelist == null) {
            LOGGER.error("ERROR: Template has no datasources!");
            return null;
        }
        if (propertyUtil.getPropertyBoolean("sql_debug", true)) {
            LOGGER.info("START dbread\nQuery: " + sqlstatement);
        }
        HashMap<String, ArrayList<HashMap<String, String>>> dbtables = new HashMap<>();
        JDBCUtil jdbcutil = new JDBCUtil(cfdatasource.getDriverclass(), cfdatasource.getUrl(), cfdatasource.getUser(), cfdatasource.getPassword());
        Connection con = jdbcutil.getConnection();
        if (null != con) {
            try {
                String catalogname;
                if (cfdatasource.getDriverclass().contains("oracle")) {     // Oracle driver
                    catalogname = con.getSchema();
                } else {                                                    // other drivers
                    catalogname = con.getCatalog();
                }
                if (catalogname.compareToIgnoreCase(catalog) == 0) {
                    Statement stmt = con.createStatement();
                    ResultSet result = stmt.executeQuery(sqlstatement);
                    ResultSetMetaData rmd = result.getMetaData();
                    TableFieldStructure tfs = getTableFieldsList(rmd);
                    ArrayList<HashMap<String, String>> tablevalues = new ArrayList<>();
                    while (result.next()) {
                        HashMap<String, String> dbexportvalues = new HashMap<>();
                        tfs.getTableFieldsList().stream().forEach((tf) -> {
                            try {
                                String value = result.getString(tf.getName());
                                dbexportvalues.put(tf.getName(), value);
                            } catch (java.sql.SQLException ex) {
                                LOGGER.warn(ex.getMessage());
                            }
                        });
                        tablevalues.add(dbexportvalues);
                    }
                    dbtables.put(tablename, tablevalues);
                }
                contentmap.put("db", dbtables);
                con.close();
            } catch (SQLException ex1) {
                LOGGER.error("CATALOG: "+ catalog + " TABLE: " + tablename + " STATEMENT: " + sqlstatement);
                LOGGER.error(ex1.getMessage());
                con.close();
            }
            finally {
                try {
                    con.close();
                }
                catch (SQLException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        } else {
            LOGGER.warn("Connection to database not established");
        }
        //LOGGER.info("END dbread");
        return contentmap;
    }
    
    public boolean dbexecute(String catalog, String sqlstatement) {
        boolean ok = false;
        if (sitedatasourcelist == null) {
            LOGGER.error("ERROR: Template has no datasources!");
            return ok;
        }
        if (propertyUtil.getPropertyBoolean("sql_debug", true)) {
            LOGGER.info("START dbexecute\nQuery: " + sqlstatement);
        }
        for (CfSitedatasource sitedatasource : sitedatasourcelist) {
            CfDatasource cfdatasource = cfdatasourceService.findById(sitedatasource.getCfSitedatasourcePK().getDatasourceref());
            JDBCUtil jdbcutil = new JDBCUtil(cfdatasource.getDriverclass(), cfdatasource.getUrl(), cfdatasource.getUser(), cfdatasource.getPassword());
            Connection con = jdbcutil.getConnection();
            if (null != con) {
                String catalogName;

                try {
                    if (cfdatasource.getDriverclass().contains("oracle"))
                    {     // Oracle driver
                        catalogName = con.getSchema();
                    }
                    else
                    {                                                    // other drivers
                        catalogName = con.getCatalog();
                    }

                    if (catalogName.compareToIgnoreCase(catalog) == 0)
                    {
                        try (Statement stmt = con.createStatement()) {
                            int count = stmt.executeUpdate(sqlstatement);
                            if (count > 0 ) {
                                ok = true;
                                LOGGER.info("START dbexecute TRUE");
                            } else {
                                LOGGER.info("START dbexecute FALSE");
                            }
                        }
                    }
                    con.close();
                } catch (SQLIntegrityConstraintViolationException e) {
                    LOGGER.error(e.getMessage());
                    ok = true;
                }
                catch (SQLException ex) {
                    LOGGER.error("CATALOG: "+ catalog + " STATEMENT: " + sqlstatement);
                    LOGGER.error(ex.getMessage());
                } finally {
                    try {
                        con.close();
                    } catch (SQLException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                }
            } else {
                LOGGER.warn("Connection to database not established");
            }
        }
        //LOGGER.info("END dbexecute");
        return ok;
    }

    public Map dbexecute(String[] params)
    {
        if (sitedatasourcelist == null) {
            LOGGER.error("ERROR: Template has no datasources!");
            return null;
        }
        Map map = new HashMap<>();
        String catalog = params[0];
        String sqlStatement = params[1];
        if (propertyUtil.getPropertyBoolean("sql_debug", true)) {
            LOGGER.info("START dbexecute\nQuery: " + sqlStatement);
        }
        for (CfSitedatasource sitedatasource : sitedatasourcelist)
        {
            CfDatasource cfdatasource = cfdatasourceService.findById(sitedatasource.getCfSitedatasourcePK().getDatasourceref());
            JDBCUtil jdbcutil = new JDBCUtil(cfdatasource.getDriverclass(), cfdatasource.getUrl(), cfdatasource.getUser(), cfdatasource.getPassword());
            Connection con = jdbcutil.getConnection();
            if (null != con)
            {
                String catalogName;
                try {

                    if (cfdatasource.getDriverclass().contains("oracle")) // Oracle driver
                        catalogName = con.getSchema();
                    else // other drivers
                        catalogName = con.getCatalog();

                    if (catalogName.compareToIgnoreCase(catalog) == 0)
                    {
                        try (Statement stmt = con.createStatement())
                        {
                            if (stmt.execute(sqlStatement)) // Statement has one or potentially multiple result sets
                            {
                                LOGGER.info("START dbexecute TRUE (Statement type: Result set)");
                                ResultSet resultSet = stmt.getResultSet();
                                ResultSetMetaData rmd = resultSet.getMetaData();
                                TableFieldStructure tfs = getTableFieldsList(rmd);
                                ArrayList<HashMap> tablevalues = new ArrayList<>();
                                int count = 0;

                                while (resultSet.next()) {
                                    count++;
                                    tfs.getTableFieldsList().forEach((tf) ->
                                    {
                                        try
                                        {
                                            String value = resultSet.getString(tf.getName());
                                            map.put(tf.getName(), value);
                                        }
                                        catch (java.sql.SQLException ex)
                                        {
                                            LOGGER.error(ex.getMessage());
                                        }
                                    });
                                }
                                LOGGER.info("Results: " + count);
                            }
                            else // Statement only has an update count or does not return anything
                            {
                                if (stmt.getUpdateCount() > 0)
                                {
                                    LOGGER.info("START dbexecute TRUE (Statement type: Update count only");
                                    int updateCount = stmt.getUpdateCount();

                                    map.put("updatecount", updateCount);
                                    LOGGER.info("Statement successfully executed! " + updateCount + " records added.");
                                }
                                else
                                {
                                    LOGGER.info("START dbexecute TRUE (Statement type: No return value)");
                                    map.put("", "");
                                    LOGGER.warn("No results for query");
                                }
                            }
                        }
                    }
                    con.close();
                } catch (SQLException ex)
                {
                    LOGGER.error("CATALOG: "+ catalog + " STATEMENT: " + sqlStatement);
                    LOGGER.error(ex.getMessage());
                    try {
                        con.close();
                    } catch (SQLException ex1) {
                        LOGGER.error(ex.getMessage());
                    }
                }
            }
            else
                LOGGER.warn("Connection to database could not be established!");
        }
        //LOGGER.info("END dbexecute");
        return map;
    }
    
    private TableFieldStructure getTableFieldsList(ResultSetMetaData dmd) {
        try {
            TableFieldStructure tfs = new TableFieldStructure();
            ArrayList<TableField> tableFieldsList = new ArrayList<>();
            int columncount = dmd.getColumnCount();
            for (int i = 1; i <= columncount; i++) {
                String columnName = dmd.getColumnLabel(i);
                int columnType = dmd.getColumnType(i);
                String columnTypeName = dmd.getColumnTypeName(i);
                int columnsize = dmd.getColumnDisplaySize(i);
                int decimaldigits = dmd.getPrecision(i);
                /*
                if (decimaldigits == null) {
                    decimaldigits = "0";
                }
                 */
                int isNullable = dmd.isNullable(i);
                //String is_autoIncrment = columns.getString("IS_AUTOINCREMENT");
                String is_autoIncrment = "";

                switch (columnType) {
                    case -1:      // TEXT, varchar, char -> String
                    case 1:
                    case 12:
                    case -15:
                    case -16:
                    case -9:
                    case 2005:
                        tableFieldsList.add(new TableField(columnName, "STRING", columnTypeName, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 2:       // int, smallint, tinyint
                    case 4:
                    case 5:
                    case -6:
                        tableFieldsList.add(new TableField(columnName, "INT", columnTypeName, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 7:       // real, decimal
                    case 3:
                        tableFieldsList.add(new TableField(columnName, "REAL", columnTypeName, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 8:       // float
                    case 6:
                        tableFieldsList.add(new TableField(columnName, "FLOAT", columnTypeName, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case -5:      // long
                        tableFieldsList.add(new TableField(columnName, "LONG", columnTypeName, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case -7:      // bit, boolean
                    case 16:
                        tableFieldsList.add(new TableField(columnName, "BOOLEAN", columnTypeName, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 93:      // Date
                    case 92:
                    case 91:
                    case 2014:
                        tableFieldsList.add(new TableField(columnName, "DATE", columnTypeName, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    default:
                        tableFieldsList.add(new TableField(columnName, "STRING", columnTypeName, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        LOGGER.error("FEHLENDE DATENTYPUMWANDLUNG: " + columnType);
                        break;
                }
            }
            tfs.setDefault_order("");
            tfs.setTableFieldsList(tableFieldsList);
            return tfs;
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }
}
