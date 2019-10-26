package ru.olegcherednik.zip4jvm;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockModelReader;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockZipEntryModelReader;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.BlockZipEntryModel;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.view.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.EndCentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.Zip64View;
import ru.olegcherednik.zip4jvm.view.ZipEntryView;

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

        Diagnostic diagnostic = new Diagnostic();
        BlockModel blockModel = new BlockModelReader(zip, charsetCustomizer, diagnostic).read();
        BlockZipEntryModel zipEntryModel = new BlockZipEntryModelReader(blockModel.getZipModel(), charsetCustomizer,
                diagnostic.getZipEntryBlock()).read();

        PrintStream out = System.out;

        printEndCentralDirectoryView(blockModel, charset, prefix, out);
        out.println();
        printZip64View(blockModel, charset, prefix, out);
        out.println();
        printCentralDirectory(blockModel, charset, prefix, out);
        out.println();
        printZipEntries(zipEntryModel, charset, prefix, out);
    }

    private static void printEndCentralDirectoryView(BlockModel blockModel, Charset charset, String prefix, PrintStream out) {
        EndCentralDirectoryView.builder()
                               .block(blockModel.getDiagnostic().getEndCentralDirectory())
                               .dir(blockModel.getEndCentralDirectory())
                               .charset(charset)
                               .prefix(prefix).build().print(out);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    private static void printZip64View(BlockModel blockModel, Charset charset, String prefix, PrintStream out) {
        Zip64View.builder()
                 .zip64(blockModel.getZip64())
                 .diagZip64(blockModel.getDiagnostic().getZip64())
                 .charset(charset)
                 .prefix(prefix).build().print(out);
    }

    private static void printCentralDirectory(BlockModel blockModel, Charset charset, String prefix, PrintStream out) {
        CentralDirectoryView.builder()
                            .centralDirectory(blockModel.getCentralDirectory())
                            .diagCentralDirectory(blockModel.getDiagnostic().getCentralDirectory())
                            .charset(charset)
                            .prefix(prefix).build().print(out);
    }

    private static void printZipEntries(BlockZipEntryModel zipEntryModel, Charset charset, String prefix, PrintStream out) throws IOException {
        ZipEntryView.builder()
                    .blockZipEntryModel(zipEntryModel)
                    .charset(charset)
                    .prefix(prefix).build().print(out);
    }

    public void decompose(Path path) {
        int a = 0;
        a++;
    }

}
