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
    private final Map<String, Data> content = new LinkedHashMap<>();

    public Set<String> getFileNames() {
        return content.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(content.keySet());
    }

    public void addLocalFileHeader(LocalFileHeader localFileHeader) {
        String fileName = localFileHeader.getFileName();
        content.computeIfAbsent(fileName, Data::new);
        content.get(fileName).setLocalFileHeader(localFileHeader);
    }

    public void addDataDescriptor(String fileName, DataDescriptor dataDescriptor) {
        content.computeIfAbsent(fileName, Data::new);
        content.get(fileName).setDataDescriptor(dataDescriptor);
    }

    public LocalFileHeader getLocalFileHeader(String fileName) {
        return content.get(fileName).getLocalFileHeader();
    }

    public DataDescriptor getDataDescriptor(String fileName) {
        return content.get(fileName).getDataDescriptor();
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
