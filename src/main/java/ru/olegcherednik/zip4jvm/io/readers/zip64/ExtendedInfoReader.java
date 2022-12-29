package ru.olegcherednik.zip4jvm.io.readers.zip64;

import lombok.AllArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.realBigZip64;

/**
 * @author Oleg Cherednik
 * @since 29.12.2022
 */
@AllArgsConstructor
public class ExtendedInfoReader implements Reader<Zip64.ExtendedInfo> {

    private final int size;
    private boolean uncompressedSizeExists;
    private boolean compressedSizeExists;
    private boolean offsLocalHeaderRelativeExists;
    private boolean diskExists;

    private void updateFlags(DataInput in) {
        if (uncompressedSizeExists || compressedSizeExists || offsLocalHeaderRelativeExists || diskExists)
            return;

        uncompressedSizeExists = size >= in.qwordSize();
        compressedSizeExists = size >= in.qwordSize() * 2;
        offsLocalHeaderRelativeExists = size >= in.qwordSize() * 3;
        diskExists = size >= in.qwordSize() * 3 + in.dwordSize();
    }

    @Override
    public Zip64.ExtendedInfo read(DataInput in) {
        long offs = in.getAbsoluteOffs();
        updateFlags(in);

        Zip64.ExtendedInfo extendedInfo = readExtendedInfo(in);

        if (in.getAbsoluteOffs() - offs != size) {
            // section exists, but not need to read it; all data is in FileHeader
            extendedInfo = Zip64.ExtendedInfo.NULL;
            in.seek(offs + size);
        }

        if (extendedInfo.getDiskNo() != ExtraField.NO_DATA)
            realBigZip64(extendedInfo.getDiskNo(), "zip64.extendedInfo.disk");

        return extendedInfo;
    }

    private Zip64.ExtendedInfo readExtendedInfo(DataInput in) {
        return Zip64.ExtendedInfo.builder()
                                 .uncompressedSize(uncompressedSizeExists ? in.readQword() : ExtraField.NO_DATA)
                                 .compressedSize(compressedSizeExists ? in.readQword() : ExtraField.NO_DATA)
                                 .localFileHeaderRelativeOffs(offsLocalHeaderRelativeExists ? in.readQword() : ExtraField.NO_DATA)
                                 .diskNo(diskExists ? in.readDword() : ExtraField.NO_DATA)
                                 .build();
    }

    @Override
    public String toString() {
        return "ZIP64";
    }

}
