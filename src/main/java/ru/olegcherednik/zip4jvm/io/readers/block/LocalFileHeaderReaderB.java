package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.ExtraFieldReader;
import ru.olegcherednik.zip4jvm.io.readers.LocalFileHeaderReader;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public class LocalFileHeaderReaderB extends LocalFileHeaderReader {

    private final Diagnostic.ExtraFieldBlock localFileHeader;

    public LocalFileHeaderReaderB(long offs, Function<Charset, Charset> charsetCustomizer, Diagnostic.ExtraFieldBlock localFileHeader) {
        super(offs, charsetCustomizer);
        this.localFileHeader = localFileHeader;
    }

    @Override
    protected LocalFileHeader readLocalFileHeader(DataInput in) throws IOException {
        return localFileHeader.calc(in, () -> super.readLocalFileHeader(in));
    }

    @Override
    protected ExtraFieldReader getExtraFiledReader(int size, LocalFileHeader localFileHeader) {
        return new BlockExtraFieldReader(size, ExtraFieldReader.getReaders(localFileHeader), this.localFileHeader);
    }

}
