package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.CentralDirectory;
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

        new Zip64ExtendedInfoWriter(zipModel, fileHeader, writeZip64FileSize, writeZip64OffsetLocalHeader);
        new AESExtraDataRecordWriter(fileHeader.getAesExtraDataRecord(), zipModel).write(out);
    }

}
