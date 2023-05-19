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

import io.clownfish.clownfish.daointerface.CfStaticsiteDAO;
import io.clownfish.clownfish.dbentities.CfStaticsite;
import io.clownfish.clownfish.serviceinterface.CfStaticsiteService;
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
public class CfStaticsiteServiceImpl implements CfStaticsiteService {
    private final CfStaticsiteDAO cfstaticsiteDAO;
    
    @Autowired
    public CfStaticsiteServiceImpl(CfStaticsiteDAO cfstaticsiteDAO) {
        this.cfstaticsiteDAO = cfstaticsiteDAO;
    }

    @Override
    public List<CfStaticsite> findAll() {
        return this.cfstaticsiteDAO.findAll();
    }

    @Override
    public CfStaticsite findById(Long id) {
        return this.cfstaticsiteDAO.findById(id);
    }

    @Override
    public List<CfStaticsite> findBySite(String name) {
        return this.cfstaticsiteDAO.findBySite(name);
    }

    @Override
    public CfStaticsite findByUrlparams(String alias) {
        return this.cfstaticsiteDAO.findByUrlparams(alias);
    }

    @Override
    public CfStaticsite create(CfStaticsite entity) {
        return this.cfstaticsiteDAO.create(entity);
    }

    @Override
    public boolean delete(CfStaticsite entity) {
        return this.cfstaticsiteDAO.delete(entity);
    }

    @Override
    public CfStaticsite edit(CfStaticsite entity) {
        return this.cfstaticsiteDAO.edit(entity);
    }
}
