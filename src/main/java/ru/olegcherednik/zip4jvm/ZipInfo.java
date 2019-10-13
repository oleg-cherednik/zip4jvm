package ru.olegcherednik.zip4jvm;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.readers.ZipModelReader;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.DiagnosticModel;
import ru.olegcherednik.zip4jvm.view.EndCentralDirectoryView;

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
        DiagnosticModel diagnosticModel = new ZipModelReader(zip, charsetCustomizer).readDiagnostic();


        EndCentralDirectoryView view = EndCentralDirectoryView.builder()
                                                              .offs(diagnosticModel.getEndCentralDirectoryOffs())
                                                              .size(diagnosticModel.getEndCentralDirectorySize())
                                                              .charset(charsetCustomizer.apply(Charsets.IBM437))
                                                              .endCentralDirectory(diagnosticModel.getEndCentralDirectory()).build();

        view.print(System.out);
        int a = 0;
        a++;
    }

}
