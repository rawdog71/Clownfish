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

import io.clownfish.clownfish.daointerface.CfSearchdatabaseDAO;
import io.clownfish.clownfish.dbentities.CfSearchdatabase;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.clownfish.clownfish.serviceinterface.CfSearchdatabaseService;

/**
 *
 * @author sulzbachr
 */
@Service
@Transactional
public class CfSearchdatabaseServiceImpl implements CfSearchdatabaseService {
    private final CfSearchdatabaseDAO cfsearchdatabseDAO;
    
    @Autowired
    public CfSearchdatabaseServiceImpl(CfSearchdatabaseDAO cfsearchdatabseDAO) {
        this.cfsearchdatabseDAO = cfsearchdatabseDAO;
    }
    
    @Override
    public CfSearchdatabase findById(Long id) {
        return this.cfsearchdatabseDAO.findById(id);
    }

    @Override
    public List<CfSearchdatabase> findAll() {
        return this.cfsearchdatabseDAO.findAll();
    }

    @Override
    public CfSearchdatabase create(CfSearchdatabase entity) {
        return this.cfsearchdatabseDAO.create(entity);
    }

    @Override
    public boolean delete(CfSearchdatabase entity) {
        return this.cfsearchdatabseDAO.delete(entity);
    }

    @Override
    public CfSearchdatabase edit(CfSearchdatabase entity) {
        return this.cfsearchdatabseDAO.edit(entity);
    }

    @Override
    public CfSearchdatabase findByDatasourceRef(Long id) {
        return this.cfsearchdatabseDAO.findByDatasourceRef(id);
    }

    @Override
    public CfSearchdatabase findByDatasourceRefAndTable(Long id, String table) {
        return this.cfsearchdatabseDAO.findByDatasourceRefAndTable(id, table);
    }

}
