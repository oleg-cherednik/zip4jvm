package com.cop.zip4j.io.writers;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.EndCentralDirectory;
import com.cop.zip4j.model.Zip64;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 10.04.2019
 */
@RequiredArgsConstructor
final class EndCentralDirectoryWriter {

    @NonNull
    private final EndCentralDirectory endCentralDirectory;
    @NonNull
    private final Charset charset;

    public void write(@NonNull DataOutput out) throws IOException {
        byte[] comment = endCentralDirectory.getComment(charset);

        out.writeDwordSignature(EndCentralDirectory.SIGNATURE);
        out.writeWord(endCentralDirectory.getTotalDisks());
        out.writeWord(endCentralDirectory.getStartDiskNumber());
        out.writeWord(endCentralDirectory.getDiskEntries());
        out.writeWord(endCentralDirectory.getTotalEntries());
        out.writeDword(endCentralDirectory.getCentralDirectorySize());
        out.writeDword(Math.min(endCentralDirectory.getCentralDirectoryOffs(), Zip64.LIMIT));
        out.writeWord(comment.length);
        out.writeBytes(comment);
    }

}
