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
package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.random.BaseRandomAccessDataInput;
import ru.olegcherednik.zip4jvm.io.readers.EndCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.charset.CharsetProvider;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public class BlockEndCentralDirectoryReader extends EndCentralDirectoryReader {

    private final Block block;

    public BlockEndCentralDirectoryReader(CharsetProvider charsetProvider, Block block) {
        super(charsetProvider);
        this.block = block;
    }

    @Override
    public EndCentralDirectory read(DataInput in) {
        return block.calcSize((BaseRandomAccessDataInput) in, () -> super.read(in));
    }

}
