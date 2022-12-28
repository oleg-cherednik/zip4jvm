package ru.olegcherednik.zip4jvm.io.in.buf;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputFile;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputLocation;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

/**
 * @author Oleg Cherednik
 * @since 24.12.2022
 */
@Getter
public final class SimpleDataInputLocation implements DataInputLocation {

    private final long absoluteOffs;
    private final long diskRelativeOffs;
    private final SrcZip srcZip;
    private final SrcZip.Disk disk;

    public SimpleDataInputLocation(DataInputFile in) {
        absoluteOffs = in.getAbsoluteOffs();
        diskRelativeOffs = in.getDiskRelativeOffs();
        srcZip = in.getSrcZip();
        disk = in.getDisk();
    }
}
