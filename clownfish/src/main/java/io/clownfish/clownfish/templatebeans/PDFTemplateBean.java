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
import com.opencsv.exceptions.CsvValidationException;
import io.clownfish.clownfish.dbentities.CfDatasource;
import io.clownfish.clownfish.dbentities.CfSitedatasource;
import io.clownfish.clownfish.jdbc.JDBCUtil;
import io.clownfish.clownfish.jdbc.TableField;
import io.clownfish.clownfish.jdbc.TableFieldStructure;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import io.clownfish.clownfish.utils.MailUtil;
import io.clownfish.clownfish.utils.PDFUtil;
import io.clownfish.clownfish.utils.PropertyUtil;
import org.apache.tika.metadata.PDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 *
 * @author philip, sulzbachr
 */
@Scope("prototype")
@Component
public class PDFTemplateBean implements Serializable
{
    @Autowired PDFUtil pdfUtil;

    final transient Logger LOGGER = LoggerFactory.getLogger(PDFTemplateBean.class);

    public PDFTemplateBean()
    {
    }

    public void init(PDFUtil pdfUtil)
    {
        this.pdfUtil = pdfUtil;
    }

    public void initjob(PDFUtil pdfUtil)
    {
        this.pdfUtil = pdfUtil;
    }

    public void generatePDF(String siteName, String param) throws IOException
    {
        pdfUtil.createPDF(siteName, param);
    }
}
