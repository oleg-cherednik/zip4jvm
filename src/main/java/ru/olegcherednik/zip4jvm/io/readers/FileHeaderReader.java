package ru.olegcherednik.zip4jvm.io.readers;

import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.InternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static ru.olegcherednik.zip4jvm.model.builders.LocalFileHeaderBuilder.LOOK_IN_EXTRA_FIELD;

/**
 * @author Oleg Cherednik
 * @since 26.04.2019
 */
@RequiredArgsConstructor
final class FileHeaderReader {

    private final long totalEntries;

    public List<CentralDirectory.FileHeader> read(@NonNull DataInput in) throws IOException {
        List<CentralDirectory.FileHeader> fileHeaders = new LinkedList<>();

        for (int i = 0; i < totalEntries; i++)
            fileHeaders.add(readFileHeader(in));

        return fileHeaders;
    }

    private static CentralDirectory.FileHeader readFileHeader(DataInput in) throws IOException {
        long offs = in.getOffs();
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();

        if (in.readSignature() != CentralDirectory.FileHeader.SIGNATURE)
            throw new Zip4jException("Expected central directory entry not found offs=" + offs);

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
        fileHeader.setDisk(in.readWord());
        fileHeader.setInternalFileAttributes(getInternalFileAttribute(in.readBytes(InternalFileAttributes.SIZE)));
        fileHeader.setExternalFileAttributes(getExternalFileAttribute(in.readBytes(ExternalFileAttributes.SIZE)));
        fileHeader.setOffsLocalFileHeader(in.readDword());
        fileHeader.setFileName(in.readString(fileNameLength));
        fileHeader.setExtraField(getExtraFieldReader(extraFieldLength, fileHeader).read(in));
        fileHeader.setFileComment(in.readString(fileCommentLength));

        return fileHeader;
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static InternalFileAttributes getInternalFileAttribute(byte[] data) {
        return InternalFileAttributes.createDataBasedDelegate(data);
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static ExternalFileAttributes getExternalFileAttribute(byte[] data) {
        ExternalFileAttributes attributes = ExternalFileAttributes.createDataBasedDelegate(data);
        attributes.readFrom(data);
        return attributes;
    }

    private static ExtraFieldReader getExtraFieldReader(int size, CentralDirectory.FileHeader fileHeader) {
        boolean uncompressedSize = fileHeader.getUncompressedSize() == LOOK_IN_EXTRA_FIELD;
        boolean compressedSize = fileHeader.getCompressedSize() == LOOK_IN_EXTRA_FIELD;
        boolean offsHeader = fileHeader.getOffsLocalFileHeader() == LOOK_IN_EXTRA_FIELD;
        boolean disk = fileHeader.getDisk() == ZipModel.MAX_TOTAL_DISKS;
        return new ExtraFieldReader(size, uncompressedSize, compressedSize, offsHeader, disk);
    }

}
