package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Diagnostic;
import ru.olegcherednik.zip4jvm.model.DiagnosticModel;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 19.10.2019
 */
@RequiredArgsConstructor
public final class DiagnosticModelReader {

    private final Path zip;
    private final Function<Charset, Charset> charsetCustomizer;

    public DiagnosticModel read() throws IOException {
        try (DataInput in = new SingleZipInputStream(zip)) {
            Diagnostic.createInstance();
            EndCentralDirectory endCentralDirectory = new EndCentralDirectoryReader(charsetCustomizer).read(in);
            Zip64 zip64 = new Zip64Reader().read(in);

            long offs = ZipModelBuilder.getCentralDirectoryOffs(endCentralDirectory, zip64);
            long totalEntries = ZipModelBuilder.getTotalEntries(endCentralDirectory, zip64);
            CentralDirectory centralDirectory = new CentralDirectoryReader(offs, totalEntries, charsetCustomizer).read(in);

            Diagnostic diagnostic = Diagnostic.removeInstance();

            return DiagnosticModel.builder()
                                  .diagnostic(diagnostic)
                                  .endCentralDirectory(endCentralDirectory)
                                  .zip64(zip64)
                                  .centralDirectory(centralDirectory).build();
        }
    }
}
