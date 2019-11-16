package ru.olegcherednik.zip4jvm.model.block;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 19.10.2019
 */
@Getter
@Setter
public final class Diagnostic {

    private final Block endCentralDirectoryBlock = new Block();
    private final Zip64Block zip64Block = new Zip64Block();
    private final CentralDirectoryBlock centralDirectoryBlock = new CentralDirectoryBlock();
    private final ZipEntryBlock zipEntryBlock = new ZipEntryBlock();

    @Getter
    @Setter
    public static final class ZipEntryBlock {

        private final Map<String, LocalFileHeaderBlock> localFileHeaders = new LinkedHashMap<>();
        private final Map<String, EncryptionHeader> encryptionHeaders = new LinkedHashMap<>();
        private final Map<String, ByteArrayBlock> dataDescriptors = new LinkedHashMap<>();

        @Setter(AccessLevel.NONE)
        private LocalFileHeaderBlock localFileHeader;

        public void addLocalFileHeader() {
            localFileHeader = new LocalFileHeaderBlock();
        }

        public void saveLocalFileHeader(String fileName) {
            localFileHeaders.put(fileName, localFileHeader);
            localFileHeader = null;
        }

        public void saveEncryptionHeader(String fileName, EncryptionHeader encryptionHeader) {
            encryptionHeaders.put(fileName, encryptionHeader);
        }

        public void saveDataDescriptor(String fileName, ByteArrayBlock block) {
            dataDescriptors.put(fileName, block);
        }

        public LocalFileHeaderBlock getLocalFileHeader(String fileName) {
            return localFileHeaders.get(fileName);
        }

        public EncryptionHeader getEncryptionHeader(String fileName) {
            return encryptionHeaders.get(fileName);
        }

        public ByteArrayBlock getDataDescriptor(String fileName) {
            return dataDescriptors.get(fileName);
        }

        @Getter
        @Setter
        public static final class LocalFileHeaderBlock {

            private final ByteArrayBlock content = new ByteArrayBlock();
            private final ExtraFieldBlock extraFieldBlock = new ExtraFieldBlock();

            private long disk;
        }

        public interface EncryptionHeader {

        }

    }

}
