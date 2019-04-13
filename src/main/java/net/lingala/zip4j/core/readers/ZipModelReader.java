package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.EndCentralDirectory;
import net.lingala.zip4j.model.Zip64EndCentralDirectory;
import net.lingala.zip4j.model.Zip64EndCentralDirectoryLocator;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.03.2019
 */
@RequiredArgsConstructor
public final class ZipModelReader {

    @NonNull
    private final Path zipFile;
    @NonNull
    private final Charset charset;

    @NonNull
    public ZipModel read() throws IOException {
        try (LittleEndianRandomAccessFile in = new LittleEndianRandomAccessFile(zipFile)) {
            ZipModel zipModel = read(in);

            if (zipModel.isSplitArchive()) {
                Path path = ZipModel.getSplitFilePath(zipFile, 1);
                zipModel.setSplitLength(Files.exists(path) ? Files.size(path) : zipModel.getSplitLength());
            }

            return zipModel;
        }
    }

    /**
     * Reads all the header information for the zip file. File names are read with
     * input charset name. If this parameter is null, default system charset is used.
     * <br><br><b>Note:</b> This method does not read local file header information
     *
     * @return {@link ZipModel}
     * @throws ZipException
     */
    private ZipModel read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        EndCentralDirectoryReader endCentralDirectoryReader = new EndCentralDirectoryReader();
        EndCentralDirectory dir = endCentralDirectoryReader.read(in);
        Zip64EndCentralDirectoryLocator locator = new Zip64EndCentralDirectoryLocatorReader(endCentralDirectoryReader.getOffs()).read(in);

        ZipModel zipModel = new ZipModel();
        zipModel.setZipFile(zipFile);
        zipModel.setCharset(charset);
        zipModel.setEndCentralDirectory(dir);

        if (locator != null)
            zipModel.zip64(locator, new Zip64EndCentralDirectoryReader(locator.getOffsetZip64EndOfCentralDirRec()).read(in));

        Zip64EndCentralDirectory zip64EndCentralDirectory = zipModel.isZip64() ? zipModel.getZip64().getEndCentralDirectory() : null;
        zipModel.setCentralDirectory(new CentralDirectoryReader(dir, zip64EndCentralDirectory).read(in));

        return zipModel;
    }

}
