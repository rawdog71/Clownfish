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

import io.clownfish.clownfish.daointerface.CfDatasourceDAO;
import io.clownfish.clownfish.dbentities.CfDatasource;
import io.clownfish.clownfish.serviceinterface.CfDatasourceService;
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
public class CfDatasourceServiceImpl implements CfDatasourceService {
    private final CfDatasourceDAO cfdatasourceDAO;
    
    @Autowired
    public CfDatasourceServiceImpl(CfDatasourceDAO cfdatasourceDAO) {
        this.cfdatasourceDAO = cfdatasourceDAO;
    }

    @Override
    public List<CfDatasource> findAll() {
        return this.cfdatasourceDAO.findAll();
    }

    @Override
    public CfDatasource findById(Long id) {
        return this.cfdatasourceDAO.findById(id);
    }

    @Override
    public CfDatasource create(CfDatasource entity) {
        return this.cfdatasourceDAO.create(entity);
    }

    @Override
    public boolean delete(CfDatasource entity) {
        return this.cfdatasourceDAO.delete(entity);
    }

    @Override
    public CfDatasource edit(CfDatasource entity) {
        return this.cfdatasourceDAO.edit(entity);
    }

    @Override
    public CfDatasource findByName(String name) {
        return this.cfdatasourceDAO.findByName(name);
    }

    @Override
    public List<CfDatasource> findByRestservice(boolean restservice) {
        return this.cfdatasourceDAO.findByRestservice(restservice);
    }
    
}
