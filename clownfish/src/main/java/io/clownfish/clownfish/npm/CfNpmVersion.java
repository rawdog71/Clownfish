/*
 * Copyright 2024 SulzbachR.
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
package io.clownfish.clownfish.npm;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author SulzbachR
 */
public class CfNpmVersion implements Comparable<CfNpmVersion> {
    private @Getter @Setter String name;
    private @Getter @Setter String version;
    private @Getter @Setter String tarball;
    private @Getter @Setter CfVersion splitversion;

    @Override
    public int compareTo(CfNpmVersion other) {
        this.makeVersion(this.version);
        other.makeVersion(other.version);
        
        int diff = this.splitversion.getMajor() - other.splitversion.getMajor();
        if (diff != 0) {
           return -diff;
        }
        if (this.splitversion.getMinor() == null && other.splitversion.getMinor() == null ) {
           return 0;
        }
        if (this.splitversion.getMinor() == null && other.splitversion.getMinor() != null ) {
           return -1;
        }
        if (this.splitversion.getMinor() != null && other.splitversion.getMinor() == null ) {
           return +1;
        }
        diff = this.splitversion.getMinor() - other.splitversion.getMinor();
        if (diff != 0) {
           return -diff;
        }
        if (this.splitversion.getPatch() == null && other.splitversion.getPatch() == null) {
           return 0;
        }
        if (this.splitversion.getPatch() == null && other.splitversion.getPatch() != null) {
           return -1;
        }
        if (this.splitversion.getPatch() != null && other.splitversion.getPatch() == null) {
           return +1;
        }
        diff = this.splitversion.getPatch() - other.splitversion.getPatch();
        return -diff;
    }
    
    private void makeVersion(String ver) {
        final String[] parts = ver.split("\\.");
        this.splitversion = new CfVersion();
        this.splitversion.setMajor(Integer.parseInt(parts[0]));
        if( parts.length > 1 ) {
           this.splitversion.setMinor(Integer.parseInt(parts[1]));
           if( parts.length > 2 ) {
              this.splitversion.setPatch(Integer.parseInt(parts[2]));
           }
        }
    }
}
