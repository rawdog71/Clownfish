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

import io.clownfish.clownfish.daointerface.CfSitelistDAO;
import io.clownfish.clownfish.dbentities.CfSitelist;
import io.clownfish.clownfish.serviceinterface.CfSitelistService;
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
public class CfSitelistServiceImpl implements CfSitelistService {
    private final CfSitelistDAO cfsitelistDAO;
    
    @Autowired
    public CfSitelistServiceImpl(CfSitelistDAO cfsitelistDAO) {
        this.cfsitelistDAO = cfsitelistDAO;
    }

    @Override
    public List<CfSitelist> findAll() {
        return this.cfsitelistDAO.findAll();
    }
    
    @Override
    public List<CfSitelist> findBySiteref(Long ref) {
        return this.cfsitelistDAO.findBySiteref(ref);
    }

    @Override
    public boolean create(CfSitelist entity) {
        return this.cfsitelistDAO.create(entity);
    }

    @Override
    public boolean delete(CfSitelist entity) {
        return this.cfsitelistDAO.delete(entity);
    }

    @Override
    public boolean edit(CfSitelist entity) {
        return this.cfsitelistDAO.edit(entity);
    }
}
