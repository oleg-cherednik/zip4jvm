package com.cop.zip4j.io.writers;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.builders.CentralDirectoryBuilder;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 27.08.2019
 */
@RequiredArgsConstructor
class LocalCentralDirectoryBuilder {

    private final List<PathZipEntry> entries;

    public CentralDirectory create() throws IOException {
        CentralDirectory centralDirectory = new CentralDirectory();
        centralDirectory.setFileHeaders(createFileHeaders());
        centralDirectory.setDigitalSignature(null);
        return centralDirectory;
    }

    private List<CentralDirectory.FileHeader> createFileHeaders() throws IOException {
        List<CentralDirectory.FileHeader> fileHeaders = new ArrayList<>(entries.size());

        for (PathZipEntry entry : entries) {
            CentralDirectory.FileHeader fileHeader = new CentralDirectoryBuilder(entry).create();
            fileHeader.setCrc32(fileHeader.getEncryption().getChecksum().apply(fileHeader));
            fileHeaders.add(fileHeader);
        }

        return fileHeaders;
    }
}
