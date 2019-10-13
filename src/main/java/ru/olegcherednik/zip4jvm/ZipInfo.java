package ru.olegcherednik.zip4jvm;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.readers.ZipModelReader;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.DiagnosticModel;
import ru.olegcherednik.zip4jvm.view.EndCentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.Zip64View;

import java.io.IOException;
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
//        Function<Charset, Charset> charsetCustomizer = charset -> Charsets.UTF_8;//Charsets.SYSTEM_CHARSET;
        Function<Charset, Charset> charsetCustomizer = Charsets.SYSTEM_CHARSET;
        Charset charset = charsetCustomizer.apply(Charsets.IBM437);
        DiagnosticModel diagnosticModel = new ZipModelReader(zip, charsetCustomizer).readDiagnostic();


        EndCentralDirectoryView.builder()
                               .offs(diagnosticModel.getEndCentralDirectoryOffs())
                               .size(diagnosticModel.getEndCentralDirectorySize())
                               .charset(charset)
                               .endCentralDirectory(diagnosticModel.getEndCentralDirectory())
                               .prefix("    ").build().print(System.out);

        System.out.println();

        Zip64View.EndCentralDirectoryLocator.builder()
                                            .offs(diagnosticModel.getZip64EndCentralDirectoryLocatorOffs())
                                            .size(diagnosticModel.getZip64EndCentralDirectoryLocatorSize())
                                            .charset(charset)
                                            .locator(diagnosticModel.getZip64().getEndCentralDirectoryLocator())
                                            .prefix("    ").build().print(System.out);
        int a = 0;
        a++;
    }

}
