package ru.olegcherednik.zip4jvm.io.readers.diagnostic;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.readers.ZipModelReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.diagnostic.Diagnostic;
import ru.olegcherednik.zip4jvm.model.diagnostic.DiagnosticModel;

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

            EndCentralDirectory endCentralDirectory = readEndCentralDirectory(in);
            Zip64 zip64 = ZipModelReader.readZip64(in);
            CentralDirectory centralDirectory = readCentralDirectory(endCentralDirectory, zip64, in);

            return DiagnosticModel.builder()
                                  .diagnostic(Diagnostic.removeInstance())
                                  .endCentralDirectory(endCentralDirectory)
                                  .zip64(zip64)
                                  .centralDirectory(centralDirectory).build();
        }
    }

    private EndCentralDirectory readEndCentralDirectory(DataInput in) throws IOException {
        ZipModelReader.findCentralDirectorySignature(in);
        long offs = in.getOffs();

        try {
            return new EndCentralDirectoryReaderB(charsetCustomizer).read(in);
        } finally {
            in.seek(offs);
        }
    }

    private CentralDirectory readCentralDirectory(EndCentralDirectory endCentralDirectory, Zip64 zip64, DataInput in) throws IOException {
        in.seek(ZipModelBuilder.getCentralDirectoryOffs(endCentralDirectory, zip64));
        long totalEntries = ZipModelBuilder.getTotalEntries(endCentralDirectory, zip64);
        return new CentralDirectoryReaderB(totalEntries, charsetCustomizer).read(in);
    }

}
