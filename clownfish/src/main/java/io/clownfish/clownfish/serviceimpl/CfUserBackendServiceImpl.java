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

import io.clownfish.clownfish.daointerface.CfUserBackendDAO;
import io.clownfish.clownfish.dbentities.CfUserbackend;
import io.clownfish.clownfish.serviceinterface.CfUserBackendService;
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
public class CfUserBackendServiceImpl implements CfUserBackendService {
    private final CfUserBackendDAO cfuserbackendDAO;
    
    @Autowired
    public CfUserBackendServiceImpl(CfUserBackendDAO cfuserbackendDAO) {
        this.cfuserbackendDAO = cfuserbackendDAO;
    }
    
    @Override
    public CfUserbackend create(CfUserbackend entity) {
        return this.cfuserbackendDAO.create(entity);
    }

    @Override
    public boolean delete(CfUserbackend entity) {
        return this.cfuserbackendDAO.delete(entity);
    }

    @Override
    public CfUserbackend edit(CfUserbackend entity) {
        return this.cfuserbackendDAO.edit(entity);
    }    

    @Override
    public List<CfUserbackend> findAll() {
        return this.cfuserbackendDAO.findAll();
    }

    @Override
    public List<CfUserbackend> findByUserRef(Long id) {
        return this.cfuserbackendDAO.findByUserRef(id);
    }

    @Override
    public List<CfUserbackend> findByBackendRef(Long id) {
        return this.cfuserbackendDAO.findByBackendRef(id);
    }
}
