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

        Zip64.EndCentralDirectory dir = new Zip64.EndCentralDirectory();
        dir.setEndCentralDirectorySize(in.readQword());
        dir.setVersionMadeBy(Version.of(in.readWord()));
        dir.setVersionToExtract(Version.of(in.readWord()));
        dir.setDiskNo(in.readDword());
        dir.setMainDiskNo(in.readDword());
        dir.setDiskEntries(in.readQword());
        dir.setTotalEntries(in.readQword());
        dir.setCentralDirectorySize(in.readQword());
        dir.setCentralDirectoryRelativeOffs(in.readQword());

        realBigZip64(dir.getCentralDirectoryRelativeOffs(), "zip64.endCentralDirectory.centralDirectoryOffs");
        realBigZip64(dir.getTotalEntries(), "zip64.endCentralDirectory.totalEntries");

        return dir;
    }

}
