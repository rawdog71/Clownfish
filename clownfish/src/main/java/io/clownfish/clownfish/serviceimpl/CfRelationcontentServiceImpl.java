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

import io.clownfish.clownfish.daointerface.CfRelationcontentDAO;
import io.clownfish.clownfish.dbentities.CfRelation;
import io.clownfish.clownfish.dbentities.CfRelationcontent;
import io.clownfish.clownfish.serviceinterface.CfRelationcontentService;
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
public class CfRelationcontentServiceImpl implements CfRelationcontentService {
    private final CfRelationcontentDAO cfrelationcontentDAO;
    
    @Autowired
    public CfRelationcontentServiceImpl(CfRelationcontentDAO cfrelationcontentDAO) {
        this.cfrelationcontentDAO = cfrelationcontentDAO;
    }
    
    @Override
    public boolean create(CfRelationcontent entity) {
        return this.cfrelationcontentDAO.create(entity);
    }

    @Override
    public boolean delete(CfRelationcontent entity) {
        return this.cfrelationcontentDAO.delete(entity);
    }

    @Override
    public boolean edit(CfRelationcontent entity) {
        return this.cfrelationcontentDAO.edit(entity);
    }    

    @Override
    public List<CfRelationcontent> findAll() {
        return this.cfrelationcontentDAO.findAll();
    }

    @Override
    public CfRelationcontent findById(Long id) {
        return this.cfrelationcontentDAO.findById(id);
    }

    @Override
    public CfRelationcontent findByRelationref(Long ref) {
        return this.cfrelationcontentDAO.findByRelationref(ref);
    }

    @Override
    public CfRelationcontent findByRefcontent1(Long refcontent) {
        return this.cfrelationcontentDAO.findByRefcontent1(refcontent);
    }

    @Override
    public CfRelationcontent findByRefcontent2(Long refcontent) {
        return this.cfrelationcontentDAO.findByRefcontent2(refcontent);
    }

}
