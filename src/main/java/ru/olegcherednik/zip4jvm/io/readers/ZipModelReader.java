package ru.olegcherednik.zip4jvm.io.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.LittleEndianReadFile;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;

import java.io.IOException;
import java.nio.file.Path;

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

    @NonNull
    private final Path zip;

    @NonNull
    public ZipModel read() throws IOException {
        try (LittleEndianReadFile in = new LittleEndianReadFile(zip)) {
            EndCentralDirectory endCentralDirectory = new EndCentralDirectoryReader().read(in);
            Zip64 zip64 = new Zip64Reader().read(in);

            long offs = ZipModelBuilder.getCentralDirectoryOffs(endCentralDirectory, zip64);
            long totalEntries = ZipModelBuilder.getTotalEntries(endCentralDirectory, zip64);
            CentralDirectory centralDirectory = new CentralDirectoryReader(offs, totalEntries).read(in);

            return new ZipModelBuilder(zip, endCentralDirectory, zip64, centralDirectory).create();
        }
    }

}
