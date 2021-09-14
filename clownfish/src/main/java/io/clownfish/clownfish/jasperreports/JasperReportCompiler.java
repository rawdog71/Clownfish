package io.clownfish.clownfish.jasperreports;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

import io.clownfish.clownfish.jdbc.JDBCUtil;
import net.sf.jasperreports.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JasperReportCompiler
{
    static transient Logger LOGGER = LoggerFactory.getLogger(JasperReportCompiler.class);
    
    public static ByteArrayOutputStream exportToPdf(String user, String password, String dataBaseUrl, InputStream template, String driver) {
        HashMap<String, Object> hm = new HashMap<>();
        try
        {
            JDBCUtil db = new JDBCUtil(driver, dataBaseUrl, user, password);

            // Fill the report
            JasperReport rp = JasperCompileManager.compileReport(template);
            JasperPrint print = JasperFillManager.fillReport(rp, hm, db.getConnection());
           
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(print, out);
            return out;
        }
        catch (JRException e)
        {
            LOGGER.error(e.getMessage());
            return null;
        }
    }
}