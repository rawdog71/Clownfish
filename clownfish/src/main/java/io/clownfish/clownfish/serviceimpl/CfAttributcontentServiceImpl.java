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

import io.clownfish.clownfish.daointerface.CfAttributcontentDAO;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.serviceinterface.CfAttributcontentService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author sulzbachr
 */
@Service
@Transactional
public class CfAttributcontentServiceImpl implements CfAttributcontentService {
    private final CfAttributcontentDAO cfattributcontentDAO;

    public CfAttributcontentServiceImpl(CfAttributcontentDAO cfattributcontentDAO) {
        this.cfattributcontentDAO = cfattributcontentDAO;
    }

    @Override
    public List<CfAttributcontent> findAll() {
        return this.cfattributcontentDAO.findAll();
    }

    @Override
    public boolean create(CfAttributcontent entity) {
        return this.cfattributcontentDAO.create(entity);
    }

    @Override
    public boolean delete(CfAttributcontent entity) {
        return this.cfattributcontentDAO.delete(entity);
    }

    @Override
    public boolean edit(CfAttributcontent entity) {
        return this.cfattributcontentDAO.edit(entity);
    }

}
