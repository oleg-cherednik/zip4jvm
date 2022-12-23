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
package ru.olegcherednik.zip4jvm.view.centraldirectory;

import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.CompressionMethodView;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 14.10.2019
 */
public final class CentralDirectoryView extends BaseView {

    private final CentralDirectory centralDirectory;
    private final Zip64.ExtensibleDataSector extensibleDataSector;
    private final Block block;

    public CentralDirectoryView(CentralDirectory centralDirectory,
                                Zip64.ExtensibleDataSector extensibleDataSector,
                                Block block,
                                int offs,
                                int columnWidth,
                                long totalDisks) {
        super(offs, columnWidth, totalDisks);
        this.centralDirectory = requireNotNull(centralDirectory, "CentralDirectoryView.centralDirectory");
        this.extensibleDataSector = extensibleDataSector;
        this.block = requireNotNull(block, "CentralDirectoryView.block");
    }

    @Override
    public boolean print(PrintStream out) {
        printTitle(out, CentralDirectory.FileHeader.SIGNATURE, "Central directory", block);
        printTotalEntries(out);

        if (extensibleDataSector != Zip64.ExtensibleDataSector.NULL) {
            printCompressionMethod(out);
            printEncryptionAlgorithm(out);
        }

        return true;
    }

    private void printTotalEntries(PrintStream out) {
        printLine(out, "total entries:", String.valueOf(centralDirectory.getFileHeaders().size()));
    }

    private void printCompressionMethod(PrintStream out) {
        CompressionMethod compressionMethod = extensibleDataSector.getCompressionMethod();
        new CompressionMethodView(compressionMethod, offs, columnWidth).print(out);
    }

    private void printEncryptionAlgorithm(PrintStream out) {
        int code = extensibleDataSector.getEncryptionAlgorithmCode();
        EncryptionAlgorithm encryptionAlgorithm = extensibleDataSector.getEncryptionAlgorithm();
        printLine(out,
                  String.format("encryption algorithm (0x%04X):", code),
                  String.format("%s %d bits", encryptionAlgorithm.getTitle(), extensibleDataSector.getBitLength()));
    }

}
