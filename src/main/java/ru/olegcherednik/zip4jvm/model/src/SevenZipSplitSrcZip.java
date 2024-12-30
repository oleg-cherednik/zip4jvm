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

import ru.olegcherednik.zip4jvm.exception.SplitPartNotFoundException;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 7-Zip has not standard split algorithm. It creates the whole zip file first and then split it with required part
 * size. It has following naming
 * convention:
 * <pre>
 * filename.zip.001
 * filename.zip.002
 * filename.zip.003
 * </pre>
 * According to the zip specification, this is not a split archive.
 *
 * @author Oleg Cherednik
 * @since 20.01.2020
 */
final class SevenZipSplitSrcZip extends SrcZip {

    public static boolean isCandidate(Path zip) {
        String ext = FilenameUtils.getExtension(zip.toString());
        return Files.isReadable(zip) && NumberUtils.isDigits(ext);
    }

    public static SevenZipSplitSrcZip create(Path zip) {
        return new SevenZipSplitSrcZip(zip, createDisks(zip));
    }

    private static List<Disk> createDisks(Path zip) {
        int i = 0;
        long absoluteOffs = 0;
        List<Disk> disks = new LinkedList<>();
        Path dir = zip.getParent();
        String baseName = FilenameUtils.getBaseName(zip.getFileName().toString());
        Set<Path> diskPaths = getDiskPaths(dir, baseName + "\\.\\d+");

        for (Path diskPath : diskPaths) {
            String actualFileName = diskPath.getFileName().toString();
            String expectedFileName = String.format("%s.%03d", baseName, i + 1);

            if (!actualFileName.equals(expectedFileName) || !Files.isReadable(diskPath))
                throw new SplitPartNotFoundException(dir.resolve(expectedFileName));

            Disk disk = Disk.builder()
                            .no(i)
                            .path(diskPath)
                            .absOffs(absoluteOffs)
                            .size(PathUtils.size(diskPath))
                            .last(i + 1 == diskPaths.size()).build();

            disks.add(disk);
            absoluteOffs += disk.getSize();
            i++;
        }

        return disks;
    }

    private SevenZipSplitSrcZip(Path zip, List<Disk> disks) {
        super(zip, disks);
    }

}
