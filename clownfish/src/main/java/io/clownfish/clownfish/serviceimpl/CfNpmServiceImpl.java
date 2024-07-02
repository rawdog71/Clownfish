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

import io.clownfish.clownfish.daointerface.CfNpmDAO;
import io.clownfish.clownfish.dbentities.CfNpm;
import io.clownfish.clownfish.serviceinterface.CfNpmService;
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
public class CfNpmServiceImpl implements CfNpmService {
    private final CfNpmDAO cfmavenDAO;
    
    @Autowired
    public CfNpmServiceImpl(CfNpmDAO cfmavenDAO) {
        this.cfmavenDAO = cfmavenDAO;
    }
    
    @Override
    public CfNpm create(CfNpm entity) {
        return this.cfmavenDAO.create(entity);
    }

    @Override
    public boolean delete(CfNpm entity) {
        return this.cfmavenDAO.delete(entity);
    }

    @Override
    public CfNpm edit(CfNpm entity) {
        return this.cfmavenDAO.edit(entity);
    }    

    @Override
    public List<CfNpm> findAll() {
        return this.cfmavenDAO.findAll();
    }

    @Override
    public CfNpm findById(Long id) {
        return this.cfmavenDAO.findById(id);
    }

    @Override
    public CfNpm findByNpmId(String name) {
        return this.cfmavenDAO.findByNpmId(name);
    }

    @Override
    public CfNpm findByNpmIdAndNpmLatestversion(String name, String version) {
        return this.cfmavenDAO.findByNpmIdAndNpmLatestversion(name, version);
    }
}
