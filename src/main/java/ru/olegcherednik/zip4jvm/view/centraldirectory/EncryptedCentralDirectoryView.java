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
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.CompressionMethodView;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 01.01.2023
 */
public final class EncryptedCentralDirectoryView extends CentralDirectoryView {

    private final Zip64.ExtensibleDataSector extensibleDataSector;

    public EncryptedCentralDirectoryView(CentralDirectory centralDirectory,
                                         Zip64.ExtensibleDataSector extensibleDataSector,
                                         Block block,
                                         int offs,
                                         int columnWidth,
                                         long totalDisks) {
        super(centralDirectory, block, offs, columnWidth, totalDisks);
        this.extensibleDataSector = extensibleDataSector;
    }

    @Override
    public boolean printTextInfo(PrintStream out) {
        super.printTextInfo(out);

        if (extensibleDataSector != null) {
            new CompressionMethodView(extensibleDataSector.getCompressionMethod(),
                                      offs,
                                      columnWidth).printTextInfo(out);
            printEncryptionAlgorithm(out);
        }

        return true;
    }

    private void printEncryptionAlgorithm(PrintStream out) {
        int code = extensibleDataSector.getEncryptionAlgorithmCode();
        EncryptionAlgorithm encryptionAlgorithm = extensibleDataSector.getEncryptionAlgorithm();
        printLine(out,
                  String.format("encryption algorithm (0x%04X):", code),
                  String.format("%s", encryptionAlgorithm.getTitle()));
    }

}
