package com.cop.zip4j.core.writers;

import com.cop.zip4j.io.SplitOutputStream;
import com.cop.zip4j.model.EndCentralDirectory;
import com.cop.zip4j.utils.InternalZipConstants;
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

    public void write(@NonNull SplitOutputStream out) throws IOException {
        byte[] comment = dir.getComment(charset);

        out.writeDword(EndCentralDirectory.SIGNATURE);
        out.writeWord(dir.getSplitParts());
        out.writeWord(dir.getStartDiskNumber());
        out.writeWord(dir.getDiskEntries());
        out.writeWord(dir.getTotalEntries());
        out.writeDword(dir.getSize());
        out.writeDword(Math.min(dir.getOffs(), InternalZipConstants.ZIP_64_LIMIT));
        out.writeWord(comment.length);
        out.writeBytes(comment);
    }

}
