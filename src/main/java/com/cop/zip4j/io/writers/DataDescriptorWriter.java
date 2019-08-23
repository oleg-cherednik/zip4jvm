package com.cop.zip4j.io.writers;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.DataDescriptor;
import com.cop.zip4j.model.activity.Activity;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 25.07.2019
 */
@RequiredArgsConstructor
public final class DataDescriptorWriter {

    @NonNull
    private final DataDescriptor dataDescriptor;
    private final Activity activity;

    public void write(@NonNull DataOutput out) throws IOException {
        out.writeDwordSignature(DataDescriptor.SIGNATURE);
        out.writeDword(dataDescriptor.getCrc32());
        activity.writeValueDataDescriptor(dataDescriptor.getCompressedSize(), out);
        activity.writeValueDataDescriptor(dataDescriptor.getUncompressedSize(), out);
    }

}

