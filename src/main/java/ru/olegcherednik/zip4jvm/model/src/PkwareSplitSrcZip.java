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

import org.apache.commons.io.FilenameUtils;
import ru.olegcherednik.zip4jvm.exception.SplitPartNotFoundException;
import ru.olegcherednik.zip4jvm.io.readers.ZipModelReader;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Oleg Cherednik
 * @since 25.08.2020
 */
final class PkwareSplitSrcZip extends SrcZip {

    public static boolean isCandidate(Path zip) {
        return Files.isReadable(zip) && ZipModelReader.getTotalDisks(SolidSrcZip.create(zip)) > 0;
    }

    public static PkwareSplitSrcZip create(Path zip) {
        return new PkwareSplitSrcZip(zip, createDisks(zip));
    }

    private static List<Disk> createDisks(Path zip) {
        int diskNo = 0;
        long absoluteOffs = 0;
        List<Disk> disks = new LinkedList<>();
        Path dir = zip.getParent();
        String baseName = FilenameUtils.getBaseName(zip.getFileName().toString());
        int totalDisk = ZipModelReader.getTotalDisks(SolidSrcZip.create(zip));
        Set<Path> diskPaths = getDiskPaths(dir, baseName + "\\.(?:z\\d+|zip)");

        for (Path diskPath : diskPaths) {
            boolean last = diskNo + 1 == totalDisk;
            String actualFileName = diskPath.getFileName().toString();
            String expectedFileName = last ? baseName + ".zip" : String.format("%s.z%02d", baseName, diskNo + 1);

            if (!actualFileName.equals(expectedFileName) || !Files.isReadable(diskPath))
                throw new SplitPartNotFoundException(dir.resolve(expectedFileName));

            Disk disk = Disk.builder()
                            .no(diskNo)
                            .path(diskPath)
                            .absoluteOffs(absoluteOffs)
                            .size(PathUtils.size(diskPath))
                            .last(last).build();

            disks.add(disk);
            absoluteOffs += disk.getSize();
            diskNo++;
        }

        return disks;
    }

    private PkwareSplitSrcZip(Path zip, List<Disk> disks) {
        super(zip, disks);
    }

}
