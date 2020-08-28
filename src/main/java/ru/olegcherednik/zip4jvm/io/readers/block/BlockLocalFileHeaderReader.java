package ru.olegcherednik.zip4jvm.io.readers.block;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.LocalFileHeaderReader;
import ru.olegcherednik.zip4jvm.io.readers.extrafiled.ExtraFieldReader;
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
    private final long disk;

    // TODO DataInput in - temporary
    public BlockLocalFileHeaderReader(ZipEntry zipEntry, DataInput in, Function<Charset, Charset> customizeCharset) {
        super(getAbsoluteOffs(zipEntry, in), customizeCharset);
        disk = zipEntry.getDisk();
    }

    private static long getAbsoluteOffs(ZipEntry zipEntry, DataInput in) {
        int disk = (int)zipEntry.getDisk();
        long relativeOffs = zipEntry.getLocalFileHeaderOffs();
        return in.convertToAbsoluteOffs(disk, relativeOffs);
    }

    @Override
    protected LocalFileHeader readLocalFileHeader(DataInput in) throws IOException {
        return block.getContent().calc(in, () -> super.readLocalFileHeader(in));
    }

    @Override
    protected ExtraField readExtraFiled(int size, LocalFileHeader localFileHeader, DataInput in) throws IOException {
        block.getContent().calc(in.getAbsoluteOffs());
        return new BlockExtraFieldReader(size, ExtraFieldReader.getReaders(localFileHeader), block.getExtraFieldBlock()).read(in);
    }

}
