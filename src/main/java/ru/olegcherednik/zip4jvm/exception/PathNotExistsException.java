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
package ru.olegcherednik.zip4jvm.exception;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 11.08.2019
 */
public class PathNotExistsException extends Zip4jvmException {

    private static final long serialVersionUID = 6634130368683535775L;

    public PathNotExistsException(Path path) {
        super("Path not exists: " + path);
    }
}
