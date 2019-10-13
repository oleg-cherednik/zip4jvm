package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.DiagnosticModel;
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
@SuppressWarnings("FieldNamingConvention")
@RequiredArgsConstructor
public final class ZipModelReader {

    public static final String MARK_END_CENTRAL_DIRECTORY_OFFS = "endCentralDirectoryOffs";
    public static final String MARK_END_CENTRAL_DIRECTORY_END_OFFS = "endCentralDirectoryEndOffs";
    public static final String MARK_ZIP64_END_CENTRAL_DIRECTORY_LOCATOR_OFFS = "zip64EndCentralDirectoryLocatorOffs";
    public static final String MARK_ZIP64_END_CENTRAL_DIRECTORY_LOCATOR_END_OFFS = "zip64EndCentralDirectoryLocatorEndOffs";
    public static final String MARK_ZIP64_END_CENTRAL_DIRECTORY_OFFS = "zip64EndCentralDirectoryOffs";
    public static final String MARK_ZIP64_END_CENTRAL_DIRECTORY_END_OFFS = "zip64EndCentralDirectoryEndOffs";

    private final Path zip;
    private final Function<Charset, Charset> charsetCustomizer;

    public ZipModel read() throws IOException {
        try (DataInput in = new SingleZipInputStream(zip)) {
            EndCentralDirectory endCentralDirectory = new EndCentralDirectoryReader(charsetCustomizer).read(in);
            Zip64 zip64 = new Zip64Reader().read(in);

            long offs = ZipModelBuilder.getCentralDirectoryOffs(endCentralDirectory, zip64);
            long totalEntries = ZipModelBuilder.getTotalEntries(endCentralDirectory, zip64);
            CentralDirectory centralDirectory = new CentralDirectoryReader(offs, totalEntries, charsetCustomizer).read(in);

            return new ZipModelBuilder(zip, endCentralDirectory, zip64, centralDirectory, charsetCustomizer).build();
        }
    }

    public DiagnosticModel readDiagnostic() throws IOException {
        try (DataInput in = new SingleZipInputStream(zip)) {
            EndCentralDirectory endCentralDirectory = new EndCentralDirectoryReader(charsetCustomizer).read(in);
            Zip64 zip64 = new Zip64Reader().read(in);

            long offs = ZipModelBuilder.getCentralDirectoryOffs(endCentralDirectory, zip64);
            long totalEntries = ZipModelBuilder.getTotalEntries(endCentralDirectory, zip64);
            CentralDirectory centralDirectory = new CentralDirectoryReader(offs, totalEntries, charsetCustomizer).read(in);

            return DiagnosticModel.builder()
                                  .endCentralDirectory(endCentralDirectory)
                                  .endCentralDirectoryOffs(in.getMark(MARK_END_CENTRAL_DIRECTORY_OFFS))
                                  .endCentralDirectorySize(
                                          in.getMark(MARK_END_CENTRAL_DIRECTORY_END_OFFS) - in.getMark(MARK_END_CENTRAL_DIRECTORY_OFFS))

                                  .zip64(zip64)
                                  .zip64EndCentralDirectoryLocatorOffs(in.getMark(MARK_ZIP64_END_CENTRAL_DIRECTORY_LOCATOR_OFFS))
                                  .zip64EndCentralDirectoryLocatorSize(in.getMark(MARK_ZIP64_END_CENTRAL_DIRECTORY_LOCATOR_END_OFFS) -
                                          in.getMark(MARK_ZIP64_END_CENTRAL_DIRECTORY_LOCATOR_OFFS))
                                  .zip64EndCentralDirectoryOffs(in.getMark(MARK_ZIP64_END_CENTRAL_DIRECTORY_OFFS))
                                  .zip64EndCentralDirectorySize(in.getMark(MARK_ZIP64_END_CENTRAL_DIRECTORY_END_OFFS) -
                                          in.getMark(MARK_ZIP64_END_CENTRAL_DIRECTORY_OFFS))
                                  .centralDirectory(centralDirectory).build();
        }
    }

}
