/*
 * Copyright 2022 raine.
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

import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfTemplate;
import javax.inject.Named;
import org.primefaces.component.tabview.TabView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author raine
 */
@Named("tabBean")
@Scope("session")
@Component
public class TabBean {
    private TabView tabView;
    @Autowired transient TemplateList templatelist;
    @Autowired transient StylesheetList stylesheetlist;
    @Autowired transient JavascriptList javascriptlist;
    
    public TabView getTabView() {
        return tabView;
    }

    public void setTabView(TabView tabView) {
        this.tabView = tabView;
    }
    
    public void setTabViewTemplate(int index, CfTemplate template) {
        this.tabView.setActiveIndex(index);
        templatelist.setSelectedTemplate(template);
        templatelist.selectTemplate();
    }
    
    public void setTabViewStylesheet(int index, CfStylesheet stylesheet) {
        this.tabView.setActiveIndex(index);
        stylesheetlist.setSelectedStylesheet(stylesheet);
        stylesheetlist.selectStylesheet();
    }
    
    public void setTabViewJavascript(int index, CfJavascript javascript) {
        this.tabView.setActiveIndex(index);
        javascriptlist.setSelectedJavascript(javascript);
        javascriptlist.selectJavascript();
    }
}
