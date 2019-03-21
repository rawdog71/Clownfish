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

import io.clownfish.clownfish.daointerface.CfUserDAO;
import io.clownfish.clownfish.dbentities.CfUser;
import io.clownfish.clownfish.serviceinterface.CfUserService;
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
public class CfUserServiceImpl implements CfUserService {
    private final CfUserDAO cfuserDAO;
    
    @Autowired
    public CfUserServiceImpl(CfUserDAO cfuserDAO) {
        this.cfuserDAO = cfuserDAO;
    }
    
    @Override
    public CfUser findById(Long id) {
        return this.cfuserDAO.findById(id);
    }

    @Override
    public CfUser findByEmail(String email) {
        return this.cfuserDAO.findByEmail(email);
    }

    @Override
    public List<CfUser> findAll() {
        return this.cfuserDAO.findAll();
    }

    @Override
    public boolean create(CfUser entity) {
        return this.cfuserDAO.create(entity);
    }

    @Override
    public boolean delete(CfUser entity) {
        return this.cfuserDAO.delete(entity);
    }

    @Override
    public boolean edit(CfUser entity) {
        return this.cfuserDAO.edit(entity);
    }
    
}
