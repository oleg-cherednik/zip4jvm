/*
 * Copyright © 2019 Cherednik Oleg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.olegcherednik.zip4jvm;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.engine.UnzipEngine;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Extract regular files and/or directories from the zip archive
 *
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class UnzipIt {

    private final Path zip;
    private Path destDir;
    private UnzipSettings settings = UnzipSettings.DEFAULT;

    public static UnzipIt zip(Path zip) {
        return new UnzipIt(zip).destDir(zip.getParent());
    }

    public UnzipIt destDir(Path destDir) {
        this.destDir = destDir;
        return this;
    }

    public UnzipIt settings(UnzipSettings settings) {
        this.settings = Optional.ofNullable(settings).orElse(UnzipSettings.DEFAULT);
        return this;
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public UnzipIt password(char[] password) {
        settings = settings.toBuilder().password(password).build();
        return this;
    }

    public void extract() throws IOException {
        new UnzipEngine(zip, settings).extract(destDir);
    }

    public void extract(@NonNull String fileName) throws IOException {
        extract(Collections.singleton(fileName));
    }

    public void extract(Collection<String> fileNames) throws IOException {
        ZipFile.Reader zipFile = ZipFile.reader(zip, settings);

        for (String fileName : fileNames)
            zipFile.extract(destDir, fileName);
    }

    public InputStream stream(String fileName) throws IOException {
        return ZipFile.reader(zip, settings).extract(fileName).getInputStream();
    }

    public ZipFile.Reader open() throws IOException {
        return ZipFile.reader(zip, settings);
    }

}
