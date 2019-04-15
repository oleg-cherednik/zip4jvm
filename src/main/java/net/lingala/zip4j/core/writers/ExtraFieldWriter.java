package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
final class ExtraFieldWriter {

    @NonNull
    private final CentralDirectory.FileHeader fileHeader;
    @NonNull
    private final ZipModel zipModel;
    private final boolean writeZip64FileSize;
    private final boolean writeZip64OffsetLocalHeader;

    public void write(@NonNull OutputStreamDecorator out) throws IOException {
        if (writeZip64FileSize || writeZip64OffsetLocalHeader)
            zipModel.zip64();

        writeZip64ExtendedInfo(out, fileHeader, writeZip64FileSize, writeZip64OffsetLocalHeader);
        writeAesExtraDataRecord(out, fileHeader.getAesExtraDataRecord());
    }

    private void writeZip64ExtendedInfo(OutputStreamDecorator out, CentralDirectory.FileHeader fileHeader, boolean writeZip64FileSize,
            boolean writeZip64OffsetLocalHeader) throws IOException {
        if (!zipModel.isZip64())
            return;


        //Zip64 header
        out.writeWord((short)InternalZipConstants.EXTRAFIELDZIP64LENGTH);

        //Zip64 extra data record size
        short dataSize = 0;

        if (writeZip64FileSize)
            dataSize += 16;
        if (writeZip64OffsetLocalHeader)
            dataSize += 8;

        out.writeWord(dataSize);

        if (writeZip64FileSize) {
            out.writeDword(fileHeader.getUncompressedSize());

            out.writeDword(fileHeader.getCompressedSize());
        }

        if (writeZip64OffsetLocalHeader)
            out.writeDword(fileHeader.getOffsLocalFileHeader());
    }

    private void writeAesExtraDataRecord(OutputStreamDecorator out, AESExtraDataRecord record) throws IOException {
        if (record == null)
            return;

        out.writeWord((short)record.getSignature());
        out.writeWord((short)record.getDataSize());
        out.writeShort((short)record.getVersionNumber());
        out.writeBytes(record.getVendor().getBytes(zipModel.getCharset()));
        out.writeBytes(record.getAesStrength().getValue(), (byte)0);
        out.writeWord(record.getCompressionMethod().getValue());
    }

}
