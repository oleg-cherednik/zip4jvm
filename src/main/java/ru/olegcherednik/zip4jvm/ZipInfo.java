package ru.olegcherednik.zip4jvm;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.engine.DecomposeEngine;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockModelReader;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.view.EndCentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.IView;
import ru.olegcherednik.zip4jvm.view.Zip64View;
import ru.olegcherednik.zip4jvm.view.centraldirectory.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryListView;

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
        Charset charset = charsetCustomizer.apply(Charsets.IBM437);
        final int offs = 4;
        final int columnWidth = 52;

        BlockModel blockModel = new BlockModelReader(zip, charsetCustomizer).readWithEntries();

        boolean emptyLine = createEndCentralDirectoryView(blockModel, charset, offs, columnWidth).print(out);
        emptyLine = createZip64View(blockModel, offs, columnWidth).print(out, emptyLine);
        emptyLine = createCentralDirectoryView(blockModel, charset, offs, columnWidth).print(out, emptyLine);
        createZipEntriesView(blockModel, charset, offs, columnWidth).print(out, emptyLine);
    }

    private static IView createEndCentralDirectoryView(BlockModel blockModel, Charset charset, int offs, int columnWidth) {
        return EndCentralDirectoryView.builder()
                                      .endCentralDirectory(blockModel.getEndCentralDirectory())
                                      .block(blockModel.getEndCentralDirectoryBlock())
                                      .charset(charset)
                                      .offs(offs)
                                      .columnWidth(columnWidth).build();
    }

    @SuppressWarnings("NewMethodNamingConvention")
    private static IView createZip64View(BlockModel blockModel, int offs, int columnWidth) {
        return Zip64View.builder()
                        .zip64(blockModel.getZip64())
                        .block(blockModel.getZip64Block())
                        .offs(offs)
                        .columnWidth(columnWidth).build();
    }

    private static IView createCentralDirectoryView(BlockModel blockModel, Charset charset, int offs, int columnWidth) {
        return CentralDirectoryView.builder()
                                   .centralDirectory(blockModel.getCentralDirectory())
                                   .diagCentralDirectory(blockModel.getCentralDirectoryBlock())
                                   .charset(charset)
                                   .offs(offs)
                                   .columnWidth(columnWidth).build();
    }

    private static IView createZipEntriesView(BlockModel blockModel, Charset charset, int offs, int columnWidth) {
        return ZipEntryListView.builder()
                               .blockZipEntryModel(blockModel.getZipEntryModel())
                               .getDataFunc(getDataFunc(blockModel))
                               .charset(charset)
                               .offs(offs)
                               .columnWidth(columnWidth).build();
    }

    private static Function<Block, byte[]> getDataFunc(BlockModel blockModel) {
        return block -> {
            if (block.getSize() > Integer.MAX_VALUE)
                return ArrayUtils.EMPTY_BYTE_ARRAY;

            try (DataInput in = new SingleZipInputStream(blockModel.getZipModel().getFile())) {
                in.skip(block.getOffs());
                return in.readBytes((int)block.getSize());
            } catch(Exception e) {
                e.printStackTrace();
                return ArrayUtils.EMPTY_BYTE_ARRAY;
            }
        };
    }

    public void decompose(Path destDir) throws IOException {
        new DecomposeEngine(zip, destDir, Charsets.UTF_8, 4, 52).decompose();
    }

}
