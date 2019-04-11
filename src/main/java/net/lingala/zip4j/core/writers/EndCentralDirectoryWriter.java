package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.EndCentralDirectory;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 10.04.2019
 */
@RequiredArgsConstructor
final class EndCentralDirectoryWriter {

    @NonNull
    private final OutputStreamDecorator out;
    @NonNull
    private final ZipModel zipModel;

    public void write(@NonNull EndCentralDirectory dir, long offs, int size) throws IOException {
        out.writeDword(dir.getSignature());
        out.writeWord((short)dir.getDiskNumber());
        out.writeWord((short)dir.getStartDiskNumber());
        out.writeWord((short)dir.getTotalEntries());
        out.writeWord((short)dir.getDiskEntries());
        out.writeDword(dir.getSize());
        out.writeDword(Math.min(offs, InternalZipConstants.ZIP_64_LIMIT));

        byte[] comment = dir.getComment(zipModel.getCharset());
        out.writeWord((short)ArrayUtils.getLength(comment));
        out.writeBytes(comment);

        /*
                dir.setDiskNumber(in.readWord());
        dir.setStartDiskNumber(in.readWord());
        dir.setDiskEntries(in.readWord());
        dir.setTotalEntries(in.readWord());
        dir.setSize(in.readDword());
        dir.setOffs(in.readDwordLong());
        dir.setCommentLength(in.readWord());
        dir.setComment(in.readString(dir.getCommentLength()));
         */
    }

}
