package ru.olegcherednik.zip4jvm.io.readers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.SignatureWasNotFoundException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.SrcFile;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
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

    private static final String MARKER_END_CENTRAL_DIRECTORY = "end_central_directory";

    protected final SrcFile srcFile;
    protected final Function<Charset, Charset> customizeCharset;

    @Getter
    protected EndCentralDirectory endCentralDirectory;
    @Getter
    protected Zip64 zip64;
    @Getter
    protected CentralDirectory centralDirectory;

    public final void readCentralData() throws IOException {
        try (DataInput in = createDataInput()) {
            findCentralDirectorySignature(in);
            endCentralDirectory = readEndCentralDirectory(in);
            zip64 = readZip64(in);
            centralDirectory = readCentralDirectory(in);
        }
    }

    private EndCentralDirectory readEndCentralDirectory(DataInput in) throws IOException {
        return getEndCentralDirectoryReader().read(in);
    }

    private Zip64 readZip64(DataInput in) throws IOException {
        in.seek(MARKER_END_CENTRAL_DIRECTORY);
        return getZip64Reader().read(in);
    }

    private CentralDirectory readCentralDirectory(DataInput in) throws IOException {
        // TODO same with ZipMode.createDataInput()
        int mainDisk = ZipModelBuilder.getMainDisk(endCentralDirectory, zip64);
        SrcFile.Item item = srcFile.getItems().get(mainDisk);
        long offs = item.getOffs() + ZipModelBuilder.getCentralDirectoryOffs(endCentralDirectory, zip64);
        in.seek(offs);
        long totalEntries = ZipModelBuilder.getTotalEntries(endCentralDirectory, zip64);
        return getCentralDirectoryReader(totalEntries).read(in);
    }

    protected abstract DataInput createDataInput() throws IOException;

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
                in.mark(MARKER_END_CENTRAL_DIRECTORY);
                return;
            }
        } while (commentLength >= 0 && available >= 0);

        throw new SignatureWasNotFoundException(EndCentralDirectory.SIGNATURE, "EndCentralDirectory");
    }

}
