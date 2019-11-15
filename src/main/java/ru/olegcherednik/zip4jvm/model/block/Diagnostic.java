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
    private final Zip64 zip64 = new Zip64();
    private final CentralDirectoryBlock centralDirectoryBlock = new CentralDirectoryBlock();
    private final ZipEntryBlock zipEntryBlock = new ZipEntryBlock();

    @Getter
    @Setter
    public static final class Zip64 {

        private final Block endCentralDirectoryLocator = new Block();
        private final Block endCentralDirectory = new Block();

    }

    @Getter
    @Setter
    public static final class ZipEntryBlock {

        public static final ZipEntry NULL = new ZipEntry();

        private final Map<String, LocalFileHeaderB> localFileHeaders = new LinkedHashMap<>();
        private final Map<String, EncryptionHeader> encryptionHeaders = new LinkedHashMap<>();
        private final Map<String, ByteArrayBlock> dataDescriptors = new LinkedHashMap<>();

        @Setter(AccessLevel.NONE)
        private LocalFileHeaderB localFileHeader;

        public void addLocalFileHeader() {
            localFileHeader = new LocalFileHeaderB();
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

        public LocalFileHeaderB getLocalFileHeader(String fileName) {
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
        public static final class LocalFileHeader extends Block {

            public static final LocalFileHeader NULL = new LocalFileHeader();

            private final ExtraFieldBlock extraFields = new ExtraFieldBlock();

            private long disk;

        }

        @Getter
        @Setter
        public static final class LocalFileHeaderB {

            private final ByteArrayBlock content = new ByteArrayBlock();
            private final ExtraFieldBlock extraField = new ExtraFieldBlock();

            private long disk;
        }

        public interface EncryptionHeader {

        }

    }

    @Getter
    @Setter
    public static final class ZipEntry extends Block {

        public static final ZipEntry NULL = new ZipEntry();

        private final Map<String, LocalFileHeader> localFileHeaders = new LinkedHashMap<>();

        @Setter(AccessLevel.NONE)
        private LocalFileHeader localFileHeader = LocalFileHeader.NULL;


        public void createLocalFileHeader() {
            localFileHeader = new LocalFileHeader();
        }

        public void saveLOcalFileHeader(String fileName) {
            localFileHeaders.put(fileName, localFileHeader);
            localFileHeader = LocalFileHeader.NULL;
        }

        public LocalFileHeader getLocalFileHeader(String fileName) {
            return localFileHeaders.get(fileName);
        }

        @Getter
        @Setter
        public static final class LocalFileHeader extends Block {

            public static final LocalFileHeader NULL = new LocalFileHeader();

            private ExtraFieldBlock extraField;

            public void addExtraField() {
                extraField = new ExtraFieldBlock();
            }
        }

    }

}
