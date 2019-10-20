package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.ExtraFieldReader;
import ru.olegcherednik.zip4jvm.io.readers.FileHeaderReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public class BlockFileHeaderReader extends FileHeaderReader {

    public BlockFileHeaderReader(long totalEntries, Function<Charset, Charset> charsetCustomizer) {
        super(totalEntries, charsetCustomizer);
    }

    @Override
    protected CentralDirectory.FileHeader readFileHeader(DataInput in) throws IOException {
        Diagnostic.CentralDirectory centralDirectory = Diagnostic.getInstance().getCentralDirectory();
        centralDirectory.createFileHeader();

        CentralDirectory.FileHeader fileHeader = centralDirectory.getFileHeader().calc(in, () -> super.readFileHeader(in));
        centralDirectory.saveFileHeader(fileHeader.getFileName());

        return fileHeader;
    }

    @Override
    protected ExtraFieldReader getExtraFiledReader(int size, CentralDirectory.FileHeader fileHeader) {
        return new BlockExtraFieldReader(size, ExtraFieldReader.getReaders(fileHeader));
    }

}
