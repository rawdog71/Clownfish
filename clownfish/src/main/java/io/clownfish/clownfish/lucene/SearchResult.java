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
package io.clownfish.clownfish.lucene;

import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.dbentities.CfJava;
import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author sulzbachr
 */
public class SearchResult {
    private @Getter @Setter ArrayList<CfSite> foundSites;
    private @Getter @Setter ArrayList<CfAsset> foundAssets;
    private @Getter @Setter HashMap<String, HashMap> foundAssetsMetadata;
    private @Getter @Setter HashMap<String, String> foundClasscontent;
    private @Getter @Setter ArrayList<CfTemplate> foundTemplates;
    private @Getter @Setter ArrayList<CfJavascript> foundJavascripts;
    private @Getter @Setter ArrayList<CfStylesheet> foundStylesheets;
    private @Getter @Setter ArrayList<CfJava> foundJavas;

    public SearchResult() {
    }
}
