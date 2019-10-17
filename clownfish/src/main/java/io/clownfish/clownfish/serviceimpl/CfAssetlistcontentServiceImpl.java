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

import io.clownfish.clownfish.daointerface.CfAssetlistcontentDAO;
import io.clownfish.clownfish.dbentities.CfAssetlistcontent;
import io.clownfish.clownfish.serviceinterface.CfAssetlistcontentService;
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
public class CfAssetlistcontentServiceImpl implements CfAssetlistcontentService {
    private final CfAssetlistcontentDAO cfassetlistcontentDAO;
    
    @Autowired
    public CfAssetlistcontentServiceImpl(CfAssetlistcontentDAO cfassetlistcontentDAO) {
        this.cfassetlistcontentDAO = cfassetlistcontentDAO;
    }

    @Override
    public List<CfAssetlistcontent> findAll() {
        return cfassetlistcontentDAO.findAll();
    }

    @Override
    public List<CfAssetlistcontent> findByAssetlistref(long assetlistref) {
        return cfassetlistcontentDAO.findByAssetlistref(assetlistref);
    }

    @Override
    public List<CfAssetlistcontent> findByAssetref(long assetref) {
        return cfassetlistcontentDAO.findByAssetref(assetref);
    }

    @Override
    public boolean create(CfAssetlistcontent entity) {
        return cfassetlistcontentDAO.create(entity);
    }

    @Override
    public boolean delete(CfAssetlistcontent entity) {
        return cfassetlistcontentDAO.delete(entity);
    }

    @Override
    public boolean edit(CfAssetlistcontent entity) {
        return cfassetlistcontentDAO.edit(entity);
    }

}
