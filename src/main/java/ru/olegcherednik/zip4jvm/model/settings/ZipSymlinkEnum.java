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
package ru.olegcherednik.zip4jvm.model.settings;

/**
 * @author Oleg Cherednik
 * @since 22.01.2023
 */
public enum ZipSymlinkEnum {

    /** Ignore symlink. **/
    IGNORE_SYMLINK,
    /**
     * Replace symlink with target regular file or directory.<br>
     * It is possible to have a duplicate in zip
     */
    REPLACE_SYMLINK_WITH_TARGET
    //    /**
    //     * TODO #203 temporary disable because of implementation is too complicated and not working correctly
    //     * Replace symlink with target regular file or directory.<br>
    //     * In case of duplicate content, there will only one target with multiple relative symlinks.<br>
    //     * Duplicate is the content with similar source path.
    //     */
    //    REPLACE_SYMLINK_WITH_UNIQUE_TARGET

}
