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

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.model.Charsets;

import javax.imageio.ImageIO;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
public class ZipEntryRegularFileAssert extends AbstractZipEntryAssert<ZipEntryRegularFileAssert> implements IRegularFileAssert<ZipEntryRegularFileAssert> {

    private static final Pattern NEW_LINE = Pattern.compile("\\r?\\n");

    public ZipEntryRegularFileAssert(ZipArchiveEntry actual, ZipFileDecorator zipFile) {
        super(actual, ZipEntryRegularFileAssert.class, zipFile);
    }

    @Override
    public ZipEntryRegularFileAssert hasSize(long size) {
        if (actual.getSize() == -1) {
            try (InputStream in = zipFile.getInputStream(actual)) {
                byte[] buf = new byte[1024 * 4];
                int available = 0;
                int res;

                while ((res = in.read(buf)) != IOUtils.EOF)
                    available += res;

                actual.setSize(available);
            } catch (Exception e) {
                assertThatThrownBy(() -> {
                    throw e;
                }).doesNotThrowAnyException();
            }
        }

        assertThat(actual.getSize()).isEqualTo(size);
        return myself;
    }

    @Override
    public ZipEntryRegularFileAssert isImage() {
        try (InputStream in = zipFile.getInputStream(actual)) {
            assertThat(ImageIO.read(in)).isNotNull();
        } catch (Exception e) {
            assertThatThrownBy(() -> {
                throw e;
            }).doesNotThrowAnyException();
        }

        return myself;
    }

    public ZipEntryRegularFileAssert hasContent(String expected) {
        try (InputStream in = zipFile.getInputStream(actual)) {
            String[] expectedLines = expected.isEmpty() ? ArrayUtils.EMPTY_STRING_ARRAY : NEW_LINE.split(expected);

            List<String> lines = IOUtils.readLines(in, Charsets.UTF_8);
            assertThat(lines).hasSize(expectedLines.length);

            int i = 0;

            for (String line : lines)
                assertThat(line).isEqualTo(expectedLines[i++]);
        } catch (Exception e) {
            assertThatThrownBy(() -> {
                throw e;
            }).doesNotThrowAnyException();
        }

        return myself;
    }

    public ZipEntryRegularFileAssert hasComment(String comment) {
        if (comment == null)
            assertThat(actual.getComment()).isNull();
        else
            assertThat(actual.getComment()).isEqualTo(comment);
        return myself;
    }

    @Override
    public ZipEntryRegularFileAssert matches(Consumer<IRegularFileAssert<?>> consumer) {
        consumer.accept(this);
        return myself;
    }
}
