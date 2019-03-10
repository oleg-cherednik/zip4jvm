package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.EndCentralDirectory;
import net.lingala.zip4j.model.Zip64EndCentralDirectoryLocator;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
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
        try (LittleEndianRandomAccessFile in = new LittleEndianRandomAccessFile(
                new RandomAccessFile(zipFile.toFile(), InternalZipConstants.READ_MODE))) {
            return read(in);
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
    @NonNull
    private ZipModel read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        EndCentralDirectoryReader endCentralDirectoryReader = new EndCentralDirectoryReader(in);
        EndCentralDirectory dir = endCentralDirectoryReader.read();
        Zip64EndCentralDirectoryLocator locator = new Zip64EndCentralDirectoryLocatorReader(in, endCentralDirectoryReader.getOffs()).read();

        ZipModel zipModel = new ZipModel();
        zipModel.setZipFile(zipFile);
        zipModel.setCharset(charset);
        zipModel.setEndCentralDirectory(dir);

        if (locator != null) {
            zipModel.setZip64EndCentralDirectoryLocator(locator);
            zipModel.setZip64EndCentralDirectory(new Zip64EndCentralDirectoryReader(in, locator.getOffsetZip64EndOfCentralDirRec()).read());
        }

        zipModel.setCentralDirectory(new CentralDirectoryReader(in,
                dir, zipModel.getZip64EndCentralDirectory(), zipModel.isZip64Format()).read());

        return zipModel;
    }

}
