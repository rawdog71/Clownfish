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

import io.clownfish.clownfish.daointerface.CfBackendDAO;
import io.clownfish.clownfish.dbentities.CfBackend;
import io.clownfish.clownfish.serviceinterface.CfBackendService;
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
public class CfBackendServiceImpl implements CfBackendService {
    private final CfBackendDAO cfbackendDAO;
    
    @Autowired
    public CfBackendServiceImpl(CfBackendDAO cfbackendDAO) {
        this.cfbackendDAO = cfbackendDAO;
    }
    
    @Override
    public boolean create(CfBackend entity) {
        return this.cfbackendDAO.create(entity);
    }

    @Override
    public boolean delete(CfBackend entity) {
        return this.cfbackendDAO.delete(entity);
    }

    @Override
    public boolean edit(CfBackend entity) {
        return this.cfbackendDAO.edit(entity);
    }    

    @Override
    public List<CfBackend> findAll() {
        return this.cfbackendDAO.findAll();
    }

    @Override
    public CfBackend findById(Long id) {
        return this.cfbackendDAO.findById(id);
    }

    @Override
    public CfBackend findByName(String name) {
        return this.cfbackendDAO.findByName(name);
    }

}
