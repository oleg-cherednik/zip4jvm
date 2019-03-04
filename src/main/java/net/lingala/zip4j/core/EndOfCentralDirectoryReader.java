package net.lingala.zip4j.core;

import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.EndOfCentralDirectory;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.03.2019
 */
@RequiredArgsConstructor
public final class EndOfCentralDirectoryReader {

    private final LittleEndianRandomAccessFile in;

    public EndOfCentralDirectory read() throws ZipException, IOException {
        findHead();

        EndOfCentralDirectory dir = new EndOfCentralDirectory();
        dir.setNoOfThisDisk(in.readShort());
        dir.setNoOfThisDiskStartOfCentralDir(in.readShort());
        dir.setTotNoOfEntriesInCentralDirOnThisDisk(in.readShort());
        dir.setTotNoOfEntriesInCentralDir(in.readShort());
        dir.setSizeOfCentralDir(in.readInt());
        dir.setOffsetOfStartOfCentralDir(in.readIntAsLong());
        dir.setCommentLength(in.readShort());

        if (dir.getCommentLength() > 0)
            dir.setComment(in.readString(dir.getCommentLength()));

        return dir;
    }

    private void findHead() throws ZipException, IOException {
        int commentLength = EndOfCentralDirectory.MAX_COMMENT_LENGTH;
        long offs = in.length() - EndOfCentralDirectory.MIN_SIZE;

        do {
            in.seek(offs--);
            commentLength--;

            if (in.readInt() == InternalZipConstants.ENDSIG)
                return;
        } while (commentLength >= 0 && offs >= 0);

        throw new ZipException("zip headers not found. probably not a zip file");
    }

}
