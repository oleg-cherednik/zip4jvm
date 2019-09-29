package ru.olegcherednik.zip4jvm.model.builders;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 27.08.2019
 */
@RequiredArgsConstructor
public final class CentralDirectoryBuilder {

    private final Collection<ZipEntry> entries;

    public CentralDirectory create() throws IOException {
        CentralDirectory centralDirectory = new CentralDirectory();
        centralDirectory.setFileHeaders(createFileHeaders());
        centralDirectory.setDigitalSignature(null);
        return centralDirectory;
    }

    private List<CentralDirectory.FileHeader> createFileHeaders() throws IOException {
        List<CentralDirectory.FileHeader> fileHeaders = new ArrayList<>(entries.size());

        for (ZipEntry entry : entries)
            fileHeaders.add(new FileHeaderBuilder(entry).create());

        return fileHeaders;
    }

}
