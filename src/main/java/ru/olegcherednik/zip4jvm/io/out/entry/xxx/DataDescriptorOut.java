package ru.olegcherednik.zip4jvm.io.out.entry.xxx;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.writers.DataDescriptorWriter;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 29.10.2024
 */
public final class DataDescriptorOut {

    public void write(ZipEntry zipEntry, DataOutput out) throws IOException {
        if (!zipEntry.isDataDescriptorAvailable())
            return;

        DataDescriptor dataDescriptor = new DataDescriptor(zipEntry.getChecksum(),
                                                           zipEntry.getCompressedSize(),
                                                           zipEntry.getUncompressedSize());
        DataDescriptorWriter.get(zipEntry.isZip64(), dataDescriptor).write(out);
    }

}
