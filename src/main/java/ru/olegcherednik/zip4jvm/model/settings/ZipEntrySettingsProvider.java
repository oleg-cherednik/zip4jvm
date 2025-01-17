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
package ru.olegcherednik.zip4jvm.model.settings;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipEntrySettingsProvider {

    public static final ZipEntrySettingsProvider DEFAULT = of(ZipEntrySettings.DEFAULT);

    private final Function<String, ZipEntrySettings> entryNameSettings;

    public static ZipEntrySettingsProvider of(ZipEntrySettings entrySettings) {
        return new ZipEntrySettingsProvider(entryName -> entrySettings);
    }

    public static ZipEntrySettingsProvider of(Function<String, ZipEntrySettings> entryNameSettings) {
        return new ZipEntrySettingsProvider(entryNameSettings);
    }

    // @NotNull
    public ZipEntrySettings getEntrySettings(String entryName) {
        return Optional.ofNullable(entryNameSettings.apply(entryName))
                       .orElse(ZipEntrySettings.DEFAULT);
    }

}
