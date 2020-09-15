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

import io.clownfish.clownfish.daointerface.CfSitedatasourceDAO;
import io.clownfish.clownfish.dbentities.CfSitedatasource;
import io.clownfish.clownfish.serviceinterface.CfSitedatasourceService;
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
public class CfSitedatasourceServiceImpl implements CfSitedatasourceService {
    private final CfSitedatasourceDAO cfsitedatasourceDAO;
    
    @Autowired
    public CfSitedatasourceServiceImpl(CfSitedatasourceDAO cfsitedatasourceDAO) {
        this.cfsitedatasourceDAO = cfsitedatasourceDAO;
    }

    @Override
    public List<CfSitedatasource> findAll() {
        return this.cfsitedatasourceDAO.findAll();
    }
    
    @Override
    public List<CfSitedatasource> findBySiteref(Long ref) {
        return this.cfsitedatasourceDAO.findBySiteref(ref);
    }

    @Override
    public CfSitedatasource create(CfSitedatasource entity) {
        return this.cfsitedatasourceDAO.create(entity);
    }

    @Override
    public boolean delete(CfSitedatasource entity) {
        return this.cfsitedatasourceDAO.delete(entity);
    }

    @Override
    public CfSitedatasource edit(CfSitedatasource entity) {
        return this.cfsitedatasourceDAO.edit(entity);
    }

    @Override
    public List<CfSitedatasource> findByDatasourceref(Long datasourceref) {
        return this.cfsitedatasourceDAO.findByDatasourceref(datasourceref);
    }
}
