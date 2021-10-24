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
import java.io.Serializable;
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

/**
 *
 * @author sulzbachr
 */
@Scope("prototype")
@Component
public class DatabaseTemplateBean implements Serializable {
    private CfDatasourceService cfdatasourceService;
    private transient @Getter @Setter Map contentmap;
    private List<CfSitedatasource> sitedatasourcelist;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(DatabaseTemplateBean.class);
    
    public DatabaseTemplateBean() {
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
        //LOGGER.info("START dbread");
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
                        LOGGER.error(ex1.getMessage());
                        con.close();
                    }
                    finally {
                        try {
                            con.close();
                        }
                        catch (SQLException e) {
                            LOGGER.warn(e.getMessage());
                        }
                    }
                } else {
                    LOGGER.warn("Connection to database not established");
                }
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage());
            }
        });
        //LOGGER.info("END dbread");
        return contentmap;
    }
    
    public boolean dbexecute(String catalog, String sqlstatement) {
        boolean ok = false;
        LOGGER.info("START dbexecute: " + sqlstatement);
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
                    } catch (SQLException ex) {
                        LOGGER.error(ex.getMessage());
                        try {
                            con.close();
                        } catch (SQLException ex1) {
                            LOGGER.error(ex1.getMessage());
                        }
                    }
                } else {
                    LOGGER.warn("Connection to database not established");
                }
        };
        LOGGER.info("END dbexecute");
        return ok;
    }

    public Map dbexecute(String[] params)
    {
        Map map = new HashMap<>();
        String catalog = params[0];
        String sqlStatement = params[1];
        LOGGER.info("START dbexecute:\n" + sqlStatement);

        for (CfSitedatasource sitedatasource : sitedatasourcelist)
        {
            CfDatasource cfdatasource = cfdatasourceService.findById(sitedatasource.getCfSitedatasourcePK().getDatasourceref());
            JDBCUtil jdbcutil = new JDBCUtil(cfdatasource.getDriverclass(), cfdatasource.getUrl(), cfdatasource.getUser(), cfdatasource.getPassword());
            Connection con = jdbcutil.getConnection();
            if (null != con)
            {
                String catalogName;

                try
                {
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
                                LOGGER.info("START dbexecute TRUE (Statement type: Result set");
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
                                            LOGGER.warn(ex.getMessage());
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
                } 
                catch (SQLException ex)
                {
                    try {
                        LOGGER.error(ex.getMessage());
                        con.close();
                    } catch (SQLException ex1) {
                        LOGGER.error(ex1.getMessage());
                    }
                }
            }
            else
                LOGGER.warn("Connection to database could not be established!");
        }
        LOGGER.info("END dbexecute");
        return map;
    }
    
    private TableFieldStructure getTableFieldsList(ResultSetMetaData dmd) {
        try {
            TableFieldStructure tfs = new TableFieldStructure();
            ArrayList<TableField> tableFieldsList = new ArrayList<>();
            int columncount = dmd.getColumnCount();
            for (int i = 1; i <= columncount; i++) {
                String columnName = dmd.getColumnName(i);
                int colomuntype = dmd.getColumnType(i);
                String colomuntypename = dmd.getColumnTypeName(i);
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

                switch (colomuntype) {
                    case -1:      // TEXT -> String
                        tableFieldsList.add(new TableField(columnName, "STRING", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 1:      // varchar -> String
                        tableFieldsList.add(new TableField(columnName, "STRING", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 2:       // int
                        tableFieldsList.add(new TableField(columnName, "INT", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 4:       // int
                        tableFieldsList.add(new TableField(columnName, "INT", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 5:       // smallint
                        tableFieldsList.add(new TableField(columnName, "INT", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 7:       // real
                        tableFieldsList.add(new TableField(columnName, "REAL", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;            
                    case 8:       // float
                        tableFieldsList.add(new TableField(columnName, "FLOAT", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 12:      // varchar -> String
                        tableFieldsList.add(new TableField(columnName, "STRING", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case -5:      // long
                        tableFieldsList.add(new TableField(columnName, "LONG", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case -7:      // bit
                        tableFieldsList.add(new TableField(columnName, "BOOLEAN", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;    
                    case 2005:    // text -> String
                        tableFieldsList.add(new TableField(columnName, "STRING", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 93:      // Date
                        tableFieldsList.add(new TableField(columnName, "DATE", colomuntypename, false, columnsize, decimaldigits, String.valueOf(isNullable)));
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
