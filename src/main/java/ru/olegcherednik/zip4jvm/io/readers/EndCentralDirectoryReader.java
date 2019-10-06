package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 04.03.2019
 */
@RequiredArgsConstructor
final class EndCentralDirectoryReader implements Reader<EndCentralDirectory> {

    private final Function<Charset, Charset> charsetCustomizer;

    @Override
    public EndCentralDirectory read(DataInput in) throws IOException {
        long offs = findHead(in);

        EndCentralDirectory dir = new EndCentralDirectory();
        dir.setTotalDisks(in.readWord());
        dir.setMainDisk(in.readWord());
        dir.setDiskEntries(in.readWord());
        dir.setTotalEntries(in.readWord());
        dir.setCentralDirectorySize(in.readDword());
        dir.setCentralDirectoryOffs(in.readDword());
        int commentLength = in.readWord();
        dir.setComment(in.readString(commentLength, charsetCustomizer.apply(StandardCharsets.UTF_8)));

        in.seek(offs);

        return dir;
    }

    private static long findHead(DataInput in) throws IOException {
        int commentLength = ZipModel.MAX_COMMENT_SIZE;
        long available = in.length() - EndCentralDirectory.MIN_SIZE;

        do {
            in.seek(available--);
            commentLength--;
            long offs = in.getOffs();

            if (in.readSignature() == EndCentralDirectory.SIGNATURE)
                return offs;
        } while (commentLength >= 0 && available >= 0);

        throw new Zip4jvmException("zip headers not found. probably not a zip file");
    }

}
