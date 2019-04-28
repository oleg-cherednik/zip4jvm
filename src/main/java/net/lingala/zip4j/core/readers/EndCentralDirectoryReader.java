package net.lingala.zip4j.core.readers;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.EndCentralDirectory;
import net.lingala.zip4j.io.LittleEndianRandomAccessFile;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.03.2019
 */
@RequiredArgsConstructor
final class EndCentralDirectoryReader {

    @Getter
    private long offs = -1;

    @NonNull
    public EndCentralDirectory read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        findHead(in);

        EndCentralDirectory dir = new EndCentralDirectory();
        dir.setSplitParts(in.readWord());
        dir.setStartDiskNumber(in.readWord());
        dir.setDiskEntries(in.readWord());
        dir.setTotalEntries(in.readWord());
        dir.setSize(in.readDwordLong());
        dir.setOffs(in.readDwordLong());
        dir.setComment(in.readString(in.readWord() & 0xFFFF));

        return dir;
    }

    private void findHead(LittleEndianRandomAccessFile in) throws IOException {
        offs = -1;

        int commentLength = EndCentralDirectory.MAX_COMMENT_LENGTH;
        long offs = in.length() - EndCentralDirectory.MIN_SIZE;

        do {
            in.seek(offs--);
            commentLength--;
            this.offs = in.getFilePointer();

            if (in.readDword() == EndCentralDirectory.SIGNATURE)
                return;
        } while (commentLength >= 0 && offs >= 0);

        this.offs = -1;

        throw new ZipException("zip headers not found. probably not a zip file");
    }

}
