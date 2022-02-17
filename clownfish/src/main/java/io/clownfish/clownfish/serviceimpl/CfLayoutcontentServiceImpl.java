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

import io.clownfish.clownfish.daointerface.CfLayoutcontentDAO;
import io.clownfish.clownfish.dbentities.CfLayoutcontent;
import io.clownfish.clownfish.serviceinterface.CfLayoutcontentService;
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
public class CfLayoutcontentServiceImpl implements CfLayoutcontentService {
    private final CfLayoutcontentDAO cfsitecontentDAO;
    
    @Autowired
    public CfLayoutcontentServiceImpl(CfLayoutcontentDAO cfsitecontentDAO) {
        this.cfsitecontentDAO = cfsitecontentDAO;
    }

    @Override
    public List<CfLayoutcontent> findAll() {
        return this.cfsitecontentDAO.findAll();
    }
    
    @Override
    public List<CfLayoutcontent> findBySiteref(Long ref) {
        return this.cfsitecontentDAO.findBySiteref(ref);
    }
    
    @Override
    public List<CfLayoutcontent> findBySiterefAndTemplateref(Long siteref, Long templateref) {
        return this.cfsitecontentDAO.findBySiterefAndTemplateref(siteref, templateref);
    }
    
    @Override
    public List<CfLayoutcontent> findBySiterefAndTemplaterefAndContenttype(Long siteref, Long templateref, String contenttype) {
        return this.cfsitecontentDAO.findBySiterefAndTemplaterefAndContenttype(siteref, templateref, contenttype);
    }

    @Override
    public CfLayoutcontent findBySiterefAndTemplaterefAndContenttypeAndLfdnr(Long siteref, Long templateref, String contenttype, int lfdnr) {
        return this.cfsitecontentDAO.findBySiterefAndTemplaterefAndContenttypeAndLfdnr(siteref, templateref, contenttype, lfdnr);
    }
    
    @Override
    public CfLayoutcontent create(CfLayoutcontent entity) {
        return this.cfsitecontentDAO.create(entity);
    }

    @Override
    public boolean delete(CfLayoutcontent entity) {
        return this.cfsitecontentDAO.delete(entity);
    }

    @Override
    public CfLayoutcontent edit(CfLayoutcontent entity) {
        return this.cfsitecontentDAO.edit(entity);
    }
}
