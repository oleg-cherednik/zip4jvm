package com.cop.zip4j.model.activity;

import com.cop.zip4j.io.readers.DataDescriptorReader;
import com.cop.zip4j.io.writers.DataDescriptorWriter;
import com.cop.zip4j.model.DataDescriptor;
import com.cop.zip4j.model.ZipModel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 23.08.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Zip64Activity implements Activity {

    public static final Zip64Activity INSTANCE = new Zip64Activity();

    // ENdCentralDirectory

    public int getTotalEntriesECD(ZipModel zipModel) {
        return 0xFFFF;
    }

    public DataDescriptorReader getDataDescriptorReader() {
        return new DataDescriptorReader.Zip64();
    }

    public DataDescriptorWriter getDataDescriptorWriter(DataDescriptor dataDescriptor) {
        return new DataDescriptorWriter.Zip64(dataDescriptor);
    }
}
