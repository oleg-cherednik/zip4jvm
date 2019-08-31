package com.cop.zip4j.model.activity;

import com.cop.zip4j.io.readers.DataDescriptorReader;
import com.cop.zip4j.io.writers.DataDescriptorWriter;
import com.cop.zip4j.model.DataDescriptor;
import com.cop.zip4j.model.ZipModel;

/**
 * @author Oleg Cherednik
 * @since 23.08.2019
 */
public interface Activity {

    // EndCentralDirectory

    int getTotalEntriesECD(ZipModel zipModel);

    DataDescriptorReader getDataDescriptorReader();

    DataDescriptorWriter getDataDescriptorWriter(DataDescriptor dataDescriptor);

}
