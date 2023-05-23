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
package io.clownfish.clownfish.serviceimpl;

import io.clownfish.clownfish.daointerface.CfSiteDAO;
import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.dbentities.CfSite;
import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.serviceinterface.CfSiteService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author sulzbachr
 */
@Service
@Transactional
public class CfSiteServiceImpl implements CfSiteService {
    private final CfSiteDAO cfsiteDAO;
    
    @Autowired
    public CfSiteServiceImpl(CfSiteDAO cfsiteDAO) {
        this.cfsiteDAO = cfsiteDAO;
    }

    //@Cacheable(value = "site")
    @Override
    public List<CfSite> findAll() {
        return this.cfsiteDAO.findAll();
    }

    //@Cacheable(value = "site", key = "#id")
    @Override
    public CfSite findById(Long id) {
        return this.cfsiteDAO.findById(id);
    }

    //@Cacheable(value = "site", key = "#name")
    @Override
    public CfSite findByName(String name) {
        return this.cfsiteDAO.findByName(name);
    }

    //@Cacheable(value = "site", key = "#ref")
    @Override
    public List<CfSite> findByTemplateref(CfTemplate ref) {
        return this.cfsiteDAO.findByTemplateref(ref);
    }

    //@CachePut(value = "site", key = "#entity.id")
    @Override
    public CfSite create(CfSite entity) {
        return this.cfsiteDAO.create(entity);
    }

    //@CacheEvict(value = "site", key = "#entity.id")
    @Override
    public boolean delete(CfSite entity) {
        return this.cfsiteDAO.delete(entity);
    }

    //@CachePut(value = "site", key = "#entity.id")
    @Override
    public CfSite edit(CfSite entity) {
        return this.cfsiteDAO.edit(entity);
    }

    //@Cacheable(value = "site", key = "#ref")
    @Override
    public List<CfSite> findByParentref(CfSite ref) {
        return this.cfsiteDAO.findByParentref(ref);
    }

    //@Cacheable(value = "site", key = "#alias")
    @Override
    public CfSite findByAliaspath(String alias) {
        return this.cfsiteDAO.findByAliaspath(alias);
    }

    @Override
    public List<CfSite> findBySitemap(boolean sitemap) {
        return this.cfsiteDAO.findBySitemap(sitemap);
    }

    @Override
    public CfSite findByShorturl(String shorturl) {
        return this.cfsiteDAO.findByShorturl(shorturl);
    }

    @Override
    public List<CfSite> findByStylesheetref(CfStylesheet ref) {
        return this.cfsiteDAO.findByStylesheetref(ref);
    }

    @Override
    public List<CfSite> findByJavascriptref(CfJavascript ref) {
        return this.cfsiteDAO.findByJavascriptref(ref);
    }

}
