package ru.olegcherednik.zip4jvm.io.readers;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Oleg Cherednik
 * @since 04.03.2019
 */
final class EndCentralDirectoryReader implements Reader<EndCentralDirectory> {

    @NonNull
    @Override
    public EndCentralDirectory read(@NonNull DataInput in) throws IOException {
        long offs = findHead(in);

        EndCentralDirectory dir = new EndCentralDirectory();
        dir.setTotalDisks(in.readWord());
        dir.setMainDisk(in.readWord());
        dir.setDiskEntries(in.readWord());
        dir.setTotalEntries(in.readWord());
        dir.setCentralDirectorySize(in.readDword());
        dir.setCentralDirectoryOffs(in.readDword());
        int commentLength = in.readWord();
        dir.setComment(in.readString(commentLength, StandardCharsets.UTF_8));

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
