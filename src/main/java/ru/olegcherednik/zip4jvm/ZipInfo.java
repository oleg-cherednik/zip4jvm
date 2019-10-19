package ru.olegcherednik.zip4jvm;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.readers.DiagnosticModelReader;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.DiagnosticModel;
import ru.olegcherednik.zip4jvm.view.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.EndCentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.Zip64View;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Function;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireExists;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireRegularFile;

/**
 * @author Oleg Cherednik
 * @since 11.10.2019
 */
@RequiredArgsConstructor
public final class ZipInfo {

    private final Path zip;

    public static ZipInfo zip(Path zip) {
        requireNotNull(zip, "ZipInfo.zip");
        requireExists(zip);
        requireRegularFile(zip, "ZipInfo.zip");

        return new ZipInfo(zip);
    }

    public void getShortInfo() throws IOException {
        Function<Charset, Charset> charsetCustomizer = charset -> Charsets.UTF_8;//Charsets.SYSTEM_CHARSET;
//        Function<Charset, Charset> charsetCustomizer = Charsets.SYSTEM_CHARSET;
        Charset charset = charsetCustomizer.apply(Charsets.IBM437);
        final String prefix = "    ";
        DiagnosticModel diagnosticModel = new DiagnosticModelReader(zip, charsetCustomizer).read();

        PrintStream out = System.out;

        EndCentralDirectoryView.builder()
                               .offs(diagnosticModel.getDiagnostic().getEndCentralDirectoryOffs())
                               .size(diagnosticModel.getDiagnostic().getEndCentralDirectorySize())
                               .dir(diagnosticModel.getEndCentralDirectory())
                               .charset(charset)
                               .prefix(prefix).build().print(out);

        out.println();

        Zip64View.builder()
                 .zip64(diagnosticModel.getZip64())
                 .diagZip64(diagnosticModel.getDiagnostic().getZip64())
                 .charset(charset)
                 .prefix(prefix).build().print(out);

        out.println();

        CentralDirectoryView.builder()
                            .centralDirectory(diagnosticModel.getCentralDirectory())
                            .diagCentralDirectory(diagnosticModel.getDiagnostic().getCentralDirectory())
                            .charset(charset)
                            .prefix(prefix).build().print(out);
    }

}
