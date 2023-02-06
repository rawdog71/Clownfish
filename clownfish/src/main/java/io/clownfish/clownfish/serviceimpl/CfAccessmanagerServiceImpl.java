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

import io.clownfish.clownfish.daointerface.CfAccessmanagerDAO;
import io.clownfish.clownfish.dbentities.CfAccessmanager;
import io.clownfish.clownfish.serviceinterface.CfAccessmanagerService;
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
public class CfAccessmanagerServiceImpl implements CfAccessmanagerService {
    private final CfAccessmanagerDAO cfaccessmanagerDAO;
    
    @Autowired
    public CfAccessmanagerServiceImpl(CfAccessmanagerDAO cfaccessmanagerDAO) {
        this.cfaccessmanagerDAO = cfaccessmanagerDAO;
    }

    @Override
    public List<CfAccessmanager> findAll() {
        return this.cfaccessmanagerDAO.findAll();
    }

    @Override
    public CfAccessmanager findById(Long id) {
        return this.cfaccessmanagerDAO.findById(id);
    }

    @Override
    public List<CfAccessmanager> findByType(Integer type) {
        return this.cfaccessmanagerDAO.findByType(type);
    }

    @Override
    public List<CfAccessmanager> findByRef(BigInteger ref) {
        return this.cfaccessmanagerDAO.findByRef(ref);
    }

    @Override
    public List<CfAccessmanager> findByRefclasscontent(BigInteger refclasscontent) {
        return this.cfaccessmanagerDAO.findByRefclasscontent(refclasscontent);
    }

    @Override
    public CfAccessmanager create(CfAccessmanager entity) {
        return this.cfaccessmanagerDAO.create(entity);
    }

    @Override
    public boolean delete(CfAccessmanager entity) {
        return this.cfaccessmanagerDAO.delete(entity);
    }

    @Override
    public CfAccessmanager edit(CfAccessmanager entity) {
        return this.cfaccessmanagerDAO.edit(entity);
    }

    @Override
    public CfAccessmanager findByTypeAndRefAndRefclasscontent(Integer type, BigInteger ref, BigInteger refclasscontent) {
        return this.cfaccessmanagerDAO.findByTypeAndRefAndRefclasscontent(type, ref, refclasscontent);
    }

    @Override
    public List<CfAccessmanager> findByTypeAndRef(Integer type, BigInteger ref) {
        return this.cfaccessmanagerDAO.findByTypeAndRef(type, ref);
    }
}
