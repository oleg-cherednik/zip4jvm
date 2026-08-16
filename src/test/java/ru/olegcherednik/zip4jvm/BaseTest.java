/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package ru.olegcherednik.zip4jvm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 16.08.2026
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseTest {

    protected final DirRoot dirRoot = new DirRoot();

    @BeforeClass
    public void createDir() {
        dirRoot.createDir();
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public void removeDir() {
        dirRoot.removeDir();
    }

    public Path getDirRoot() {
        return dirRoot.getPath();
    }

    public Path resolve(String other) {
        return getDirRoot().resolve(other);
    }

    public Path resolve(Path other) {
        return getDirRoot().resolve(other);
    }

    public Path getTestRoot() {
        return dirRoot.getTestRoot();
    }

    public Path getZip() {
        return dirRoot.getZipSrc();
    }

}
