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
public final class PlainActivity implements Activity {

    public static final PlainActivity INSTANCE = new PlainActivity();

    // ENdCentralDirectory

    public int getTotalEntriesECD(ZipModel zipModel) {
        return zipModel.getEntries().size();
    }

    public DataDescriptorReader getDataDescriptorReader() {
        return new DataDescriptorReader.Plain();
    }

    public DataDescriptorWriter getDataDescriptorWriter(DataDescriptor dataDescriptor) {
        return new DataDescriptorWriter.Plain(dataDescriptor);
    }
}
