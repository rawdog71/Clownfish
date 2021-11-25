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

import io.clownfish.clownfish.daointerface.CfMavenDAO;
import io.clownfish.clownfish.dbentities.CfMaven;
import io.clownfish.clownfish.serviceinterface.CfMavenService;
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
public class CfMavenServiceImpl implements CfMavenService {
    private final CfMavenDAO cfmavenDAO;
    
    @Autowired
    public CfMavenServiceImpl(CfMavenDAO cfmavenDAO) {
        this.cfmavenDAO = cfmavenDAO;
    }
    
    @Override
    public CfMaven create(CfMaven entity) {
        return this.cfmavenDAO.create(entity);
    }

    @Override
    public boolean delete(CfMaven entity) {
        return this.cfmavenDAO.delete(entity);
    }

    @Override
    public CfMaven edit(CfMaven entity) {
        return this.cfmavenDAO.edit(entity);
    }    

    @Override
    public List<CfMaven> findAll() {
        return this.cfmavenDAO.findAll();
    }

    @Override
    public CfMaven findById(Long id) {
        return this.cfmavenDAO.findById(id);
    }

    @Override
    public CfMaven findByMavenId(String name) {
        return this.cfmavenDAO.findByMavenId(name);
    }
}
