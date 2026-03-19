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
package ru.olegcherednik.zip4jvm.view.zip64;

import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Flag;
import ru.olegcherednik.zip4jvm.crypto.strong.HashAlgorithm;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.CompressionView;
import ru.olegcherednik.zip4jvm.view.SizeView;
import ru.olegcherednik.zip4jvm.view.out.Out;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Optional;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 10.12.2022
 */
public final class ExtensibleDataSectorView extends BaseView {

    private final Zip64.ExtensibleDataSector extensibleDataSector;
    private final Block block;

    public ExtensibleDataSectorView(Zip64.ExtensibleDataSector extensibleDataSector,
                                    Block block,
                                    int offs,
                                    int columnWidth) {
        super(offs, columnWidth);
        this.extensibleDataSector = requireNotNull(extensibleDataSector,
                                                   "ExtensibleDataSectorView.extensibleDataSector");
        this.block = requireNotNull(block, "ExtensibleDataSectorView.block");
    }

    @Override
    public boolean printTextInfo(Out out) {
        printTitle(out, "ZIP64 Extensible data sector", block);
        printCompressionMethod(out);
        printSize(out);
        printEncryptionAlgorithm(out);
        printBitLength(out);
        printFlags(out);
        printHashAlgorithm(out);
        printHashData(out);
        return true;
    }

    private void printCompressionMethod(Out out) {
        Compression compression = extensibleDataSector.getCompression();
        new CompressionView(compression, offs, columnWidth).printTextInfo(out);
    }

    private void printSize(Out out) {
        new SizeView("compressed size:",
                     extensibleDataSector.getCompressedSize(),
                     offs,
                     columnWidth).printTextInfo(out);
        new SizeView("uncompressed size:", extensibleDataSector.getUncompressedSize(), offs, columnWidth).printTextInfo(
                out);
    }

    private void printEncryptionAlgorithm(Out out) {
        int code = extensibleDataSector.getEncryptionAlgorithmCode();
        EncryptionAlgorithm encryptionAlgorithm = extensibleDataSector.getEncryptionAlgorithm();
        printLine(out, String.format("encryption algorithm (0x%04X):", code), encryptionAlgorithm.getTitle());
    }

    private void printBitLength(Out out) {
        printLine(out, "encryption key bits:", extensibleDataSector.getBitLength());
    }

    private void printFlags(Out out) {
        Flag flags = extensibleDataSector.getFlags();
        printLine(out, String.format("flags (0x%02X):", flags.getCode()), flags.getTitle());
    }

    private void printHashAlgorithm(Out out) {
        int code = extensibleDataSector.getHashAlgorithmCode();
        HashAlgorithm hashAlgorithm = extensibleDataSector.getHashAlgorithm();
        printLine(out, String.format("hash algorithm (0x%04X):", code), hashAlgorithm.getTitle());
    }

    private void printHashData(Out out) {
        byte[] hashData = Optional.ofNullable(extensibleDataSector.getHashData()).orElse(ArrayUtils.EMPTY_BYTE_ARRAY);
        printSize(out, "hashData:", hashData.length);
        new ByteArrayHexView(hashData, offs + 2, columnWidth).printTextInfo(out);
    }

}
