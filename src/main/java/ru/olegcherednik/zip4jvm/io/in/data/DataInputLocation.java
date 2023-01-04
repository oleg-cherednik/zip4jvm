package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.model.src.SrcZip;

/**
 * Represents a location of the {@link DataInput}
 *
 * @author Oleg Cherednik
 * @since 24.12.2022
 */
public interface DataInputLocation {

    long getAbsoluteOffs();

    long getDiskRelativeOffs();

    SrcZip getSrcZip();

    SrcZip.Disk getDisk();

}
