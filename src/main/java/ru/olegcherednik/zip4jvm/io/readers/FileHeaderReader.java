package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.InternalFileAttributes;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static ru.olegcherednik.zip4jvm.io.readers.ZipModelReader.MARK_FILE_HEADER_END_OFFS;
import static ru.olegcherednik.zip4jvm.io.readers.ZipModelReader.MARK_FILE_HEADER_OFFS;
import static ru.olegcherednik.zip4jvm.model.ExternalFileAttributes.PROP_OS_NAME;

/**
 * @author Oleg Cherednik
 * @since 26.04.2019
 */
@RequiredArgsConstructor
final class FileHeaderReader implements Reader<List<CentralDirectory.FileHeader>> {

    private final long totalEntries;
    private final Function<Charset, Charset> charsetCustomizer;

    @Override
    public List<CentralDirectory.FileHeader> read(DataInput in) throws IOException {
        List<CentralDirectory.FileHeader> fileHeaders = new LinkedList<>();

        for (int i = 0; i < totalEntries; i++) {
            long offs = in.getOffs();

            if (in.readSignature() != CentralDirectory.FileHeader.SIGNATURE)
                throw new Zip4jvmException("Expected central directory entry not found offs=" + offs);

            CentralDirectory.FileHeader fileHeader = readFileHeader(in);
            fileHeaders.add(fileHeader);

            in.mark(MARK_FILE_HEADER_OFFS + '_' + fileHeader.getFileName(), offs);
            in.mark(MARK_FILE_HEADER_END_OFFS + '_' + fileHeader.getFileName());
        }

        return fileHeaders;
    }

    private CentralDirectory.FileHeader readFileHeader(DataInput in) throws IOException {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();

        fileHeader.setVersionMadeBy(in.readWord());
        fileHeader.setVersionToExtract(in.readWord());
        fileHeader.setGeneralPurposeFlagData(in.readWord());
        fileHeader.setCompressionMethod(CompressionMethod.parseCode(in.readWord()));
        fileHeader.setLastModifiedTime((int)in.readDword());
        fileHeader.setCrc32(in.readDword());
        fileHeader.setCompressedSize(in.readDword());
        fileHeader.setUncompressedSize(in.readDword());

        int fileNameLength = in.readWord();
        int extraFieldLength = in.readWord();
        int fileCommentLength = in.readWord();
        Charset charset = fileHeader.getGeneralPurposeFlag().getCharset();

        fileHeader.setDisk(in.readWord());
        fileHeader.setInternalFileAttributes(getInternalFileAttribute(in.readBytes(InternalFileAttributes.SIZE)));
        fileHeader.setExternalFileAttributes(getExternalFileAttribute(in.readBytes(ExternalFileAttributes.SIZE)));
        fileHeader.setLocalFileHeaderOffs(in.readDword());
        fileHeader.setFileName(in.readString(fileNameLength, charsetCustomizer.apply(charset)));
        fileHeader.setExtraField(ExtraFieldReader.build(extraFieldLength, fileHeader).read(in));
        fileHeader.setComment(in.readString(fileCommentLength, charsetCustomizer.apply(charset)));

        return fileHeader;
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static InternalFileAttributes getInternalFileAttribute(byte[] data) {
        return InternalFileAttributes.build(data);
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static ExternalFileAttributes getExternalFileAttribute(byte[] data) throws IOException {
        return ExternalFileAttributes.build(PROP_OS_NAME).readFrom(data);
    }

}
