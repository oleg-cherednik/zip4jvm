/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.model.src;

import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
final class SolidSrcZip extends SrcZip {

    public static SolidSrcZip create(Path zip) {
        return new SolidSrcZip(zip);
    }

    private SolidSrcZip(Path zip) {
        super(zip, createDisks(zip));
    }

    private static List<Disk> createDisks(Path zip) {
        Disk disk = Disk.builder()
                        .no(0)
                        .path(zip)
                        .absOffs(0)
                        .size(PathUtils.size(zip))
                        .last(true).build();

        return Collections.singletonList(disk);
    }

    @Override
    public String toString() {
        return path.toString();
    }

}
