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
import ru.olegcherednik.zip4jvm.engine.UnzipEngine;
import ru.olegcherednik.zip4jvm.io.in.buf.DiskByteArrayDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputLocation;
import ru.olegcherednik.zip4jvm.io.in.file.DataInputFile;
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
    private long relativeOffs;
    private long absoluteOffs;
    private int diskNo;
    private String fileName;
    private SrcZip srcZip;

    public <T> T calcSize(DataInput in, LocalSupplier<T> task) {
        if (in instanceof DataInputLocation)
            return calcSize((DataInputLocation) in, task);

        absoluteOffs = in.getAbsoluteOffs();
        relativeOffs = in.getAbsoluteOffs();

        if (in instanceof DiskByteArrayDataInput) {
            SrcZip.Disk disk = ((DiskByteArrayDataInput) in).getDisk();
            diskNo = disk.getNo();
            fileName = disk.getFileName();
        }

        try {

            return task.get();
        } finally {
            calcSize(in);
        }
    }

    public <T> T calcSize(DataInputFile in, LocalSupplier<T> task) {
        return calcSize((DataInputLocation) in, task);
    }

    public <T> T calcSize(DataInputLocation dataInputLocation, LocalSupplier<T> task) {
        try {
            absoluteOffs = dataInputLocation.getAbsoluteOffs();
            relativeOffs = dataInputLocation.getDiskRelativeOffs();
            diskNo = dataInputLocation.getDisk().getNo();
            fileName = dataInputLocation.getDisk().getFileName();
            srcZip = dataInputLocation.getSrcZip();
            return task.get();
        } finally {
            calcSize(dataInputLocation);
        }
    }

    public void calcSize(DataInput in) {
        size = in.getAbsoluteOffs() - absoluteOffs;
    }

    @Deprecated
    public void calcSize(DataInputLocation in) {
        size = in.getAbsoluteOffs() - absoluteOffs;
    }

    public byte[] getData() {
        if (size > Integer.MAX_VALUE)
            return ArrayUtils.EMPTY_BYTE_ARRAY;

        try (DataInputFile in = UnzipEngine.createDataInput(srcZip)) {
            in.seek(diskNo, relativeOffs);
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
        return String.format("offs: %d, size: %s, disk: %d", relativeOffs, size, diskNo);
    }
}
