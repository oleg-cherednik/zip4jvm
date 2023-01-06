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
package ru.olegcherednik.zip4jvm.view;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 11.11.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class EmptyView implements View {

    public static final EmptyView INSTANCE = new EmptyView();

    @Override
    public boolean printTextInfo(PrintStream out) {
        return false;
    }

    @Override
    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        return false;
    }
}
