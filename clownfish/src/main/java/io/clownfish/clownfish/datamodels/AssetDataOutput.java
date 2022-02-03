/*
 * Copyright 2020 SulzbachR.
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
package io.clownfish.clownfish.datamodels;

import io.clownfish.clownfish.dbentities.CfAsset;
import java.util.ArrayList;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author SulzbachR
 */
public class AssetDataOutput {
    private @Getter @Setter CfAsset asset;
    private @Getter @Setter ArrayList<String> keywords;

    @Override
    public boolean equals(Object object) {
        return object instanceof AssetDataOutput && ((AssetDataOutput) object).asset.getId().equals(asset.getId());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.asset);
        return hash;
    }
}
