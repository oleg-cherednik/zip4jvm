package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

import static ru.olegcherednik.zip4jvm.io.readers.ZipModelReader.MARK_END_CENTRAL_DIRECTORY_END_OFFS;
import static ru.olegcherednik.zip4jvm.io.readers.ZipModelReader.MARK_END_CENTRAL_DIRECTORY_OFFS;

/**
 * @author Oleg Cherednik
 * @since 04.03.2019
 */
@RequiredArgsConstructor
final class EndCentralDirectoryReader implements Reader<EndCentralDirectory> {

    private final Function<Charset, Charset> charsetCustomizer;

    @Override
    public EndCentralDirectory read(DataInput in) throws IOException {
        findHead(in);

        EndCentralDirectory dir = new EndCentralDirectory();
        dir.setTotalDisks(in.readWord());
        dir.setMainDisk(in.readWord());
        dir.setDiskEntries(in.readWord());
        dir.setTotalEntries(in.readWord());
        dir.setCentralDirectorySize(in.readDword());
        dir.setCentralDirectoryOffs(in.readDword());
        int commentLength = in.readWord();
        dir.setComment(in.readString(commentLength, charsetCustomizer.apply(Charsets.IBM437)));

        in.mark(MARK_END_CENTRAL_DIRECTORY_END_OFFS);
        in.seek(MARK_END_CENTRAL_DIRECTORY_OFFS);

        return dir;
    }

    private static void findHead(DataInput in) throws IOException {
        int commentLength = ZipModel.MAX_COMMENT_SIZE;
        long available = in.length() - EndCentralDirectory.MIN_SIZE;

        do {
            in.seek(available--);
            commentLength--;
            in.mark(MARK_END_CENTRAL_DIRECTORY_OFFS);

            if (in.readSignature() == EndCentralDirectory.SIGNATURE)
                return;
        } while (commentLength >= 0 && available >= 0);

        throw new Zip4jvmException("EncCentralDirectory was not found");
    }

}
