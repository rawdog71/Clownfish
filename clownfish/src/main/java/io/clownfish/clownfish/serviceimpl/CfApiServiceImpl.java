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

import io.clownfish.clownfish.daointerface.CfApiDAO;
import io.clownfish.clownfish.dbentities.CfApi;
import io.clownfish.clownfish.serviceinterface.CfApiService;
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
public class CfApiServiceImpl implements CfApiService {
    private final CfApiDAO cfapiDAO;
    
    @Autowired
    public CfApiServiceImpl(CfApiDAO cfapiDAO) {
        this.cfapiDAO = cfapiDAO;
    }
    
    @Override
    public CfApi create(CfApi entity) {
        return this.cfapiDAO.create(entity);
    }

    @Override
    public boolean delete(CfApi entity) {
        return this.cfapiDAO.delete(entity);
    }

    @Override
    public CfApi edit(CfApi entity) {
        return this.cfapiDAO.edit(entity);
    }    

    @Override
    public List<CfApi> findAll() {
        return this.cfapiDAO.findAll();
    }

    @Override
    public List<CfApi> findBySiteRef(Long id) {
        return this.cfapiDAO.findBySiteRef(id);
    }

    @Override
    public List<CfApi> findByKeyname(String key) {
        return this.cfapiDAO.findByKeyname(key);
    }

    @Override
    public CfApi findBySiteRefAndKeyname(Long id, String keyname) {
        return this.cfapiDAO.findBySiteRefAndKeyname(id, keyname);
    }

}
