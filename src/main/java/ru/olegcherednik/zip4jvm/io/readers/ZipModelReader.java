package ru.olegcherednik.zip4jvm.io.readers;

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
 * @since 06.03.2019
 */
@RequiredArgsConstructor
public final class ZipModelReader {

    private final Path zip;
    private final Function<Charset, Charset> charsetCustomizer;

    public ZipModel read() throws IOException {
        try (DataInput in = new SingleZipInputStream(zip)) {
            EndCentralDirectory endCentralDirectory = readEndCentralDirectory(in);
            Zip64 zip64 = readZip64(in);
            CentralDirectory centralDirectory = readCentralDirectory(endCentralDirectory, zip64, in);

            return new ZipModelBuilder(zip, endCentralDirectory, zip64, centralDirectory, charsetCustomizer).build();
        }
    }

    private EndCentralDirectory readEndCentralDirectory(DataInput in) throws IOException {
        findCentralDirectorySignature(in);
        long offs = in.getOffs();

        try {
            return new EndCentralDirectoryReader(charsetCustomizer).read(in);
        } finally {
            in.seek(offs);
        }
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static Zip64 readZip64(DataInput in) throws IOException {
        return new Zip64Reader().read(in);
    }

    private CentralDirectory readCentralDirectory(EndCentralDirectory endCentralDirectory, Zip64 zip64, DataInput in) throws IOException {
        in.seek(ZipModelBuilder.getCentralDirectoryOffs(endCentralDirectory, zip64));
        long totalEntries = ZipModelBuilder.getTotalEntries(endCentralDirectory, zip64);
        return new CentralDirectoryReader(totalEntries, charsetCustomizer).read(in);
    }

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
