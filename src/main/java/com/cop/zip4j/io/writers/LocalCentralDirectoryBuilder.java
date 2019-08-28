package com.cop.zip4j.io.writers;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.builders.CentralDirectoryBuilder;
import com.cop.zip4j.model.entry.PathZipEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 27.08.2019
 */
class LocalCentralDirectoryBuilder {

    public CentralDirectory create(List<PathZipEntry> entries, ZipModel zipModel) throws IOException {
        CentralDirectory centralDirectory = new CentralDirectory();
        centralDirectory.setFileHeaders(getFileHeaders(entries, zipModel));
        centralDirectory.setDigitalSignature(null);
        return centralDirectory;
    }

    private List<CentralDirectory.FileHeader> getFileHeaders(List<PathZipEntry> entries, ZipModel zipModel) throws IOException {
        List<CentralDirectory.FileHeader> fileHeaders = new ArrayList<>(entries.size());

        for (PathZipEntry entry : entries) {
            CentralDirectory.FileHeader fileHeader = new CentralDirectoryBuilder(entry, zipModel, entry.getDisc()).create();
            fileHeader.setCrc32(fileHeader.getEncryption().getChecksum().apply(fileHeader));
            fileHeaders.add(fileHeader);
        }
        return fileHeaders;
    }
}
