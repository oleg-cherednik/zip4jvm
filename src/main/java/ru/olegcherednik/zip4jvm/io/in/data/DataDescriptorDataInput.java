package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.io.in.data.xxx.BaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.xxx.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DataDescriptorReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 15.11.2024
 */
public class DataDescriptorDataInput extends BaseDataInput {

    private final ZipEntry zipEntry;

    public static DataDescriptorDataInput create(ZipEntry zipEntry, DataInput in) {
        return new DataDescriptorDataInput(zipEntry, in);
    }

    protected DataDescriptorDataInput(ZipEntry zipEntry, DataInput in) {
        super(in);
        this.zipEntry = zipEntry;
    }

    // ---------- AutoCloseable ----------

    /**
     * Just read {@link DataDescriptor} and ignore its value. We get it from
     * {@link CentralDirectory.FileHeader}
     */
    @Override
    public void close() throws IOException {
        if (zipEntry.isDataDescriptorAvailable()) {
            DataDescriptorReader reader = DataDescriptorReader.get(zipEntry.isZip64());
            /* DataDescriptor dataDescriptor = */
            reader.read(in);
        }

        super.close();
    }

}
