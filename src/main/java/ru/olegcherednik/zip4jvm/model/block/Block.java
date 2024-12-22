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
package ru.olegcherednik.zip4jvm.model.block;

import ru.olegcherednik.zip4jvm.decompose.Utils;
import ru.olegcherednik.zip4jvm.engine.unzip.UnzipEngine;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.random.BaseRandomAccessDataInput;
import ru.olegcherednik.zip4jvm.io.in.file.random.RandomAccessDataInput;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.function.LocalSupplier;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 19.10.2019
 */
@Slf4j
@Getter
public class Block {

    public static final Block NULL = new Block();

    private long size;
    private long diskOffs;
    private long absOffs;
    private int diskNo;
    private String fileName;
    private SrcZip srcZip;

    public <T> T calcSize(BaseRandomAccessDataInput in, LocalSupplier<T> task) throws IOException {
        try {
            srcZip = in.getSrcZip();
            absOffs = in.getAbsOffs();

            SrcZip.Disk disk = srcZip.getDiskByAbsOffs(absOffs);

            diskOffs = absOffs - disk.getAbsOffs();
            diskNo = disk.getNo();
            fileName = disk.getFileName();

            return task.get();
        } finally {
            calcSize(in);
        }
    }

    public void calcSize(DataInput in) {
        size = in.getAbsOffs() - absOffs;
    }

    @Deprecated
    public void calcSize(BaseRandomAccessDataInput in) {
        size = in.getAbsOffs() - absOffs;
    }

    public byte[] getData() {
        if (size > Integer.MAX_VALUE)
            return ArrayUtils.EMPTY_BYTE_ARRAY;

        try (RandomAccessDataInput in = UnzipEngine.createRandomAccessDataInput(srcZip)) {
            in.seek(absOffs);
            return in.readBytes((int) size);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
    }

    public void copyLarge(ZipModel zipModel, Path out) throws IOException {
        Utils.copyLarge(zipModel, out, this);
    }

    @Override
    public String toString() {
        return String.format("offs: %d, size: %s, disk: %d", diskOffs, size, diskNo);
    }
}
