package com.cop.zip4j.io.in;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.out.entry.EntryOutputStream;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
public class SplitZipInputStream extends BaseMarkDataInput {

    @NonNull
    public static SplitZipInputStream create(@NonNull ZipModel zipModel, int diskNumber) throws IOException {
        int counter = diskNumber + 1;

        LittleEndianReadFile in = new LittleEndianReadFile(zipModel.getPartFile(diskNumber));

        if (counter == 1) {
            int signature = in.readDword();

            if (signature != EntryOutputStream.SPLIT_SIGNATURE)
                throw new Zip4jException("Expected first part of split file signature (offs:" + in.getOffs() + ')');
        }

        return new SplitZipInputStream(in);
    }

    private SplitZipInputStream(@NonNull DataInput delegate) {
        super(delegate);
    }

}
