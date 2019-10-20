package ru.olegcherednik.zip4jvm.model.block;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 19.10.2019
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Diagnostic {

    public static final Diagnostic NULL = new Diagnostic();

    private static volatile Diagnostic instance = NULL;

    private final Block endCentralDirectory = new Block();
    private Zip64 zip64 = Zip64.NULL;
    private CentralDirectory centralDirectory = new CentralDirectory();

    public static synchronized void createInstance() {
        instance = new Diagnostic();
    }

    public static synchronized Diagnostic removeInstance() {
        try {
            return instance;
        } finally {
            instance = NULL;
        }
    }

    public static synchronized Diagnostic getInstance() {
        return instance;
    }

    public void addZip64() {
        zip64 = new Zip64();
    }

    @Getter
    @Setter
    public static final class Zip64 {

        public static final Zip64 NULL = new Zip64();

        private final Block endCentralDirectoryLocator = new Block();
        private final Block endCentralDirectory = new Block();

    }

    @Getter
    @Setter
    public static final class CentralDirectory extends Block {

        public static final CentralDirectory NULL = new CentralDirectory();

        private Block digitalSignature = Block.NULL;

        private final Map<String, FileHeader> fileHeaders = new LinkedHashMap<>();

        @Setter(AccessLevel.NONE)
        private FileHeader fileHeader = FileHeader.NULL;

        public void addDigitalSignature() {
            digitalSignature = new Block();
        }

        public void createFileHeader() {
            fileHeader = new FileHeader();
        }

        public void saveFileHeader(String fileName) {
            fileHeaders.put(fileName, fileHeader);
            fileHeader = FileHeader.NULL;
        }

        public FileHeader getFileHeader(String fileName) {
            return fileHeaders.get(fileName);
        }

        @Getter
        @Setter
        public static final class FileHeader extends Block {

            public static final FileHeader NULL = new FileHeader();

            private ExtraField extraField;

            public void addExtraField() {
                extraField = new ExtraField();
            }
        }

    }

    @Getter
    @Setter
    public static final class ExtraField extends Block {

        private final Map<Integer, Block> records = new LinkedHashMap<>();

        @Setter(AccessLevel.NONE)
        private Block record;

        public void addRecord() {
            record = new Block();
        }

        public void saveRecord(int signature) {
            records.put(signature, record);
            record = null;
        }

        public Block getRecord(int signature) {
            return records.get(signature);
        }

    }

}
