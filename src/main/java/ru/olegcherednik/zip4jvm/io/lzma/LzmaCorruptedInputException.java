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
package ru.olegcherednik.zip4jvm.io.lzma;

import java.io.IOException;

/**
 * Thrown when the compressed input data is corrupt. However, it is possible that some or all of the data already read from the input stream was
 * corrupt too.
 *
 * @author Oleg Cherednik
 * @since 14.02.2020
 */
public final class LzmaCorruptedInputException extends IOException {

    private static final long serialVersionUID = 9196697492303828564L;

    public LzmaCorruptedInputException() {
        super("LZMA compressed data is corrupt");
    }

}
