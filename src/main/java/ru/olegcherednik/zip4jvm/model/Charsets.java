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
package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 07.10.2019
 */
@SuppressWarnings("CharsetObjectCanBeUsed")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Charsets {

    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final Charset IBM437 = Charset.forName("IBM437");
    public static final Charset SYSTEM = Charset.forName(System.getProperty("sun.jnu.encoding", "UTF-8"));

    public static final Charset ZIP_DEFAULT = IBM437;

    public static final Function<Charset, Charset> UNMODIFIED = charset -> charset;
    public static final Function<Charset, Charset> SYSTEM_CHARSET = charset -> SYSTEM;

}
