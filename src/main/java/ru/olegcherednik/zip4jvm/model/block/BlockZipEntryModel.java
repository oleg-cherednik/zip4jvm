package ru.olegcherednik.zip4jvm.model.block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@Getter
@RequiredArgsConstructor
public final class BlockZipEntryModel {

    private final ZipEntryBlock zipEntryBlock;
    private final Map<String, Data> fileNameData = new LinkedHashMap<>();

    public Set<String> getFileNames() {
        return fileNameData.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(fileNameData.keySet());
    }

    public void addLocalFileHeader(LocalFileHeader localFileHeader) {
        String fileName = localFileHeader.getFileName();
        fileNameData.computeIfAbsent(fileName, Data::new);
        fileNameData.get(fileName).setLocalFileHeader(localFileHeader);
    }

    public void addDataDescriptor(String fileName, DataDescriptor dataDescriptor) {
        fileNameData.computeIfAbsent(fileName, Data::new);
        fileNameData.get(fileName).setDataDescriptor(dataDescriptor);
    }

    public LocalFileHeader getLocalFileHeader(String fileName) {
        return fileNameData.get(fileName).getLocalFileHeader();
    }

    public DataDescriptor getDataDescriptor(String fileName) {
        return fileNameData.get(fileName).getDataDescriptor();
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static final class Data {

        private final String fileName;
        private LocalFileHeader localFileHeader;
        private DataDescriptor dataDescriptor;

    }

}
