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
package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.io.in.RandomAccess;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
public interface DataInput extends DataInputNew {

    long convertToAbsoluteOffs(int diskNo, long relativeOffs);

    // Retrieves offs starting from the beginning of the current disk
    long getDiskRelativeOffs();

    SrcZip getSrcZip();

    SrcZip.Disk getDisk();

    long size() throws IOException;

    void seek(int diskNo, long relativeOffs) throws IOException;

    void seek(String id) throws IOException;

}
