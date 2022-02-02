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
public class FolderUtil {
    private @Getter @Setter String cache_folder;
    private @Getter @Setter String static_folder;
    private @Getter @Setter String index_folder;
    private @Getter @Setter String media_folder;
    private @Getter @Setter String pdf_folder;
    private @Getter @Setter String attachments_folder;
    private @Getter @Setter String js_folder;
    private @Getter @Setter String css_folder;
    private @Getter @Setter String fonts_folder;
    
    @Autowired private PropertyUtil propertyUtil;

    //@PostConstruct
    public void init() {
        setCache_folder(propertyUtil.getPropertyValue("folder_cache"));
        setStatic_folder(propertyUtil.getPropertyValue("folder_static"));
        setIndex_folder(propertyUtil.getPropertyValue("folder_index"));
        setMedia_folder(propertyUtil.getPropertyValue("folder_media"));
        setPdf_folder(propertyUtil.getPropertyValue("folder_pdf"));
        setAttachments_folder(propertyUtil.getPropertyValue("folder_attachments"));
        setJs_folder(propertyUtil.getPropertyValue("folder_js"));
        setCss_folder(propertyUtil.getPropertyValue("folder_css"));
        setFonts_folder(propertyUtil.getPropertyValue("folder_fonts"));
    }
    
    public FolderUtil() {
    }
}
