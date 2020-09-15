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

import io.clownfish.clownfish.daointerface.CfAssetlistDAO;
import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.serviceinterface.CfAssetlistService;
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
public class CfAssetlistServiceImpl implements CfAssetlistService {
    private final CfAssetlistDAO cfassetlistDAO;
    
    @Autowired
    public CfAssetlistServiceImpl(CfAssetlistDAO cfassetlistDAO) {
        this.cfassetlistDAO = cfassetlistDAO;
    }

    @Override
    public List<CfAssetlist> findAll() {
        return cfassetlistDAO.findAll();
    }

    @Override
    public CfAssetlist findById(Long id) {
        return cfassetlistDAO.findById(id);
    }

    @Override
    public CfAssetlist findByName(String name) {
        return cfassetlistDAO.findByName(name);
    }

    @Override
    public CfAssetlist create(CfAssetlist entity) {
        return cfassetlistDAO.create(entity);
    }

    @Override
    public boolean delete(CfAssetlist entity) {
        return cfassetlistDAO.delete(entity);
    }

    @Override
    public CfAssetlist edit(CfAssetlist entity) {
        return cfassetlistDAO.edit(entity);
    }

}
