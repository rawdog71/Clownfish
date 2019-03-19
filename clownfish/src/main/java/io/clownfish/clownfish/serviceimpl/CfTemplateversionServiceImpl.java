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

import io.clownfish.clownfish.daointerface.CfTemplateversionDAO;
import io.clownfish.clownfish.dbentities.CfTemplateversion;
import io.clownfish.clownfish.serviceinterface.CfTemplateversionService;
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
public class CfTemplateversionServiceImpl implements CfTemplateversionService {
    private final CfTemplateversionDAO cftemplateversionDAO;
    
    @Autowired
    public CfTemplateversionServiceImpl(CfTemplateversionDAO cftemplateversionDAO) {
        this.cftemplateversionDAO = cftemplateversionDAO;
    }

    @Override
    public boolean create(CfTemplateversion entity) {
        return this.cftemplateversionDAO.create(entity);
    }

    @Override
    public boolean delete(CfTemplateversion entity) {
        return this.cftemplateversionDAO.delete(entity);
    }

    @Override
    public boolean edit(CfTemplateversion entity) {
        return this.cftemplateversionDAO.edit(entity);
    }

    @Override
    public List<CfTemplateversion> findAll() {
        return this.cftemplateversionDAO.findAll();
    }

    @Override
    public List<CfTemplateversion> findByTemplateref(long ref) {
        return this.cftemplateversionDAO.findByTemplateref(ref);
    }

    @Override
    public long findMaxVersion(long ref) {
        return this.cftemplateversionDAO.findMaxVersion(ref);
    }

    @Override
    public CfTemplateversion findByPK(long ref, long version) {
        return this.cftemplateversionDAO.findByPK(ref, version);
    }
}
