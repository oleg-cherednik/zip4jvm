package ru.olegcherednik.zip4jvm.io.readers.block;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.ExtraFieldReader;
import ru.olegcherednik.zip4jvm.io.readers.LocalFileHeaderReader;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@Getter
public class BlockLocalFileHeaderReader extends LocalFileHeaderReader {

    private final ZipEntryBlock.LocalFileHeaderBlock block = new ZipEntryBlock.LocalFileHeaderBlock();

    public BlockLocalFileHeaderReader(ZipEntry zipEntry, Function<Charset, Charset> customizeCharset) {
        super(zipEntry.getLocalFileHeaderOffs(), customizeCharset);
        block.setDisk(zipEntry.getDisk());
    }

    @Override
    protected LocalFileHeader readLocalFileHeader(DataInput in) throws IOException {
        in.cleanBuffer();
        block.getContent().setOffs(in.getOffs());
        return super.readLocalFileHeader(in);
    }

    @Override
    protected ExtraField readExtraFiled(int size, LocalFileHeader localFileHeader, DataInput in) throws IOException {
        block.getContent().calc(in.getOffs());
        block.getContent().setData(in.getLastBytes((int)block.getContent().getSize()));
        in.cleanBuffer();
        return new BlockExtraFieldReader(size, ExtraFieldReader.getReaders(localFileHeader), block.getExtraFieldBlock()).read(in);
    }

}
