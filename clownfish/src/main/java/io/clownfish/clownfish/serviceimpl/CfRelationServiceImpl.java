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

import io.clownfish.clownfish.daointerface.CfRelationDAO;
import io.clownfish.clownfish.dbentities.CfRelation;
import io.clownfish.clownfish.serviceinterface.CfRelationService;
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
public class CfRelationServiceImpl implements CfRelationService {
    private final CfRelationDAO cfrelationDAO;
    
    @Autowired
    public CfRelationServiceImpl(CfRelationDAO cfrelationDAO) {
        this.cfrelationDAO = cfrelationDAO;
    }
    
    @Override
    public boolean create(CfRelation entity) {
        return this.cfrelationDAO.create(entity);
    }

    @Override
    public boolean delete(CfRelation entity) {
        return this.cfrelationDAO.delete(entity);
    }

    @Override
    public boolean edit(CfRelation entity) {
        return this.cfrelationDAO.edit(entity);
    }    

    @Override
    public List<CfRelation> findAll() {
        return this.cfrelationDAO.findAll();
    }

    @Override
    public CfRelation findById(Long id) {
        return this.cfrelationDAO.findById(id);
    }

    @Override
    public CfRelation findByRef1(Long ref) {
        return this.cfrelationDAO.findByRef1(ref);
    }

    @Override
    public CfRelation findByRef2(Long ref) {
        return this.cfrelationDAO.findByRef2(ref);
    }
}
