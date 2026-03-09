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

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.exceptions.CsvValidationException;
import io.clownfish.clownfish.dbentities.CfDatasource;
import io.clownfish.clownfish.dbentities.CfSitedatasource;
import io.clownfish.clownfish.jdbc.JDBCUtil;
import io.clownfish.clownfish.jdbc.TableField;
import io.clownfish.clownfish.jdbc.TableFieldStructure;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 *
 * @author philip, sulzbachr
 */
@Scope("request")
@Component
public class ImportTemplateBean implements Serializable
{
    private CfDatasourceService cfdatasourceService;
    private List<CfSitedatasource> sitedatasourcelist;

    final transient Logger LOGGER = LoggerFactory.getLogger(ImportTemplateBean.class);

    public ImportTemplateBean()
    {
    }

    public void init(List<CfSitedatasource> sitedatasourcelist, CfDatasourceService cfdatasourceService)
    {
        this.sitedatasourcelist = sitedatasourcelist;
        this.cfdatasourceService = cfdatasourceService;
    }

    public void initjob(List<CfSitedatasource> sitedatasourcelist, CfDatasourceService cfdatasourceService)
    {
        this.sitedatasourcelist = sitedatasourcelist;
        this.cfdatasourceService = cfdatasourceService;
    }

    private static String generateSqlStatement(ArrayList<String> csvHeader, String tblName)
    {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("INSERT INTO ").append(tblName).append(" (");

        int iIndex = 0;

        for (String value : csvHeader)
        {
            ++iIndex;

            if (iIndex < csvHeader.size())
            {
                strBuilder.append(value).append(", ");
            }
            else if (iIndex == csvHeader.size())
            {
                strBuilder.append(value).append(") VALUES (").append("?, ".repeat(csvHeader.size() - 1)).append("?);");
            }
        }
        return strBuilder.toString();
    }

    public long readCsvAndFillDatabase(String fileIn, String schemaName, String tblName, boolean bHeader, boolean bTruncate, String encoding)
    {
        encoding = encoding != null ? encoding : "UTF8"; // Default UTF-8 encoding
        File fileIn1 = new File(fileIn);
        boolean status;
        long iTotalRecords = 0;

        for (CfSitedatasource sitedatasource : sitedatasourcelist)
        {
            try
            {
                CfDatasource cfdatasource = cfdatasourceService.findById(sitedatasource.getCfSitedatasourcePK().getDatasourceref());
                JDBCUtil jdbcutil = new JDBCUtil(cfdatasource.getDriverclass(), cfdatasource.getUrl(), cfdatasource.getUser(), cfdatasource.getPassword());
                Connection connection = jdbcutil.getConnection();

                if (connection != null)
                {
                    String catalogName;

                    if (cfdatasource.getDriverclass().contains("oracle"))
                    {     // Oracle driver
                        catalogName = connection.getSchema();
                    }
                    else
                    {                                                    // other drivers
                        catalogName = connection.getCatalog();
                    }
                    
                    boolean bSkipFirstLine;

                    if (catalogName.compareToIgnoreCase(schemaName) == 0)
                    {
                        ArrayList<String> header = new ArrayList<>();

                        // If our CSV has a header, use it
                        if (bHeader)
                        {
                            Reader readr = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn1), encoding));
                            CSVParser prsr = new CSVParserBuilder().withSeparator(';').withIgnoreLeadingWhiteSpace(true).withEscapeChar(ICSVParser.NULL_CHARACTER).build();
                            CSVReader csvReadr = new CSVReaderBuilder(readr).withCSVParser(prsr).build();
                            Collections.addAll(header, csvReadr.readNext());
                            bSkipFirstLine = true;

                            csvReadr.close();
                            readr.close();
                        }
                        // ...otherwise, grab all the table's column names
                        else
                        {
                            String query = "SELECT * FROM " + schemaName + "." + tblName + " LIMIT 1;";
                            Statement stmt = connection.createStatement();
                            ResultSet result = stmt.executeQuery(query);
                            ResultSetMetaData rmd = result.getMetaData();
                            //TableFieldStructure tfs = getTableFieldsList(rmd);

                            for (int i = 1; i <= rmd.getColumnCount(); i++)
                            {
                                header.add(rmd.getColumnName(i));
                            }
                            
                            bSkipFirstLine = false;
                        }

                        Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn1), encoding));
                        CSVParser parser = new CSVParserBuilder().withSeparator(';').withIgnoreLeadingWhiteSpace(true).withEscapeChar(ICSVParser.NULL_CHARACTER).build();
                        CSVReader csvReader;
                        
                        if (bSkipFirstLine)
                            csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).withSkipLines(1).build();
                        else
                            csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build();
                                 
                            String[] nextLine;
                            int iLines = 0;
                            final int iBatchSize = 10;

                            if (bTruncate)
                            {
                                Statement truncate = connection.createStatement();
                                truncate.execute("TRUNCATE TABLE " + tblName + ";");
                            }

                            PreparedStatement statement = connection.prepareStatement(generateSqlStatement(header, tblName));

                            // Read CSV and write to database in batches
                            while ((nextLine = csvReader.readNext()) != null)
                            {   
                                iLines++;

                                // Add results to batch
                                for (int i = 0; i < header.size(); i++)
                                {
                                    statement.setString(i + 1, nextLine[i].trim());
                                }
                                statement.addBatch();

                                // Execute SQL statement once batch size is reached
                                if (iLines >= iBatchSize)
                                {
                                    iTotalRecords += doExecute(statement);
                                    iLines = 0;
                                }

                                // Finish up remaining rows
                                if (iLines >= 0)
                                {
                                    //System.out.println(statement.toString());
                                    iTotalRecords += doExecute(statement);
                                } 
                            }

                            status = true;
                            LOGGER.info("Finished database import for " + fileIn + " successfully! " + iTotalRecords + " records added to " + tblName + ".");

                            csvReader.close();
                            reader.close();
                    }
                    connection.close();
                    return iTotalRecords;
                }
                else
                {
                    status = false;
                    LOGGER.error("Connection to database not established");
                    return -1;
                }
            }
            catch (SQLException | IOException | CsvValidationException ex)
            {
                LOGGER.error(ex.getMessage());
                return -1;
            }
        }
        return -1;
    }

    private int doExecute(PreparedStatement stmt)
    {
        int iTotalRecords = 0;
        int[] result = null;

        try
        {
            result = stmt.executeBatch();

            for (int i : result)
            {
                iTotalRecords += i;
            }
        }
        catch (BatchUpdateException e)
        {
            result = e.getUpdateCounts();

            for (int i : result)
            {
                iTotalRecords += i;
            }
            LOGGER.error("DOPPELT");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (IndexOutOfBoundsException ex) {
            LOGGER.error("DOPPELT");
            LOGGER.error(ex.getMessage());
            return -1;
        }

        return iTotalRecords;
    }
}
