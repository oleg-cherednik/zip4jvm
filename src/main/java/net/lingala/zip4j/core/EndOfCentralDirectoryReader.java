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

    private final LittleEndianRandomAccessFile zip4jRaf;

    public EndOfCentralDirectory read() throws ZipException, IOException {
        findHead();

        EndOfCentralDirectory dir = new EndOfCentralDirectory();
        dir.setNoOfThisDisk(zip4jRaf.readShort());
        dir.setNoOfThisDiskStartOfCentralDir(zip4jRaf.readShort());
        dir.setTotNoOfEntriesInCentralDirOnThisDisk(zip4jRaf.readShort());
        dir.setTotNoOfEntriesInCentralDir(zip4jRaf.readShort());
        dir.setSizeOfCentralDir(zip4jRaf.readInt());
        dir.setOffsetOfStartOfCentralDir(zip4jRaf.readIntAsLong());
        dir.setCommentLength(zip4jRaf.readShort());

        if (dir.getCommentLength() > 0)
            dir.setComment(zip4jRaf.readString(dir.getCommentLength()));

        return dir;
    }

    private void findHead() throws ZipException, IOException {
        int commentLength = EndOfCentralDirectory.MAX_COMMENT_LENGTH;
        long offs = zip4jRaf.length() - EndOfCentralDirectory.MIN_SIZE;

        do {
            zip4jRaf.seek(offs--);
            commentLength--;

            if (zip4jRaf.readInt() == InternalZipConstants.ENDSIG)
                return;
        } while (commentLength >= 0 && offs >= 0);

        throw new ZipException("zip headers not found. probably not a zip file");
    }

}
