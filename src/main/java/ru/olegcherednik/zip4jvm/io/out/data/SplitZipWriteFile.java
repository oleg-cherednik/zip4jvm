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
package ru.olegcherednik.zip4jvm.io.out.data;

import ru.olegcherednik.zip4jvm.io.writers.ZipModelWriter;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
@Getter
public class SplitZipWriteFile extends WriteFileDataOutput {

    /** see 8.5.5 */
    public static final int SPLIT_SIGNATURE = DataDescriptor.SIGNATURE;

    protected final ZipModel zipModel;
    private int diskNo;

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public SplitZipWriteFile(ZipModel zipModel) throws IOException {
        this.zipModel = zipModel;
        createFile(zipModel.getSrcZip().getPath());
        ValidationUtils.requireZeroOrPositive(zipModel.getSplitSize(), "zipModel.splitSize");
        writeDwordSignature(SPLIT_SIGNATURE);
    }

    @Override
    public void writeWordSignature(int sig) throws IOException {
        doNotSplitSignature(2);
        super.writeWordSignature(sig);
    }

    @Override
    public void writeDwordSignature(int sig) throws IOException {
        doNotSplitSignature(4);
        super.writeDwordSignature(sig);
    }

    private void doNotSplitSignature(int len) throws IOException {
        long available = zipModel.getSplitSize() - getRelativeOffs();

        if (available <= len)
            openNextDisk();
    }

    @Override
    @SuppressWarnings("PMD.AvoidReassigningParameters")
    public void write(byte[] buf, int offs, int len) throws IOException {
        final int offsInit = offs;

        while (len > 0) {
            long available = zipModel.getSplitSize() - getRelativeOffs();

            if (available <= 0 || len > available && offsInit != offs)
                openNextDisk();

            available = zipModel.getSplitSize() - getRelativeOffs();

            int writeBytes = Math.min(len, (int) available);
            super.write(buf, offs, writeBytes);

            offs += writeBytes;
            len -= writeBytes;
        }
    }

    private void openNextDisk() throws IOException {
        super.close();

        SrcZip srcZip = zipModel.getSrcZip();
        Path path = srcZip.getPath();
        Path diskPath = srcZip.getDiskPath(++diskNo);

        // TODO #34 - Validate all new create split disks are not exist
        if (Files.exists(diskPath))
            throw new IOException("split file: " + diskPath.getFileName()
                                          + " already exists in the current directory, cannot rename this file");

        if (!path.toFile().renameTo(diskPath.toFile()))
            throw new IOException("cannot rename newly created split file");

        createFile(path);
    }

    @Override
    public void close() throws IOException {
        new ZipModelWriter(zipModel).write(this);
        super.close();
    }

    @Override
    public final String toString() {
        return super.toString() + "; disk: " + diskNo;
    }

}
