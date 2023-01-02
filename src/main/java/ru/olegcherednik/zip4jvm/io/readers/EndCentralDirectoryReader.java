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
package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.SignatureWasNotFoundException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 04.03.2019
 */
@RequiredArgsConstructor
public class EndCentralDirectoryReader implements Reader<EndCentralDirectory> {

    private final Function<Charset, Charset> customizeCharset;

    @Override
    public EndCentralDirectory read(DataInput in) {
        in.skip(in.dwordSignatureSize());

        EndCentralDirectory ecd = new EndCentralDirectory();
        ecd.setTotalDisks(in.readWord());
        ecd.setMainDiskNo(in.readWord());
        ecd.setDiskEntries(in.readWord());
        ecd.setTotalEntries(in.readWord());
        ecd.setCentralDirectorySize(in.readDword());
        ecd.setCentralDirectoryRelativeOffs(in.readDword());
        ecd.setComment(readComment(in));

        return ecd;
    }

    private String readComment(DataInput in) {
        int commentLength = in.readWord();
        return in.readString(commentLength, customizeCharset.apply(Charsets.IBM437));
    }

}
