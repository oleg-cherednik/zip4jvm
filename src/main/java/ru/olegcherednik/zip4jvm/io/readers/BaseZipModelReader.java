package ru.olegcherednik.zip4jvm.io.readers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * Start reading from the end of the file.
 *
 * <pre>
 * ...
 * [zip64 end of central directory record]
 * [zip64 end of central directory locator]
 * [end of central directory record]
 * EOF
 * </pre>
 *
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseZipModelReader {

    protected final Path zip;
    protected final Function<Charset, Charset> charsetCustomizer;

    protected EndCentralDirectory endCentralDirectory;
    protected Zip64 zip64;
    protected CentralDirectory centralDirectory;

    protected final void readData() throws IOException {
        try (DataInput in = new SingleZipInputStream(zip)) {
            findCentralDirectorySignature(in);
            endCentralDirectory = readEndCentralDirectory(in);
            zip64 = getZip64Reader().read(in);
            centralDirectory = readCentralDirectory(endCentralDirectory, zip64, in);
        }
    }

    private EndCentralDirectory readEndCentralDirectory(DataInput in) throws IOException {
        long offs = in.getOffs();

        try {
            return getEndCentralDirectoryReader().read(in);
        } finally {
            in.seek(offs);
        }
    }

    private CentralDirectory readCentralDirectory(EndCentralDirectory endCentralDirectory, Zip64 zip64, DataInput in) throws IOException {
        in.seek(ZipModelBuilder.getCentralDirectoryOffs(endCentralDirectory, zip64));
        long totalEntries = ZipModelBuilder.getTotalEntries(endCentralDirectory, zip64);
        return getCentralDirectoryReader(totalEntries).read(in);
    }

    protected abstract EndCentralDirectoryReader getEndCentralDirectoryReader();

    protected abstract Zip64Reader getZip64Reader();

    protected abstract CentralDirectoryReader getCentralDirectoryReader(long totalEntries);

    public static void findCentralDirectorySignature(DataInput in) throws IOException {
        int commentLength = ZipModel.MAX_COMMENT_SIZE;
        long available = in.length() - EndCentralDirectory.MIN_SIZE;

        do {
            in.seek(available--);
            commentLength--;

            if (in.readDwordSignature() == EndCentralDirectory.SIGNATURE) {
                in.backward(in.dwordSignatureSize());
                return;
            }
        } while (commentLength >= 0 && available >= 0);

        throw new Zip4jvmException("EndCentralDirectory was not found");
    }
}
