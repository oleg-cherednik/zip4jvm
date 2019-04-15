package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.Zip64ExtendedInfo;
import net.lingala.zip4j.model.ZipModel;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
final class ExtraFieldWriter {

    @NonNull
    private final ZipModel zipModel;
    @NonNull
    private final CentralDirectory.FileHeader fileHeader;
    private final boolean writeZip64FileSize;
    private final boolean writeZip64OffsetLocalHeader;

    public void write(@NonNull OutputStreamDecorator out) throws IOException {
        if (writeZip64FileSize || writeZip64OffsetLocalHeader)
            zipModel.zip64();

        // TODO move it before
        Zip64ExtendedInfo info = fileHeader.getZip64ExtendedInfo();
        info.setSize(getSize());
        info.setUnCompressedSize(writeZip64FileSize ? fileHeader.getUncompressedSize() : -1);
        info.setCompressedSize(writeZip64FileSize ? fileHeader.getCompressedSize() : -1);
        info.setOffsLocalHeaderRelative(writeZip64OffsetLocalHeader ? fileHeader.getOffsLocalFileHeader() : -1);

        new Zip64ExtendedInfoWriter(fileHeader.getZip64ExtendedInfo()).write(out);
        new AESExtraDataRecordWriter(fileHeader.getAesExtraDataRecord(), zipModel.getCharset()).write(out);
    }

    private int getSize() {
        short dataSize = 0;

        if (writeZip64FileSize)
            dataSize += 16;
        if (writeZip64OffsetLocalHeader)
            dataSize += 8;

        return dataSize;
    }

}
