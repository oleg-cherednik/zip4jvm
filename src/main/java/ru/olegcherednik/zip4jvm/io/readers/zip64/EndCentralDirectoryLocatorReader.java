package ru.olegcherednik.zip4jvm.io.readers.zip64;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.realBigZip64;

/**
 * @author Oleg Cherednik
 * @since 29.12.2022
 */
public class EndCentralDirectoryLocatorReader implements Reader<Zip64.EndCentralDirectoryLocator> {

    @Override
    public Zip64.EndCentralDirectoryLocator read(DataInput in) {
        in.skip(in.dwordSignatureSize());

        Zip64.EndCentralDirectoryLocator locator = new Zip64.EndCentralDirectoryLocator();
        locator.setMainDiskNo(in.readDword());
        locator.setEndCentralDirectoryRelativeOffs(in.readQword());
        locator.setTotalDisks(in.readDword());

        realBigZip64(locator.getMainDiskNo(), "zip64.locator.mainDisk");
        realBigZip64(locator.getMainDiskNo(), "zip64.locator.totalDisks");
        realBigZip64(locator.getEndCentralDirectoryRelativeOffs(), "zip64.locator.centralDirectoryOffs");

        return locator;
    }

}
