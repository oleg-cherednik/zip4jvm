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
package ru.olegcherednik.zip4jvm.engine.symlink;

import ru.olegcherednik.zip4jvm.engine.np.NamedPath;
import ru.olegcherednik.zip4jvm.engine.symlink.strategy.ZipSymlinkStrategy;
import ru.olegcherednik.zip4jvm.model.settings.ZipSymlinkEnum;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ZipSymlinkEngine {

    protected final ZipSymlinkEnum zipSymlink;
    protected final ZipSymlinkStrategy zipSymlinkStrategy;
    protected final Map<Path, NamedPath> map = new LinkedHashMap<>();

    public ZipSymlinkEngine(ZipSymlinkEnum zipSymlink) {
        this.zipSymlink = zipSymlink;
        zipSymlinkStrategy = ZipSymlinkStrategy.of(zipSymlink);
    }

    // @NotNull
    public List<NamedPath> list(List<NamedPath> namedPaths) {
        return zipSymlinkStrategy.list(namedPaths);
    }

    // @NotNull
    public static Path getSymlinkTarget(Path symlink) {
        assert Files.exists(symlink) : symlink;
        assert Files.isSymbolicLink(symlink) : symlink;

        return Quietly.doRuntime(() -> {
            Path resSymlink = symlink;

            while (Files.isSymbolicLink(resSymlink)) {
                Path symlinkTarget = Files.readSymbolicLink(resSymlink);
                resSymlink = symlinkTarget.isAbsolute() ? Files.readSymbolicLink(resSymlink)
                                                        : resSymlink.getParent().resolve(symlinkTarget);
            }

            return resSymlink.toRealPath();
        });
    }

    public static void createRelativeSymlink(Path symlink, Path target) {
        Quietly.doRuntime(() -> Files.createSymbolicLink(symlink, symlink.getParent().relativize(target)));
    }

    public static void createAbsoluteSymlink(Path symlink, Path target) {
        Quietly.doRuntime(() -> Files.createSymbolicLink(symlink, target));
    }

}
