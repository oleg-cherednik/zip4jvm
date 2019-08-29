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
    private final EndCentralDirectory dir;
    @NonNull
    private final Charset charset;

    public void write(@NonNull DataOutput out) throws IOException {
        byte[] comment = dir.getComment(charset);

        out.writeDwordSignature(EndCentralDirectory.SIGNATURE);
        out.writeWord(dir.getSplitParts());
        out.writeWord(dir.getStartDiskNumber());
        out.writeWord(dir.getDiskEntries());
        out.writeWord(dir.getTotalEntries());
        out.writeDword(dir.getSize());
        out.writeDword(Math.min(dir.getCentralDirectoryOffs(), Zip64.LIMIT));
        out.writeWord(comment.length);
        out.writeBytes(comment);
    }

}
