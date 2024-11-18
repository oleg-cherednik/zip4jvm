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
package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.in.data.RandomAccessFileBaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.xxx.XxxDataInput;
import ru.olegcherednik.zip4jvm.io.readers.DataDescriptorReader;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.utils.function.XxxReader;

import lombok.Getter;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
public class BlockDataDescriptorReader implements XxxReader<DataDescriptor> {

    private final DataDescriptorReader reader;
    @Getter
    private final Block block = new Block();

    public BlockDataDescriptorReader(boolean zip64) {
        reader = DataDescriptorReader.get(zip64);
    }

    @Override
    public DataDescriptor read(XxxDataInput in) throws IOException {
        return block.calcSize((RandomAccessFileBaseDataInput) in, () -> reader.read(in));
    }

}
