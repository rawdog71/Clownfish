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

import io.clownfish.clownfish.dbentities.CfSiteassetlist;
import java.util.List;

/**
 *
 * @author sulzbachr
 */
public interface CfSiteassetlistDAO {
    List<CfSiteassetlist> findAll();
    List<CfSiteassetlist> findBySiteref(Long ref);
    List<CfSiteassetlist> findByAssetlistref(Long ref);
    boolean create(CfSiteassetlist entity);
    boolean delete(CfSiteassetlist entity);
    boolean edit(CfSiteassetlist entity);
}
