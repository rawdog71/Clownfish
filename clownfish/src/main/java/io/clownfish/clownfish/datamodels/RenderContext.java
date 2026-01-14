/*
 * Copyright 2026 SulzbachR.
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
package io.clownfish.clownfish.datamodels;

import java.util.List;
import java.util.Map;
import lombok.Data;
import org.apache.commons.fileupload.FileItem;

/**
 *
 * @author SulzbachR
 */
@Data
public class RenderContext {
    private String name;
    private List<JsonFormParameter> postmap;
    private List urlParams;
    private boolean makestatic;
    private List<FileItem> fileitems;
    private ClientInformation clientinfo;
    private String referrer;
    private Map searchcontentmap;
    private Map searchassetmap;
    private Map searchassetmetadatamap;
    private Map searchclasscontentmap;
    private Map searchmetadata;
    private Map sitecontentmap;
    private Map metainfomap;
    private Map parametermap;
}
