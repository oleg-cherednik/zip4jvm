package com.cop.zip4j.io.readers;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.EndCentralDirectory;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.03.2019
 */
final class EndCentralDirectoryReader {

    @NonNull
    public EndCentralDirectory read(@NonNull DataInput in) throws IOException {
        long offs = findHead(in);

        EndCentralDirectory dir = new EndCentralDirectory();
        dir.setTotalDisks(in.readWord());
        dir.setStartDiskNumber(in.readWord());
        dir.setDiskEntries(in.readWord());
        dir.setTotalEntries(in.readWord());
        dir.setCentralDirectorySize(in.readDword());
        dir.setCentralDirectoryOffs(in.readDword());
        dir.setComment(in.readString(in.readWord()));

        in.seek(offs);

        return dir;
    }

    private static long findHead(DataInput in) throws IOException {
        int commentLength = EndCentralDirectory.MAX_COMMENT_LENGTH;
        long available = in.length() - EndCentralDirectory.MIN_SIZE;

        do {
            in.seek(available--);
            commentLength--;
            long offs = in.getOffs();

            if (in.readSignature() == EndCentralDirectory.SIGNATURE)
                return offs;
        } while (commentLength >= 0 && available >= 0);

        throw new Zip4jException("zip headers not found. probably not a zip file");
    }

}
