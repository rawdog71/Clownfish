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

import io.clownfish.clownfish.daointerface.CfPropertyDAO;
import io.clownfish.clownfish.dbentities.CfProperty;
import io.clownfish.clownfish.serviceinterface.CfPropertyService;
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
public class CfPropertyServiceImpl implements CfPropertyService {
    private final CfPropertyDAO cfpropertyDAO;
    
    @Autowired
    public CfPropertyServiceImpl(CfPropertyDAO cfpropertyDAO) {
        this.cfpropertyDAO = cfpropertyDAO;
    }

    @Override
    public List<CfProperty> findAll() {
        return this.cfpropertyDAO.findAll();
    }

    @Override
    public CfProperty findByHashkey(String hashkey) {
        return this.cfpropertyDAO.findByHashkey(hashkey);
    }

    @Override
    public CfProperty findByValue(String value) {
        return this.cfpropertyDAO.findByValue(value);
    }

    @Override
    public boolean create(CfProperty entity) {
        return this.cfpropertyDAO.create(entity);
    }

    @Override
    public boolean delete(CfProperty entity) {
        return this.cfpropertyDAO.delete(entity);
    }

    @Override
    public boolean edit(CfProperty entity) {
        return this.cfpropertyDAO.edit(entity);
    }
}
