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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Component
public class DatabaseTemplateBean {
    @Autowired CfDatasourceService cfdatasourceService;
    private @Getter @Setter Map contentmap;
    private List<CfSitedatasource> sitedatasourcelist;
    
    public DatabaseTemplateBean() {
        contentmap = new HashMap<>();
    }
    
    public void init(List<CfSitedatasource> sitedatasourcelist) {
        this.sitedatasourcelist = sitedatasourcelist;
    }
    
    public Map dbread(String catalog, String tablename, String sqlstatement) {
        System.out.println("DBREAD->catalog: " + catalog);
        System.out.println("DBREAD->tablename: " + tablename);
        System.out.println("DBREAD->sqlstatement: " + sqlstatement);
        HashMap<String, ArrayList> dbtables = new HashMap<>();
        for (CfSitedatasource sitedatasource : sitedatasourcelist) {
            try {
                CfDatasource cfdatasource = cfdatasourceService.findById(sitedatasource.getCfSitedatasourcePK().getDatasourceref());
                
                JDBCUtil jdbcutil = new JDBCUtil(cfdatasource.getDriverclass(), cfdatasource.getUrl(), cfdatasource.getUser(), cfdatasource.getPassword());
                Connection con = jdbcutil.getConnection();
                if (con.getCatalog().compareToIgnoreCase(catalog) == 0) {
                    //System.out.println(sqlstatement);
                    Statement stmt = con.createStatement();
                    ResultSet result = stmt.executeQuery(sqlstatement);
                    ResultSetMetaData rmd = result.getMetaData();
                    TableFieldStructure tfs = getTableFieldsList(rmd);
                    
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
                }
                //((HashMap)((HashMap) sitecontentmap.get("db")).get(cfdatasource.getDatabasename())).put(namespace, dbtables);
                contentmap.put("db", dbtables);
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseTemplateBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println(contentmap);
        return contentmap;
    }
    
    private TableFieldStructure getTableFieldsList(ResultSetMetaData dmd) {
        try {
            TableFieldStructure tfs = new TableFieldStructure();
            ArrayList<TableField> tableFieldsList = new ArrayList<>();
            int columncount = dmd.getColumnCount();
            for (int i = 1; i <= columncount; i++) {
                String columnName = dmd.getColumnName(i);
                int colomuntype = dmd.getColumnType(i);
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
                    case 1:      // varchar -> String
                        tableFieldsList.add(new TableField(columnName, "STRING", false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 2:       // int
                        tableFieldsList.add(new TableField(columnName, "INT", false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 4:       // int
                        tableFieldsList.add(new TableField(columnName, "INT", false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 5:       // smallint
                        tableFieldsList.add(new TableField(columnName, "INT", false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 7:       // real
                        tableFieldsList.add(new TableField(columnName, "REAL", false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;            
                    case 8:       // float
                        tableFieldsList.add(new TableField(columnName, "FLOAT", false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 12:      // varchar -> String
                        tableFieldsList.add(new TableField(columnName, "STRING", false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case -5:      // long
                        tableFieldsList.add(new TableField(columnName, "LONG", false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 2005:    // text -> String
                        tableFieldsList.add(new TableField(columnName, "STRING", false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                    case 93:      // Date
                        tableFieldsList.add(new TableField(columnName, "DATE", false, columnsize, decimaldigits, String.valueOf(isNullable)));
                        break;
                }
            }
            tfs.setDefault_order("");
            tfs.setTableFieldsList(tableFieldsList);
            return tfs;
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseTemplateBean.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
