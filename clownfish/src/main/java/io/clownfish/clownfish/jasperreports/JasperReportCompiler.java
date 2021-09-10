package io.clownfish.clownfish.jasperreports;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import io.clownfish.clownfish.jdbc.JDBCUtil;
import net.sf.jasperreports.engine.*;

public class JasperReportCompiler
{
    public static ByteArrayOutputStream exportToPdf(String user, String password, String dataBaseUrl, InputStream template, String driver) {
        HashMap<String, Object> hm = new HashMap<>();
        try
        {
            JDBCUtil db = new JDBCUtil(driver, dataBaseUrl, user, password);

            // Fill the report
            JasperReport rp = JasperCompileManager.compileReport(template);
            JasperPrint print = JasperFillManager.fillReport(rp, hm, db.getConnection());

            // Create a PDF exporter
            //JRPdfExporter exporter = new JRPdfExporter();

            // Configure the exporter (set output file name and print object)
            //exporter.setExporterInput(new SimpleExporterInput(print));
            //exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outFileName));
            //SimplePdfExporterConfiguration config = new SimplePdfExporterConfiguration();
            //exporter.setConfiguration(config);

            // Export the PDF file
            //exporter.exportReport();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(print, out);
            return out;
        }
        catch (JRException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}