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
package ru.olegcherednik.zip4jvm.io.readers.block.zip64;

import ru.olegcherednik.zip4jvm.io.readers.zip64.EndCentralDirectoryLocatorReader;
import ru.olegcherednik.zip4jvm.io.readers.zip64.EndCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.zip64.ExtensibleDataSectorReader;
import ru.olegcherednik.zip4jvm.io.readers.zip64.Zip64Reader;
import ru.olegcherednik.zip4jvm.model.block.Zip64Block;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@RequiredArgsConstructor
public final class BlockZip64Reader extends Zip64Reader {

    private final Zip64Block zip64Block;

    @Override
    protected EndCentralDirectoryLocatorReader getEndCentralDirectoryLocatorReader() {
        return new BlockEndCentralDirectoryLocatorReader(zip64Block.getEndCentralDirectoryLocatorBlock());
    }

    @Override
    protected EndCentralDirectoryReader getEndCentralDirectoryReader() {
        return new BlockEndCentralDirectoryReader(zip64Block.getEndCentralDirectoryBlock());
    }

    @Override
    protected ExtensibleDataSectorReader getExtensibleDataSectorReader() {
        return new BlockExtensibleDataSectorReader(zip64Block.getExtensibleDataSectorBlock());
    }

}
