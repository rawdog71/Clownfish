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

import io.clownfish.clownfish.daointerface.CfQuartzDAO;
import io.clownfish.clownfish.dbentities.CfQuartz;
import io.clownfish.clownfish.serviceinterface.CfQuartzService;
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
public class CfQuartzServiceImpl implements CfQuartzService {
    private final CfQuartzDAO cfquartzDAO;
    
    @Autowired
    public CfQuartzServiceImpl(CfQuartzDAO cfquartzDAO) {
        this.cfquartzDAO = cfquartzDAO;
    }
    
    @Override
    public CfQuartz findById(Long id) {
        return this.cfquartzDAO.findById(id);
    }

    @Override
    public CfQuartz findByName(String name) {
        return this.cfquartzDAO.findByName(name);
    }

    @Override
    public List<CfQuartz> findAll() {
        return this.cfquartzDAO.findAll();
    }

    @Override
    public CfQuartz create(CfQuartz entity) {
        return this.cfquartzDAO.create(entity);
    }

    @Override
    public boolean delete(CfQuartz entity) {
        return this.cfquartzDAO.delete(entity);
    }

    @Override
    public CfQuartz edit(CfQuartz entity) {
        return this.cfquartzDAO.edit(entity);
    }
}
