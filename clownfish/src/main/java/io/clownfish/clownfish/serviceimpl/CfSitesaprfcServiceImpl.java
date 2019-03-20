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

import io.clownfish.clownfish.daointerface.CfSitesaprfcDAO;
import io.clownfish.clownfish.dbentities.CfSitesaprfc;
import io.clownfish.clownfish.serviceinterface.CfSitesaprfcService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author sulzbachr
 */
@Service
@Transactional
public class CfSitesaprfcServiceImpl implements CfSitesaprfcService {
    private final CfSitesaprfcDAO cfsitesaprfcDAO;

    public CfSitesaprfcServiceImpl(CfSitesaprfcDAO cfsitesaprfcDAO) {
        this.cfsitesaprfcDAO = cfsitesaprfcDAO;
    }

    @Override
    public List<CfSitesaprfc> findAll() {
        return this.cfsitesaprfcDAO.findAll();
    }

    @Override
    public List<CfSitesaprfc> findBySiteref(long siteref) {
        return this.cfsitesaprfcDAO.findBySiteref(siteref);
    }

    @Override
    public List<CfSitesaprfc> findByRfcgroup(String rfcgroup) {
        return this.cfsitesaprfcDAO.findByRfcgroup(rfcgroup);
    }

    @Override
    public List<CfSitesaprfc> findByRfcfunction(String rfcfunction) {
        return this.cfsitesaprfcDAO.findByRfcfunction(rfcfunction);
    }

    @Override
    public boolean create(CfSitesaprfc entity) {
        return this.cfsitesaprfcDAO.create(entity);
    }

    @Override
    public boolean delete(CfSitesaprfc entity) {
        return this.cfsitesaprfcDAO.delete(entity);
    }

    @Override
    public boolean edit(CfSitesaprfc entity) {
        return this.cfsitesaprfcDAO.edit(entity);
    }
    
}
