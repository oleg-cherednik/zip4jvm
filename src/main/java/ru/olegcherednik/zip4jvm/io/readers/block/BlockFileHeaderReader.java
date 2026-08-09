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
import ru.olegcherednik.zip4jvm.io.readers.FileHeaderReader;
import ru.olegcherednik.zip4jvm.io.readers.extrafiled.ExtraFieldReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.BaseCentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.charset.CharsetProvider;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public class BlockFileHeaderReader extends FileHeaderReader {

    private final BaseCentralDirectoryBlock centralDirectoryBlock;
    private CentralDirectoryBlock.FileHeaderBlock block;

    public BlockFileHeaderReader(long totalEntries,
                                 CharsetProvider charsetProvider,
                                 BaseCentralDirectoryBlock centralDirectoryBlock) {
        super(totalEntries, charsetProvider);
        this.centralDirectoryBlock = centralDirectoryBlock;
    }

    @Override
    protected CentralDirectory.FileHeader readFileHeader(DataInput in) {
        block = centralDirectoryBlock.createFileHeaderBlock();
        CentralDirectory.FileHeader fileHeader = block.calcSize((BaseRandomAccessDataInput) in,
                                                                () -> super.readFileHeader(in));
        centralDirectoryBlock.addFileHeader(fileHeader.getFileName(), block);
        return fileHeader;
    }

    @Override
    protected ExtraFieldReader getExtraFiledReader(int size, CentralDirectory.FileHeader fileHeader) {
        return new BlockExtraFieldReader(size, ExtraFieldReader.getReaders(fileHeader), block.getExtraFieldBlock());
    }

}
