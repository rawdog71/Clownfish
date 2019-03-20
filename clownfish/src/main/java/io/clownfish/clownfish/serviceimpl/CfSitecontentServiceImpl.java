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


import io.clownfish.clownfish.daointerface.CfSitecontentDAO;
import io.clownfish.clownfish.dbentities.CfSitecontent;
import io.clownfish.clownfish.serviceinterface.CfSitecontentService;
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
public class CfSitecontentServiceImpl implements CfSitecontentService {
    private final CfSitecontentDAO cfsitecontentDAO;
    
    @Autowired
    public CfSitecontentServiceImpl(CfSitecontentDAO cfsitecontentDAO) {
        this.cfsitecontentDAO = cfsitecontentDAO;
    }

    @Override
    public List<CfSitecontent> findAll() {
        return this.cfsitecontentDAO.findAll();
    }
    
    @Override
    public List<CfSitecontent> findBySiteref(Long ref) {
        return this.cfsitecontentDAO.findBySiteref(ref);
    }

    @Override
    public boolean create(CfSitecontent entity) {
        return this.cfsitecontentDAO.create(entity);
    }

    @Override
    public boolean delete(CfSitecontent entity) {
        return this.cfsitecontentDAO.delete(entity);
    }

    @Override
    public boolean edit(CfSitecontent entity) {
        return this.cfsitecontentDAO.edit(entity);
    }
}
