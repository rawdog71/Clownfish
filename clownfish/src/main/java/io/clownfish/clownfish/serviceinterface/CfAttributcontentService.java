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
package io.clownfish.clownfish.serviceinterface;

import io.clownfish.clownfish.dbentities.CfAssetlist;
import io.clownfish.clownfish.dbentities.CfAttribut;
import io.clownfish.clownfish.dbentities.CfAttributcontent;
import io.clownfish.clownfish.dbentities.CfClasscontent;
import io.clownfish.clownfish.dbentities.CfList;
import java.util.List;

/**
 *
 * @author sulzbachr
 */
public interface CfAttributcontentService {
    List<CfAttributcontent> findAll();
    List<CfAttributcontent> findByClasscontentref(CfClasscontent classcontentref);
    CfAttributcontent findByAttributrefAndClasscontentref(CfAttribut attributref, CfClasscontent classcontentref);
    List<CfAttributcontent> findByAttributref(CfAttribut attributref);
    List<CfAttributcontent> findByIndexed(boolean indexed);
    CfAttributcontent create(CfAttributcontent entity);
    boolean delete(CfAttributcontent entity);
    boolean delete(long classcontentref);
    boolean deleteAttributref(long attributref);
    boolean updateContentref(long contentref);
    CfAttributcontent edit(CfAttributcontent entity);
    List<CfAttributcontent> findByContentclassRef(CfList classcontentref);
    List<CfAttributcontent> findByContentAssetRef(CfAssetlist classcontentref);
}
