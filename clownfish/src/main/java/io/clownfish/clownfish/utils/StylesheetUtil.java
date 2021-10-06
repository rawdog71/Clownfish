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
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfStylesheetversion;
import io.clownfish.clownfish.serviceinterface.CfStylesheetService;
import io.clownfish.clownfish.serviceinterface.CfStylesheetversionService;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.zip.DataFormatException;
import javax.faces.bean.ViewScoped;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@ViewScoped
@Component
@Accessors(chain = true)
public class StylesheetUtil implements Serializable {
    @Autowired transient CfStylesheetService cfstylesheetService;
    @Autowired transient CfStylesheetversionService cfstylesheetversionService;
    
    private @Getter @Setter long currentVersion;
    private @Getter @Setter String styelsheetContent = "";
    private transient @Getter @Setter Patch<String> patch = null;
    private transient @Getter @Setter List<String> source = null;
    private transient @Getter @Setter List<String> target = null;
    
    final transient Logger LOGGER = LoggerFactory.getLogger(StylesheetUtil.class);

    public StylesheetUtil() {
    }

    public String getVersion(long stylesheetref, long version) {
        try {
            CfStylesheetversion stylesheet = cfstylesheetversionService.findByPK(stylesheetref, version);
            byte[] decompress = CompressionUtils.decompress(stylesheet.getContent());
            return new String(decompress, StandardCharsets.UTF_8);
        } catch (IOException | DataFormatException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }
    
    public boolean hasDifference(CfStylesheet selectedStylesheet) {
        boolean diff = false;
        try {
            try {
            currentVersion = cfstylesheetversionService.findMaxVersion(selectedStylesheet.getId());
        } catch (NullPointerException ex) {
            currentVersion = 0;
        }
        if (currentVersion > 0) {
            styelsheetContent = selectedStylesheet.getContent();
            String contentVersion = getVersion(selectedStylesheet.getId(), currentVersion);
            source = Arrays.asList(styelsheetContent.split("\\r?\\n"));
            target = Arrays.asList(contentVersion.split("\\r?\\n"));
            patch = DiffUtils.diff(source, target);
            if (!patch.getDeltas().isEmpty()) {
                diff = true;
            }
        } else {
            diff = true;
        }
        } catch (DiffException ex) {
            LOGGER.error(ex.getMessage());
        }
        return diff;
    }
}
