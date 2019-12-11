package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.readers.BaseZipModelReader;
import ru.olegcherednik.zip4jvm.io.readers.CentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.EndCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.Zip64Reader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.BlockZipEntryModel;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.Zip64Block;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 19.10.2019
 */
public final class BlockModelReader extends BaseZipModelReader {

    private final Block endCentralDirectoryBlock = new Block();
    private final Zip64Block zip64Block = new Zip64Block();
    private final CentralDirectoryBlock centralDirectoryBlock = new CentralDirectoryBlock();

    public BlockModelReader(Path zip, Function<Charset, Charset> customizeCharset) {
        super(zip, customizeCharset);
    }

    public BlockModel read() throws IOException {
        readCentralData();

        ZipModel zipModel = new ZipModelBuilder(zip, endCentralDirectory, zip64, centralDirectory, customizeCharset).build();

        return BlockModel.builder()
                         .zipModel(zipModel)
                         .endCentralDirectory(endCentralDirectory, endCentralDirectoryBlock)
                         .zip64(zip64, zip64Block)
                         .centralDirectory(centralDirectory, centralDirectoryBlock).build();
    }

    public BlockModel readWithEntries() throws IOException {
        readCentralData();

        ZipModel zipModel = new ZipModelBuilder(zip, endCentralDirectory, zip64, centralDirectory, customizeCharset).build();
        BlockZipEntryModel zipEntryModel = new BlockZipEntryModelReader(zipModel, customizeCharset).read();

        return BlockModel.builder()
                         .zipModel(zipModel)
                         .zipEntryModel(zipEntryModel)
                         .endCentralDirectory(endCentralDirectory, endCentralDirectoryBlock)
                         .zip64(zip64, zip64Block)
                         .centralDirectory(centralDirectory, centralDirectoryBlock).build();
    }

    @Override
    protected EndCentralDirectoryReader getEndCentralDirectoryReader() {
        return new BlockEndCentralDirectoryReader(customizeCharset, endCentralDirectoryBlock);
    }

    @Override
    protected Zip64Reader getZip64Reader() {
        return new BlockZip64Reader(zip64Block);
    }

    @Override
    protected CentralDirectoryReader getCentralDirectoryReader(long totalEntries) {
        return new BlockCentralDirectoryReader(totalEntries, customizeCharset, centralDirectoryBlock);
    }
}

