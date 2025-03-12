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
    @Getter @Setter ArrayList<CfSite> foundSites;
    @Getter @Setter ArrayList<CfAsset> foundAssets;
    @Getter @Setter HashMap<String, HashMap> foundAssetsMetadata;
    @Getter @Setter HashMap<String, ArrayList> foundClasscontent;
    @Getter @Setter ArrayList<CfTemplate> foundTemplates;
    @Getter @Setter ArrayList<CfJavascript> foundJavascripts;
    @Getter @Setter ArrayList<CfStylesheet> foundStylesheets;
    @Getter @Setter ArrayList<CfJava> foundJavas;

    public SearchResult() {
    }
}
