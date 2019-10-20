package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.diagnostic.BlockExtraFieldReader;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.diagnostic.Diagnostic;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
@RequiredArgsConstructor
public final class LocalFileHeaderReader implements Reader<LocalFileHeader> {

    private final long localFileHeaderOffs;
    private final Function<Charset, Charset> charsetCustomizer;

    @Override
    public LocalFileHeader read(DataInput in) throws IOException {
        findHead(in);

        LocalFileHeader localFileHeader = new LocalFileHeader();

        localFileHeader.setVersionToExtract(in.readWord());
        localFileHeader.setGeneralPurposeFlag(new GeneralPurposeFlag(in.readWord()));
        localFileHeader.setCompressionMethod(CompressionMethod.parseCode(in.readWord()));
        localFileHeader.setLastModifiedTime((int)in.readDword());
        localFileHeader.setCrc32(in.readDword());
        localFileHeader.setCompressedSize(in.readDword());
        localFileHeader.setUncompressedSize(in.readDword());

        int fileNameLength = in.readWord();
        int extraFieldLength = in.readWord();
        Charset charset = localFileHeader.getGeneralPurposeFlag().getCharset();

        localFileHeader.setFileName(in.readString(fileNameLength, charsetCustomizer.apply(charset)));
        localFileHeader.setExtraField(getExtraFiledReader(extraFieldLength, localFileHeader).read(in));

        return localFileHeader;
    }

    private static ExtraFieldReader getExtraFiledReader(int size, LocalFileHeader localFileHeader) {
        if (Diagnostic.getInstance() == Diagnostic.NULL)
            return new ExtraFieldReader(size, ExtraFieldReader.getReaders(localFileHeader));
        return new BlockExtraFieldReader(size, ExtraFieldReader.getReaders(localFileHeader));
    }

    private void findHead(DataInput in) throws IOException {
        in.seek(localFileHeaderOffs);

        if (in.readDwordSignature() != LocalFileHeader.SIGNATURE)
            throw new Zip4jvmException("invalid local file header signature");
    }

}
