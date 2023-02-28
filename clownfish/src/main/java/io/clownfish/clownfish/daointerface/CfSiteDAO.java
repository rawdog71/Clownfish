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
package io.clownfish.clownfish.daointerface;

import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfTemplate;
import java.util.List;

/**
 *
 * @author sulzbachr
 */
public interface CfSiteDAO {
    List<CfSite> findAll();
    CfSite findById(Long id);
    CfSite findByName(String name);
    CfSite findByAliaspath(String alias);
    List<CfSite> findByTemplateref(CfTemplate ref);
    List<CfSite> findByParentref(CfSite ref);
    List<CfSite> findByStylesheetref(CfStylesheet ref);
    List<CfSite> findByJavascriptref(CfJavascript ref);
    List<CfSite> findBySitemap(boolean sitemap);
    CfSite findByShorturl(String shorturl);
    CfSite create(CfSite entity);
    boolean delete(CfSite entity);
    CfSite edit(CfSite entity);
}
