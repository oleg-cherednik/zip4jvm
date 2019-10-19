package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;
import java.nio.charset.Charset;
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
        in.skip(in.signatureSize());

        EndCentralDirectory endCentralDirectory = new EndCentralDirectory();
        endCentralDirectory.setTotalDisks(in.readWord());
        endCentralDirectory.setMainDisk(in.readWord());
        endCentralDirectory.setDiskEntries(in.readWord());
        endCentralDirectory.setTotalEntries(in.readWord());
        endCentralDirectory.setCentralDirectorySize(in.readDword());
        endCentralDirectory.setCentralDirectoryOffs(in.readDword());
        int commentLength = in.readWord();
        endCentralDirectory.setComment(in.readString(commentLength, charsetCustomizer.apply(Charsets.IBM437)));
        return endCentralDirectory;
    }

}
