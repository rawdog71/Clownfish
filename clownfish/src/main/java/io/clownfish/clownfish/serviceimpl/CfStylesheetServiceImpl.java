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

import io.clownfish.clownfish.daointerface.CfStylesheetDAO;
import io.clownfish.clownfish.dbentities.CfStylesheet;
import io.clownfish.clownfish.serviceinterface.CfStylesheetService;
import java.io.Serializable;
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
public class CfStylesheetServiceImpl implements CfStylesheetService, Serializable {
    private final transient CfStylesheetDAO cfstylesheetDAO;
    
    @Autowired
    public CfStylesheetServiceImpl(CfStylesheetDAO cfpropertyDAO) {
        this.cfstylesheetDAO = cfpropertyDAO;
    }

    @Override
    public List<CfStylesheet> findAll() {
        return this.cfstylesheetDAO.findAll();
    }
    
    @Override
    public CfStylesheet create(CfStylesheet entity) {
        return this.cfstylesheetDAO.create(entity);
    }

    @Override
    public boolean delete(CfStylesheet entity) {
        return this.cfstylesheetDAO.delete(entity);
    }

    @Override
    public CfStylesheet edit(CfStylesheet entity) {
        return this.cfstylesheetDAO.edit(entity);
    }

    @Override
    public CfStylesheet findById(Long id) {
        return this.cfstylesheetDAO.findById(id);
    }

    @Override
    public CfStylesheet findByName(String name) {
        return this.cfstylesheetDAO.findByName(name);
    }
}
