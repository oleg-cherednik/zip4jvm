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

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.AbstractPathAssert;

import java.io.BufferedReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatStringLine;

/**
 * @author Oleg Cherednik
 * @since 28.03.2019
 */
public class RegularFileAssert extends AbstractPathAssert<RegularFileAssert>
        implements IRegularFileAssert<RegularFileAssert> {

    private static final Pattern REGEX = Pattern.compile("<--\\sregexp\\((?<regex>.+)\\)\\s-->.+");

    public RegularFileAssert(Path actual) {
        super(actual, RegularFileAssert.class);
    }

    @Override
    public RegularFileAssert isImage() {
        try (InputStream in = Files.newInputStream(actual)) {
            assertThat(ImageIO.read(in)).isNotNull();
        } catch (Exception e) {
            assertThatThrownBy(() -> {
                throw e;
            }).doesNotThrowAnyException();
        }

        return myself;
    }

    @Override
    public RegularFileAssert hasSize(long size) {
        try {
            assertThat(Files.size(actual)).isEqualTo(size);
        } catch (Exception e) {
            assertThatThrownBy(() -> {
                throw e;
            }).doesNotThrowAnyException();
        }

        return myself;
    }

    @Override
    public RegularFileAssert matches(Consumer<IRegularFileAssert<?>> consumer) {
        consumer.accept(this);
        return myself;
    }

    @Override
    public RegularFileAssert exists() {
        super.exists();
        isRegularFile();
        return myself;
    }

    public RegularFileAssert matchesData(String resourceFile) {
        try {
            Path expected = Zip4jvmSuite.getResourcePath(resourceFile);
            assertThat(Files.size(actual)).isEqualTo(Files.size(expected));
        } catch (Exception e) {
            assertThatThrownBy(() -> {
                throw e;
            }).doesNotThrowAnyException();
        }
        return this;
    }

    @SuppressWarnings("PMD.CognitiveComplexity")
    public RegularFileAssert matchesTextLines(String resourceFile) {
        try (BufferedReader actualReader = Files.newBufferedReader(actual);
             BufferedReader expectedReader = Files.newBufferedReader(Zip4jvmSuite.getResourcePath(resourceFile))) {
            int pos = 0;

            while (true) {
                pos++;
                String actual = actualReader.readLine();
                String expected = expectedReader.readLine();

                if (actual == null && expected == null)
                    break;
                if (StringUtils.equals(actual, expected) || expected.startsWith("<-- ignore_line -->"))
                    continue;

                actual = Optional.ofNullable(actual).orElse("");

                Matcher matcher = REGEX.matcher(expected);

                if (matcher.matches()) {
                    String regex = matcher.group("regex");
                    if (Pattern.compile(regex).matcher(actual).matches())
                        continue;

                    throw new AssertionError(
                            String.format("(line %d)\r\nExpecting:\r\n<\"%s\">\r\nto be match"
                                                  + " the pattern:\r\n<\"%s\">\r\nbut was not.",
                                          pos,
                                          actual,
                                          regex));
                } else
                    assertThatStringLine(this.actual, pos, actual).isEqualTo(expected);
            }
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            assertThatThrownBy(() -> {
                throw e;
            }).doesNotThrowAnyException();
        }

        return myself;
    }

}
