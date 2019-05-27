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

import io.clownfish.clownfish.daointerface.CfJavascriptDAO;
import io.clownfish.clownfish.dbentities.CfJavascript;
import io.clownfish.clownfish.serviceinterface.CfJavascriptService;
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
public class CfJavascriptServiceImpl implements CfJavascriptService, Serializable {
    private final CfJavascriptDAO cfjavascriptDAO;
    
    @Autowired
    public CfJavascriptServiceImpl(CfJavascriptDAO cfpropertyDAO) {
        this.cfjavascriptDAO = cfpropertyDAO;
    }

    @Override
    public List<CfJavascript> findAll() {
        return this.cfjavascriptDAO.findAll();
    }

    @Override
    public boolean create(CfJavascript entity) {
        return this.cfjavascriptDAO.create(entity);
    }

    @Override
    public boolean delete(CfJavascript entity) {
        return this.cfjavascriptDAO.delete(entity);
    }

    @Override
    public boolean edit(CfJavascript entity) {
        return this.cfjavascriptDAO.edit(entity);
    }

    @Override
    public CfJavascript findById(Long id) {
        return this.cfjavascriptDAO.findById(id);
    }

    @Override
    public CfJavascript findByName(String name) {
        return this.cfjavascriptDAO.findByName(name);
    }
}
