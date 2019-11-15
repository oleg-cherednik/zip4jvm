package ru.olegcherednik.zip4jvm;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.engine.DecomposeEngine;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockModelReader;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockZipEntryModelReader;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.BlockZipEntryModel;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.view.EndCentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.IView;
import ru.olegcherednik.zip4jvm.view.centraldirectory.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryListView;
import ru.olegcherednik.zip4jvm.view.zip64.Zip64View;

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

    public void getShortInfo(PrintStream out) throws IOException {
        Function<Charset, Charset> charsetCustomizer = charset -> Charsets.UTF_8;//Charsets.SYSTEM_CHARSET;
//        Function<Charset, Charset> charsetCustomizer = Charsets.SYSTEM_CHARSET;
        Charset charset = charsetCustomizer.apply(Charsets.IBM437);
        final String prefix = "    ";
        final int offs = prefix.length();
        final int columnWidth = 52;

        Diagnostic diagnostic = new Diagnostic();
        BlockModel blockModel = new BlockModelReader(zip, charsetCustomizer, diagnostic).read();
        BlockZipEntryModel zipEntryModel = new BlockZipEntryModelReader(blockModel.getZipModel(), charsetCustomizer,
                diagnostic.getZipEntryBlock()).read();

        boolean emptyLine = createEndCentralDirectoryView(blockModel, charset, offs, columnWidth).print(out);
        emptyLine = createZip64View(blockModel, offs, columnWidth).print(out, emptyLine);
        emptyLine = createCentralDirectoryView(blockModel, charset, offs, columnWidth).print(out, emptyLine);
        createZipEntriesView(zipEntryModel, charset, offs, columnWidth).print(out, emptyLine);
    }

    private static IView createEndCentralDirectoryView(BlockModel blockModel, Charset charset, int offs, int columnWidth) {
        return EndCentralDirectoryView.builder()
                                      .endCentralDirectory(blockModel.getEndCentralDirectory())
                                      .block(blockModel.getDiagnostic().getEndCentralDirectoryBlock())
                                      .charset(charset)
                                      .offs(offs)
                                      .columnWidth(columnWidth).build();
    }

    @SuppressWarnings("NewMethodNamingConvention")
    private static IView createZip64View(BlockModel blockModel, int offs, int columnWidth) {
        return Zip64View.builder()
                        .zip64(blockModel.getZip64())
                        .diagZip64(blockModel.getDiagnostic().getZip64())
                        .offs(offs)
                        .columnWidth(columnWidth).build();
    }

    private static IView createCentralDirectoryView(BlockModel blockModel, Charset charset, int offs, int columnWidth) {
        return CentralDirectoryView.builder()
                                   .centralDirectory(blockModel.getCentralDirectory())
                                   .diagCentralDirectory(blockModel.getDiagnostic().getCentralDirectoryBlock())
                                   .charset(charset)
                                   .offs(offs)
                                   .columnWidth(columnWidth).build();
    }

    private static IView createZipEntriesView(BlockZipEntryModel zipEntryModel, Charset charset, int offs, int columnWidth) {
        return ZipEntryListView.builder()
                               .blockZipEntryModel(zipEntryModel)
                               .charset(charset)
                               .offs(offs)
                               .columnWidth(columnWidth).build();
    }

    public void decompose(Path destDir) throws IOException {
        new DecomposeEngine(zip, destDir).decompose();
    }

}
