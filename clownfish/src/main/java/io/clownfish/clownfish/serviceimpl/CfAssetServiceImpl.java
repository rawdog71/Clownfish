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

import io.clownfish.clownfish.daointerface.CfAssetDAO;
import io.clownfish.clownfish.dbentities.CfAsset;
import io.clownfish.clownfish.serviceinterface.CfAssetService;
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
public class CfAssetServiceImpl implements CfAssetService {
    private final CfAssetDAO cfassetDAO;
    
    @Autowired
    public CfAssetServiceImpl(CfAssetDAO cfassetDAO) {
        this.cfassetDAO = cfassetDAO;
    }
    
    @Override
    public CfAsset create(CfAsset entity) {
        return this.cfassetDAO.create(entity);
    }

    @Override
    public boolean delete(CfAsset entity) {
        return this.cfassetDAO.delete(entity);
    }

    @Override
    public CfAsset edit(CfAsset entity) {
        return this.cfassetDAO.edit(entity);
    }    

    @Override
    public List<CfAsset> findAll() {
        return this.cfassetDAO.findAll();
    }

    @Override
    public CfAsset findById(Long id) {
        return this.cfassetDAO.findById(id);
    }

    @Override
    public CfAsset findByName(String name) {
        return this.cfassetDAO.findByName(name);
    }

    @Override
    public List<CfAsset> findByIndexed(boolean indexed) {
        return this.cfassetDAO.findByIndexed(indexed);
    }

    @Override
    public List<CfAsset> findByScrapped(boolean scrapped) {
        return this.cfassetDAO.findByScrapped(scrapped);
    }

    @Override
    public List<CfAsset> findByPublicuse(boolean publicuse) {
        return this.cfassetDAO.findByPublicuse(publicuse);
    }

    @Override
    public List<CfAsset> findByFilesize(long filesize) {
        return this.cfassetDAO.findByFilesize(filesize);
    }

    @Override
    public List<CfAsset> findByAvatars() {
        return this.cfassetDAO.findByAvatars();
    }

    @Override
    public List<CfAsset> findByPublicuseAndScrapped(boolean publicuse, boolean scrapped) {
        return this.cfassetDAO.findByPublicuseAndScrapped(publicuse, scrapped);
    }
}
