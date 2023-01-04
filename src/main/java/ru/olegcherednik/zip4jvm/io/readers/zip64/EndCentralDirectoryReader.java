package ru.olegcherednik.zip4jvm.io.readers.zip64;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.Version;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.realBigZip64;

/**
 * @author Oleg Cherednik
 * @since 29.12.2022
 */
public class EndCentralDirectoryReader implements Reader<Zip64.EndCentralDirectory> {

    @Override
    public Zip64.EndCentralDirectory read(DataInput in) {
        in.skip(in.dwordSignatureSize());

        Zip64.EndCentralDirectory ecd = new Zip64.EndCentralDirectory();
        ecd.setEndCentralDirectorySize(in.readQword());
        ecd.setVersionMadeBy(Version.of(in.readWord()));
        ecd.setVersionToExtract(Version.of(in.readWord()));
        ecd.setDiskNo(in.readDword());
        ecd.setMainDiskNo(in.readDword());
        ecd.setDiskEntries(in.readQword());
        ecd.setTotalEntries(in.readQword());
        ecd.setCentralDirectorySize(in.readQword());
        ecd.setCentralDirectoryRelativeOffs(in.readQword());

        realBigZip64(ecd.getCentralDirectoryRelativeOffs(), "zip64.endCentralDirectory.centralDirectoryOffs");
        realBigZip64(ecd.getTotalEntries(), "zip64.endCentralDirectory.totalEntries");

        return ecd;
    }

}
