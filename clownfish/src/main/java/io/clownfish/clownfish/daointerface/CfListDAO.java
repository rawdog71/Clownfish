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
package io.clownfish.clownfish.daointerface;

import io.clownfish.clownfish.dbentities.CfClass;
import io.clownfish.clownfish.dbentities.CfList;
import java.math.BigInteger;
import java.util.List;

/**
 *
 * @author sulzbachr
 */
public interface CfListDAO {
    List<CfList> findAll();
    CfList findById(Long id);
    CfList findByName(String name);
    List<CfList> findByClassref(CfClass id);
    List<CfList> findNotInList(BigInteger ref);
    CfList create(CfList entity);
    boolean delete(CfList entity);
    CfList edit(CfList entity);
    List<CfList> findByMaintenance(boolean b);
    CfList findByClassrefAndName(CfClass ref, String name);

    public CfList findByNameNotInList(String name, BigInteger ref);
}
