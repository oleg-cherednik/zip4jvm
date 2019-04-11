package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.Zip64EndCentralDirectory;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 11.04.2019
 */
@RequiredArgsConstructor
final class Zip64EndCentralDirectoryWriter {

    @NonNull
    private final OutputStreamDecorator out;
    @NonNull
    private final ZipModel zipModel;

    public void write(@NonNull Zip64EndCentralDirectory dir) throws IOException {
        if (!zipModel.isZip64())
            return;

        out.writeDword(InternalZipConstants.ZIP64_ENDSIG);
        out.writeLong(Zip64EndCentralDirectory.SIZE);
        out.writeWord((short)dir.getVersionMadeBy());
        out.writeWord((short)dir.getVersionNeededToExtract());

//        if (zipModel.isEmpty()) {
//            bytes.writeShort((short)0);
//            bytes.writeShort((short)0);
//        } else {
//            bytes.writeShort((short)zipModel.getFileHeaders().get(0).getVersionMadeBy());
//            bytes.writeShort((short)zipModel.getFileHeaders().get(0).getVersionToExtract());
//        }
//
//        bytes.writeInt(zipModel.getEndCentralDirectory().getDiskNumber());
//        bytes.writeInt(zipModel.getEndCentralDirectory().getStartDiskNumber());
//        bytes.writeLong(countNumberOfFileHeaderEntriesOnDisk());
//        bytes.writeLong(zipModel.getFileHeaders().size());
//        bytes.writeLong(sizeOfCentralDir);
//        bytes.writeLong(offsetCentralDir);

        /*

        dir.setSizeOfZip64EndCentralDirRec(in.readLong());
        dir.setVersionMadeBy(in.readShort());
        dir.setVersionNeededToExtract(in.readShort());
        dir.setNoOfThisDisk(in.readInt());
        dir.setNoOfThisDiskStartOfCentralDir(in.readInt());
        dir.setTotNoOfEntriesInCentralDirOnThisDisk(in.readLong());
        dir.setTotalEntries(in.readLong());
        dir.setSizeOfCentralDir(in.readLong());
        dir.setOffsetStartCenDirWRTStartDiskNo(in.readLong());
        dir.setExtensibleDataSector(in.readBytes((int)(dir.getSizeOfZip64EndCentralDirRec() - Zip64EndCentralDirectory.SIZE)));
         */
    }
}
