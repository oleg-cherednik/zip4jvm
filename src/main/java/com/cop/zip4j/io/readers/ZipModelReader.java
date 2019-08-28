package com.cop.zip4j.io.readers;

import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.io.in.LittleEndianReadFile;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.entry.FileHeaderPathZipEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
        try (LittleEndianReadFile in = new LittleEndianReadFile(zipFile)) {
            ZipModel zipModel = read(in);

            if (zipModel.isSplitArchive()) {
                Path path = ZipModel.getSplitFilePath(zipFile, 1);
                // TODO have to check all parts and get the max size
                zipModel.setSplitLength(Files.exists(path) ? Files.size(path) : zipModel.getSplitLength());
            }

            return zipModel;
        }
    }

    private ZipModel read(@NonNull DataInput in) throws IOException {
        ZipModel zipModel = new ZipModel(zipFile, charset);
        zipModel.setEndCentralDirectory(new EndCentralDirectoryReader().read(in));
        zipModel.setZip64(new Zip64Reader().read(in));

        long offs = zipModel.getCentralDirectoryOffs();
        long totalEntries = zipModel.getActivity().getTotalEntries(zipModel);
        CentralDirectory centralDirectory = new CentralDirectoryReader(offs, totalEntries).read(in);

        createEntries(zipModel, centralDirectory);

        return zipModel;
    }

    private static void createEntries(ZipModel zipModel, CentralDirectory centralDirectory) {
        if (!zipModel.getEntries().isEmpty())
            return;

        centralDirectory.getFileHeaders().forEach(fileHeader -> {
            zipModel.getEntries().add(new FileHeaderPathZipEntry(fileHeader));
        });
    }

}
