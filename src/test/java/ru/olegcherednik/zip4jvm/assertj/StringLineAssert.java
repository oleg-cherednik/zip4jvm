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

import org.assertj.core.api.AbstractStringAssert;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 14.11.2019
 */
public class StringLineAssert extends AbstractStringAssert<StringLineAssert> {

    protected final Path path;
    protected final int pos;

    public StringLineAssert(Path path, int pos, String actual) {
        super(actual, StringLineAssert.class);
        this.path = path;
        this.pos = pos;
    }

    @Override
    public StringLineAssert isEqualTo(Object expected) {
        try {
            return super.isEqualTo(expected);
        } catch (AssertionError e) {
            throw new AssertionError(String.format("%s (line %d) %s", path.toAbsolutePath(), pos, e.getMessage()), e);
        }
    }
}
