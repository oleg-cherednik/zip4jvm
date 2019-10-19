package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Diagnostic;
import ru.olegcherednik.zip4jvm.model.DiagnosticModel;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
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
            Diagnostic.createInstance();
            EndCentralDirectory endCentralDirectory = new EndCentralDirectoryReader(charsetCustomizer).read(in);
            Zip64 zip64 = new Zip64Reader().read(in);

            long offs = ZipModelBuilder.getCentralDirectoryOffs(endCentralDirectory, zip64);
            long totalEntries = ZipModelBuilder.getTotalEntries(endCentralDirectory, zip64);
            CentralDirectory centralDirectory = new CentralDirectoryReader(offs, totalEntries, charsetCustomizer).read(in);

            Map<String, Long> fileHeaderOffs = new HashMap<>();
            Map<String, Long> fileHeaderSize = new HashMap<>();
            Map<String, Long> fileHeaderExtraFieldOffs = new HashMap<>();
            Map<String, Long> fileHeaderExtraFieldSize = new HashMap<>();

            Diagnostic diagnostic = Diagnostic.removeInstance();
            Diagnostic.CentralDirectory diagnosticCentralDirectory = diagnostic.getCentralDirectory();

            for (CentralDirectory.FileHeader fileHeader : centralDirectory.getFileHeaders()) {
                Diagnostic.CentralDirectory.FileHeader diagnosticFileHeader = diagnosticCentralDirectory.getFileHeader(fileHeader.getFileName());
                fileHeaderOffs.put(fileHeader.getFileName(), diagnosticFileHeader.getOffs());
                fileHeaderSize.put(fileHeader.getFileName(), diagnosticFileHeader.getSize());

                if (fileHeader.getExtraField() != ExtraField.NULL) {
                    fileHeaderExtraFieldOffs.put(fileHeader.getFileName(), diagnosticFileHeader.getExtraField().getOffs());
                    fileHeaderExtraFieldSize.put(fileHeader.getFileName(), diagnosticFileHeader.getExtraField().getSize());
                }
            }

            return DiagnosticModel.builder()
                                  .endCentralDirectory(endCentralDirectory)
                                  .endCentralDirectoryOffs(diagnostic.getEndCentralDirectoryOffs())
                                  .endCentralDirectorySize(diagnostic.getEndCentralDirectorySize())

                                  .zip64(zip64)
                                  .zip64EndCentralDirectoryLocatorOffs(diagnostic.getZip64().getEndCentralDirectoryLocatorOffs())
                                  .zip64EndCentralDirectoryLocatorSize(diagnostic.getZip64().getEndCentralDirectoryLocatorSize())
                                  .zip64EndCentralDirectoryOffs(diagnostic.getZip64().getEndCentralDirectoryOffs())
                                  .zip64EndCentralDirectorySize(diagnostic.getZip64().getEndCentralDirectorySize())

                                  .fileHeaderOffs(fileHeaderOffs)
                                  .fileHeaderSize(fileHeaderSize)

                                  .fileHeaderExtraFieldOffs(fileHeaderExtraFieldOffs)
                                  .fileHeaderExtraFieldSize(fileHeaderExtraFieldSize)

                                  .digitalSignatureOffs(diagnostic.getCentralDirectory().getDigitalSignatureOffs())
                                  .digitalSignatureSize(diagnostic.getCentralDirectory().getDigitalSignatureSize())

                                  .centralDirectory(centralDirectory)
                                  .centralDirectoryOffs(diagnostic.getCentralDirectory().getOffs())
                                  .centralDirectorySize(diagnostic.getCentralDirectory().getSize())
                                  .centralDirectory(centralDirectory).build();
        }
    }

}
