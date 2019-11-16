package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.ExtraFieldReader;
import ru.olegcherednik.zip4jvm.io.readers.LocalFileHeaderReader;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public class BlockLocalFileHeaderReader extends LocalFileHeaderReader {

    private final Diagnostic.ZipEntryBlock.LocalFileHeaderBlock localFileHeader;

    public BlockLocalFileHeaderReader(long offs, Function<Charset, Charset> charsetCustomizer, Diagnostic.ZipEntryBlock.LocalFileHeaderBlock localFileHeader) {
        super(offs, charsetCustomizer);
        this.localFileHeader = localFileHeader;
    }

    @Override
    protected LocalFileHeader readLocalFileHeader(DataInput in) throws IOException {
        in.cleanBuffer();
        localFileHeader.getContent().setOffs(in.getOffs());
        return super.readLocalFileHeader(in);
    }

    @Override
    protected ExtraField readExtraFiled(int size, LocalFileHeader localFileHeader, DataInput in) throws IOException {
        this.localFileHeader.getContent().calc(in.getOffs());
        this.localFileHeader.getContent().setData(in.getLastBytes((int)this.localFileHeader.getContent().getSize()));
        in.cleanBuffer();
        return new BlockExtraFieldReader(size, ExtraFieldReader.getReaders(localFileHeader), this.localFileHeader.getExtraFieldBlock()).read(in);
    }

}
