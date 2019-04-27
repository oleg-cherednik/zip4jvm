package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.model.Zip64;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.utils.LittleEndianRandomAccessFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Start reading from the end of the file.
 *
 * <pre>
 * ...
 * [zip64 end of central directory record]
 * [zip64 end of central directory locator]
 * [end of central directory record]
 * EOF
 * </pre>
 *
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

    private ZipModel read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        EndCentralDirectoryReader reader = new EndCentralDirectoryReader();

        ZipModel zipModel = new ZipModel();
        zipModel.setZipFile(zipFile);
        zipModel.setCharset(charset);
        zipModel.setEndCentralDirectory(reader.read(in));

        Zip64.EndCentralDirectoryLocator locator = new Zip64EndCentralDirectoryLocatorReader(reader.getOffs()).read(in);

        if (locator != null)
            zipModel.zip64(locator, new Zip64EndCentralDirectoryReader(locator.getOffs()).read(in));

        long offs = zipModel.getCentralDirectoryOffs();
        long totalEntries = zipModel.getTotalEntries();
        zipModel.setCentralDirectory(new CentralDirectoryReader(offs, totalEntries).read(in));

        return zipModel;
    }

}
