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

import io.clownfish.clownfish.daointerface.CfTemplateDAO;
import io.clownfish.clownfish.dbentities.CfTemplate;
import io.clownfish.clownfish.serviceinterface.CfTemplateService;
import java.io.Serializable;
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
public class CfTemplateServiceImpl implements CfTemplateService, Serializable {
    private transient final CfTemplateDAO cftemplateDAO;
    
    @Autowired
    public CfTemplateServiceImpl(CfTemplateDAO cftemplateDAO) {
        this.cftemplateDAO = cftemplateDAO;
    }

    @Override
    public boolean create(CfTemplate entity) {
        return this.cftemplateDAO.create(entity);
    }

    @Override
    public boolean delete(CfTemplate entity) {
        return this.cftemplateDAO.delete(entity);
    }

    @Override
    public boolean edit(CfTemplate entity) {
        return this.cftemplateDAO.edit(entity);
    }

    @Override
    public CfTemplate findById(Long id) {
        return this.cftemplateDAO.findById(id);
    }

    @Override
    public CfTemplate findByName(String name) {
        return this.cftemplateDAO.findByName(name);
    }

    @Override
    public List<CfTemplate> findAll() {
        return this.cftemplateDAO.findAll();
    }
}
