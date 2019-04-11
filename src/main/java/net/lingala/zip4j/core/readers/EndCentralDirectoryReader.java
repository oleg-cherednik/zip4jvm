package net.lingala.zip4j.core.readers;

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
final class EndCentralDirectoryReader {

    private final LittleEndianRandomAccessFile in;
    @Getter
    private long offs = -1;

    public EndCentralDirectory read() throws IOException {
        findHead();

        EndCentralDirectory dir = new EndCentralDirectory();
        dir.setDiskNumber(in.readWord());
        dir.setStartDiskNumber(in.readWord());
        dir.setDiskEntries(in.readWord());
        dir.setTotalEntries(in.readWord());
        dir.setSize(in.readDword());
        dir.setOffs(in.readDwordLong());

        short commentLength = in.readWord();
        dir.setComment(in.readString(commentLength));

        return dir;
    }

    private void findHead() throws IOException {
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
