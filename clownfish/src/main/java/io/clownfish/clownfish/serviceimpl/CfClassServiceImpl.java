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

import io.clownfish.clownfish.daointerface.CfClassDAO;
import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.serviceinterface.CfClassService;
import java.math.BigInteger;
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
public class CfClassServiceImpl implements CfClassService {
    private final CfClassDAO cfclassDAO;
    
    @Autowired
    public CfClassServiceImpl(CfClassDAO cfclassDAO) {
        this.cfclassDAO = cfclassDAO;
    }
    
    @Override
    public CfClass findById(Long id) {
        return this.cfclassDAO.findById(id);
    }

    @Override
    public List<CfClass> findAll() {
        return this.cfclassDAO.findAll();
    }

    @Override
    public CfClass create(CfClass entity) {
        return this.cfclassDAO.create(entity);
    }

    @Override
    public boolean delete(CfClass entity) {
        return this.cfclassDAO.delete(entity);
    }

    @Override
    public CfClass edit(CfClass entity) {
        return this.cfclassDAO.edit(entity);
    }

    @Override
    public CfClass findByName(String name) {
        return this.cfclassDAO.findByName(name);
    }

    @Override
    public List<CfClass> findNotInList(BigInteger ref) {
        return this.cfclassDAO.findNotInList(ref);
    }
}
