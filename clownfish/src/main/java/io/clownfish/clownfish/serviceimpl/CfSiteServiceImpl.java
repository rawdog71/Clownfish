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
import io.clownfish.clownfish.dbentities.CfSite;
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

    @Override
    public List<CfSite> findAll() {
        return this.cfsiteDAO.findAll();
    }

    @Override
    public CfSite findById(Long id) {
        return this.cfsiteDAO.findById(id);
    }

    @Override
    public CfSite findByName(String name) {
        return this.cfsiteDAO.findByName(name);
    }

    @Override
    public CfSite findByTemplateref(Long ref) {
        return this.cfsiteDAO.findByTemplateref(ref);
    }

    @Override
    public boolean create(CfSite entity) {
        return this.cfsiteDAO.create(entity);
    }

    @Override
    public boolean delete(CfSite entity) {
        return this.cfsiteDAO.delete(entity);
    }

    @Override
    public boolean edit(CfSite entity) {
        return this.cfsiteDAO.edit(entity);
    }

    @Override
    public List<CfSite> findByParentref(Long ref) {
        return this.cfsiteDAO.findByParentref(ref);
    }

}
