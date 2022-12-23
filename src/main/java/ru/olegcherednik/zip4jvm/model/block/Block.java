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

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.ZipDataInput;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.function.LocalSupplier;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 19.10.2019
 */
@Getter
public class Block {

    public static final Block NULL = new Block();

    private long size;
    private long relativeOffs;
    private long absoluteOffs;
    private int diskNo;
    private String fileName;
    private SrcZip srcZip;

    public <T> T calcSize(DataInput in, LocalSupplier<T> task) throws IOException {
        try {
            absoluteOffs = in.getAbsoluteOffs();
            relativeOffs = in.getDiskRelativeOffs();
            diskNo = in.getDisk().getNo();
            fileName = in.getDisk().getFileName();
            srcZip = in.getSrcZip();
            return task.get();
        } finally {
            calcSize(in);
        }
    }

    public void calcSize(DataInput in) {
        size = in.getAbsoluteOffs() - absoluteOffs;
    }

    public byte[] getData() {
        if (size > Integer.MAX_VALUE)
            return ArrayUtils.EMPTY_BYTE_ARRAY;

        try (DataInput in = new ZipDataInput(srcZip)) {
            in.seek(diskNo, relativeOffs);
            return in.readBytes((int)size);
        } catch(Exception e) {
            e.printStackTrace();
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
    }

    @Override
    public String toString() {
        return String.format("offs: %d, size: %s, disk: %d", relativeOffs, size, diskNo);
    }
}
