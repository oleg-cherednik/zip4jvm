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
package ru.olegcherednik.zip4jvm.assertj;

import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.temporaryFile;

/**
 * @author Oleg Cherednik
 * @since 03.10.2019
 */
@SuppressWarnings({ "MethodCanBeVariableArityMethod", "unused" })
class ZipFileSplitDecorator extends ZipFileDecorator {

    private final char[] password;

    public ZipFileSplitDecorator(Path zip) {
        this(zip, null);
    }

    public ZipFileSplitDecorator(Path zip, char[] password) {
        super(zip, entries(zip));
        this.password = ArrayUtils.clone(password);
    }

    @Override
    public InputStream getInputStream(ZipEntry entry) {
        try {
            Path tmp = temporaryFile(FilenameUtils.getExtension(entry.getName()));
            ZipFile zipFile = new ZipFile(zip.toFile(), password);
            zipFile.extractFile(entry.getName(), tmp.getParent().toString(), tmp.getFileName().toString());

            return new FileInputStream(tmp.toFile());
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    private static Map<String, ZipEntry> entries(Path path) {
        try {
            return new ZipFile(path.toFile()).getFileHeaders().stream()
                                             .map(fileHeader -> {
                                                 ZipEntry zipEntry = new ZipEntry(fileHeader.getFileName());
                                                 zipEntry.setSize(fileHeader.getUncompressedSize());
                                                 zipEntry.setCompressedSize(fileHeader.getCompressedSize());
                                                 zipEntry.setCrc(fileHeader.getCrc());
                                                 return zipEntry;
                                             })
                                             .collect(Collectors.toMap(ZipEntry::getName, Function.identity()));
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

}
