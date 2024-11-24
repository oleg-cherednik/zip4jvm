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
package ru.olegcherednik.zip4jvm.io.out.file;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.out.OffsOutputStream;
import ru.olegcherednik.zip4jvm.io.out.MarkerDataOutput;
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
public class SplitZipDataOutput extends MarkerDataOutput {

    /** see 8.5.5 */
    public static final int SPLIT_SIGNATURE = DataDescriptor.SIGNATURE;

    protected final ZipModel zipModel;
    private OffsOutputStream out;
    private int diskNo;

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public SplitZipDataOutput(ZipModel zipModel) throws IOException {
        this.zipModel = zipModel;
        out = OffsOutputStream.create(zipModel.getSrcZip().getPath());
        ValidationUtils.requireZeroOrPositive(zipModel.getSplitSize(), "zipModel.splitSize");
        writeDwordSignature(SPLIT_SIGNATURE);
    }

    private void doNotSplitSignature(int len) throws IOException {
        long available = zipModel.getSplitSize() - getDiskOffs();

        if (available <= len)
            openNextDisk();
    }

    private void openNextDisk() throws IOException {
        out.close();

        SrcZip srcZip = zipModel.getSrcZip();
        Path file = srcZip.getPath();
        Path diskPath = srcZip.getDiskPath(++diskNo);

        // TODO #34 - Validate all new create split disks are not exist
        if (Files.exists(diskPath))
            throw new IOException("split file: " + diskPath.getFileName()
                                          + " already exists in the current directory, cannot rename this file");

        if (!file.toFile().renameTo(diskPath.toFile()))
            throw new IOException("cannot rename newly created split file");

        out = OffsOutputStream.create(file);
    }

    // ---------- DataOutput ----------

    @Override
    public final ByteOrder getByteOrder() {
        return zipModel.getByteOrder();
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

    @Override
    public void writeByte(int val) throws IOException {
        zipModel.getByteOrder().writeByte(val, this);
    }

    @Override
    public void writeWord(int val) throws IOException {
        zipModel.getByteOrder().writeWord(val, this);
    }

    @Override
    public void writeDword(long val) throws IOException {
        zipModel.getByteOrder().writeDword(val, this);
    }

    @Override
    public void writeQword(long val) throws IOException {
        zipModel.getByteOrder().writeQword(val, this);
    }

    @Override
    public long getDiskOffs() {
        return out.getOffs();
    }

    // ---------- Flushable ----------

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    // ---------- OutputStream ----------

    @Override
    public void write(int b) throws IOException {
        if (zipModel.getSplitSize() - getDiskOffs() <= 0)
            openNextDisk();

        out.write(b);
        super.write(b);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        new ZipModelWriter(zipModel).write(this);
        out.close();
    }

    // ---------- Object ----------

    @Override
    public final String toString() {
        return out + "; disk: " + diskNo;
    }

}
