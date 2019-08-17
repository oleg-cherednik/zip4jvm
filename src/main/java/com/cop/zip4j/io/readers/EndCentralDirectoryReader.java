package com.cop.zip4j.io.readers;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.EndCentralDirectory;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
    public EndCentralDirectory read(@NonNull DataInput in) throws IOException {
        findHead(in);

        EndCentralDirectory dir = new EndCentralDirectory();
        dir.setSplitParts(in.readWord());
        dir.setStartDiskNumber(in.readWord());
        dir.setDiskEntries(in.readWord());
        dir.setTotalEntries(in.readWord());
        dir.setSize(in.readDwordLong());
        dir.setOffs(in.readDwordLong());
        dir.setComment(in.readString(in.readWord()));

        return dir;
    }

    private void findHead(DataInput in) throws IOException {
        offs = -1;

        int commentLength = EndCentralDirectory.MAX_COMMENT_LENGTH;
        long offs = in.length() - EndCentralDirectory.MIN_SIZE;

        do {
            in.seek(offs--);
            commentLength--;
            this.offs = in.getOffs();

            if (in.readSignature() == EndCentralDirectory.SIGNATURE)
                return;
        } while (commentLength >= 0 && offs >= 0);

        this.offs = -1;

        throw new Zip4jException("zip headers not found. probably not a zip file");
    }

}
