package net.lingala.zip4j.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.model.EndCentralDirectory;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.03.2019
 */
@RequiredArgsConstructor
public final class EndCentralDirectoryReader {

    private final LittleEndianRandomAccessFile in;
    @Getter
    private long offs = -1;

    public EndCentralDirectory read() throws IOException {
        findHead();

        EndCentralDirectory dir = new EndCentralDirectory();
        dir.setNoOfDisk(in.readShort());
        dir.setNoOfDiskStartCentralDir(in.readShort());
        dir.setTotalNumberOfEntriesInCentralDirOnThisDisk(in.readShort());
        dir.setTotNoOfEntriesInCentralDir(in.readShort());
        dir.setSizeOfCentralDir(in.readInt());
        dir.setOffsetOfStartOfCentralDir(in.readIntAsLong());
        dir.setCommentLength(in.readShort());

        if (dir.getCommentLength() > 0)
            dir.setComment(in.readString(dir.getCommentLength()));

        return dir;
    }

    public void findHead() throws IOException {
        int commentLength = EndCentralDirectory.MAX_COMMENT_LENGTH;
        long offs = in.length() - EndCentralDirectory.MIN_SIZE;

        do {
            in.seek(offs--);
            commentLength--;
            this.offs = in.getFilePointer();

            if (in.readInt() == InternalZipConstants.ENDSIG)
                return;
        } while (commentLength >= 0 && offs >= 0);

        this.offs = -1;

        throw new IOException("zip headers not found. probably not a zip file");
    }

}
