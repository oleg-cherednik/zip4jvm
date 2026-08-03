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

/**
 * @author Oleg Cherednik
 * @since 22.09.2024
 */
public class IncorrectCentralDirectoryPasswordException extends IncorrectPasswordException {

    private static final long serialVersionUID = 3259098878115692516L;

    public IncorrectCentralDirectoryPasswordException() {
        super("Incorrect password for central directory");
    }

}
