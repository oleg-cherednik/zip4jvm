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

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * @author Oleg Cherednik
 * @since 02.05.2019
 */
class ZipFileEncryptedDecoder extends ZipFileDecorator {

    private final String password;

    ZipFileEncryptedDecoder(Path zipFile, char[] password) {
        super(zipFile, entries(zipFile));
        this.password = password == null ? null : String.valueOf(password);
    }

    @Override
    @SuppressWarnings("PMD.ExceptionAsFlowControl")
    public InputStream getInputStream(ZipEntry entry) {
        try (IInStream in = new RandomAccessFileInStream(new RandomAccessFile(zip.toFile(), "r"));
             IInArchive zip = SevenZip.openInArchive(ArchiveFormat.ZIP, in)) {

            for (ISimpleInArchiveItem item : zip.getSimpleInterface().getArchiveItems())
                if (getItemName(item).equals(entry.getName()))
                    return getInputStream(item);

            throw new Zip4jvmException("Entry '" + entry + "' was not found");
        } catch (Zip4jvmException e) {
            throw e;
        } catch (Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    private static String getItemName(ISimpleInArchiveItem item) {
        return Quietly.doRuntime(() -> {
            String name = FilenameUtils.normalize(item.getPath(), true);
            return item.isFolder() ? name + '/' : name;
        });
    }

    @Override
    public String getComment() {
        // supports only ASCII symbols
        try (IInStream in = new RandomAccessFileInStream(new RandomAccessFile(zip.toFile(), "r"));
             IInArchive zip = SevenZip.openInArchive(ArchiveFormat.ZIP, in)) {
            String str = zip.getStringArchiveProperty(PropID.COMMENT);
            return StringUtils.length(str) == 0 ? null : str;
        } catch (Zip4jvmException e) {
            throw e;
        } catch (Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    private InputStream getInputStream(ISimpleInArchiveItem item) throws SevenZipException {
        List<byte[]> tmp = new ArrayList<>();

        if (item.getSize() == 0)
            tmp.add(ArrayUtils.EMPTY_BYTE_ARRAY);
        else {
            ExtractOperationResult res = item.extractSlow(data -> {
                tmp.add(data);
                return ArrayUtils.getLength(data);
            }, password);

            if (tmp.isEmpty() || res != ExtractOperationResult.OK)
                throw new Zip4jvmException("Cannot extract zip entry: " + res);
        }

        int size = tmp.stream().mapToInt(buf -> buf.length).sum();
        byte[] buf = new byte[size];
        int offs = 0;

        for (byte[] data : tmp) {
            System.arraycopy(data, 0, buf, offs, data.length);
            offs += data.length;
        }

        return new ByteArrayInputStream(buf);
    }

    private static Map<String, ZipArchiveEntry> entries(Path path) {
        try (IInStream in = new RandomAccessFileInStream(new RandomAccessFile(path.toFile(), "r"));
             IInArchive zip = SevenZip.openInArchive(ArchiveFormat.ZIP, in)) {

            return Arrays.stream(zip.getSimpleInterface().getArchiveItems())
                         .map(ZipFileEncryptedDecoder::getItemName)
                         .map(ZipArchiveEntry::new)
                         .collect(Collectors.toMap(ZipArchiveEntry::getName, Function.identity()));
        } catch (Zip4jvmException e) {
            throw e;
        } catch (Exception e) {
            throw new Zip4jvmException(e);
        }
    }
}
