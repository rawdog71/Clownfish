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
package io.clownfish.clownfish.utils;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfJavascriptversion;
import io.clownfish.clownfish.dbentities.CfJavascriptversionPK;
import io.clownfish.clownfish.serviceinterface.CfJavascriptService;
import io.clownfish.clownfish.serviceinterface.CfJavascriptversionService;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.zip.DataFormatException;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Scope("singleton")
@Component
@Accessors(chain = true)
public class JavascriptUtil implements IVersioningInterface, Serializable {
    @Autowired transient CfJavascriptService cfjavascriptService;
    @Autowired transient CfJavascriptversionService cfjavascriptversionService;
    @Autowired private FolderUtil folderUtil;
    
    private @Getter @Setter long currentVersion;
    private @Getter @Setter String javascriptContent = "";
    private transient @Getter @Setter Patch<String> patch = null;
    private transient @Getter @Setter List<String> source = null;
    private transient @Getter @Setter List<String> target = null;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(JavascriptUtil.class);

    public JavascriptUtil() {
    }

    @Override
    public String getVersion(long javascriptref, long version) {
        try {
            CfJavascriptversion javascript = cfjavascriptversionService.findByPK(javascriptref, version);
            byte[] decompress = CompressionUtils.decompress(javascript.getContent());
            return new String(decompress, StandardCharsets.UTF_8);
        } catch (IOException | DataFormatException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }
    
    @Override
    public void writeVersion(long javascriptref, long version, byte[] content, long currentuserid) {
        CfJavascriptversionPK javascriptversionpk = new CfJavascriptversionPK();
        javascriptversionpk.setJavascriptref(javascriptref);
        javascriptversionpk.setVersion(version);

        CfJavascriptversion cfjavascriptversion = new CfJavascriptversion();
        cfjavascriptversion.setCfJavascriptversionPK(javascriptversionpk);
        cfjavascriptversion.setContent(content);
        cfjavascriptversion.setTstamp(new Date());
        cfjavascriptversion.setCommitedby(BigInteger.valueOf(currentuserid));
        cfjavascriptversionService.create(cfjavascriptversion);
    }
    
    @Override
    public boolean hasDifference(Object object) {
        boolean diff = false;
        try {
            currentVersion = cfjavascriptversionService.findMaxVersion(((CfJavascript)object).getId());
        } catch (NullPointerException ex) {
            currentVersion = 0;
        }
        if (currentVersion > 0) {
            javascriptContent = ((CfJavascript)object).getContent();
            String contentVersion = getVersion(((CfJavascript)object).getId(), currentVersion);
            source = Arrays.asList(javascriptContent.split("\\r?\\n"));
            target = Arrays.asList(contentVersion.split("\\r?\\n"));
            patch = DiffUtils.diff(source, target);
            if (!patch.getDeltas().isEmpty()) {
                diff = true;
            }
        } else {
            diff = true;
        }
        return diff;
    }

    @Override
    public long getCurrentVersionNumber(String name) {
        CfJavascript cfjavascript = cfjavascriptService.findByName(name);
        return cfjavascriptversionService.findMaxVersion((cfjavascript).getId());
    }

    @Override
    public String getUniqueName(String name) {
        int i = 1;
        boolean found = false;
        do {
            CfJavascript dummy = cfjavascriptService.findByName(name+"_"+i);
            if (null != dummy) {
                i++;
            } else {
                found = true;
            }
        } while (!found);
        return name+"_"+i;
    }
    
    public void commit(CfJavascript selectedJavascript) {
        boolean canCommit = false;
            
        if (hasDifference(selectedJavascript)) {
            canCommit = true;
        }
        if (canCommit) {
            try {
                try {
                    long maxversion = cfjavascriptversionService.findMaxVersion(selectedJavascript.getId());
                    byte[] output = CompressionUtils.compress(selectedJavascript.getContent().getBytes("UTF-8"));
                    setCurrentVersion(maxversion + 1);
                    writeVersion(selectedJavascript.getId(), getCurrentVersion(), output, 0);
                } catch (NullPointerException npe) {
                    byte[] output = CompressionUtils.compress(selectedJavascript.getContent().getBytes("UTF-8"));
                    writeVersion(selectedJavascript.getId(), 1, output, 0);
                    setCurrentVersion(1);
                }
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }
    
    public void writeStaticJS(String filename, String js) {
        FileOutputStream fileStream = null;
        try {
            fileStream = new FileOutputStream(new File(folderUtil.getJs_folder()+ File.separator + filename + ".js"));
            OutputStreamWriter writer = new OutputStreamWriter(fileStream, "UTF-8");
            try {
                writer.write(js);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create the destination file", e);
            }
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            LOGGER.error(ex.getMessage());
        } finally {
            try {
                if (null != fileStream) {
                    fileStream.close();
                }
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }
}
