/*
 * Copyright 2020 sulzbachr.
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
package io.clownfish.clownfish.beans;

import java.io.Serializable;
import javax.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulzbachr
 */
@Named("backendBean")
@Scope("singleton")
@Component
public class Backend implements Serializable  {
    @Autowired ContentList contentlist;
    @Autowired AssetList assetlist;
    @Autowired AssetLibrary assetlibrary;
    @Autowired ClassList classlist;
    @Autowired DataList datalist;
    @Autowired DatasourceList datasourcelist;
    @Autowired MavenList mavenlist;
    @Autowired KeywordList keywordlist;
    @Autowired transient TemplateList templatelist;
    @Autowired transient StylesheetList stylesheetlist;
    @Autowired transient JavascriptList javascriptlist;
    @Autowired transient JavaList javalist;

    public Backend() {
    }
    
    public void onRefreshAll() {
        contentlist.onRefreshAll();
        assetlist.onRefreshAll();
        assetlibrary.onRefreshAll();
        classlist.onRefreshAll();
        datalist.onRefreshAll();
        datasourcelist.onRefreshAll();
        keywordlist.onRefreshAll();
        templatelist.refresh();
        stylesheetlist.refresh();
        javascriptlist.refresh();
        javalist.refresh();
        mavenlist.onRefreshAll();
    }
}
