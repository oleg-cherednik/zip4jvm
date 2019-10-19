package ru.olegcherednik.zip4jvm.model;

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

    private long endCentralDirectoryOffs;
    private long endCentralDirectorySize;
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

    public void setEndCentralDirectoryEndOffs(long offs) {
        endCentralDirectorySize = offs - endCentralDirectoryOffs;
    }

    public void createZip64() {
        zip64 = new Zip64();
    }

    public void createCentralDirectory() {
        centralDirectory = new CentralDirectory();
    }

    @Getter
    @Setter
    public static final class Zip64 {

        public static final Zip64 NULL = new Zip64();

        private long endCentralDirectoryLocatorOffs;
        private long endCentralDirectoryLocatorSize;
        private long endCentralDirectoryOffs;
        private long endCentralDirectorySize;

        public void setEndCentralDirectoryLocatorEndOffs(long offs) {
            endCentralDirectoryLocatorSize = offs - endCentralDirectoryLocatorOffs;
        }

        public void setEndCentralDirectoryEndOffs(long offs) {
            endCentralDirectorySize = offs - endCentralDirectoryOffs;
        }

    }

    @Getter
    @Setter
    public static final class CentralDirectory {

        public static final CentralDirectory NULL = new CentralDirectory();

        private long offs;
        private long size;

        private long digitalSignatureOffs;
        private long digitalSignatureSize;

        private final Map<String, FileHeader> fileHeaders = new LinkedHashMap<>();

        @Setter(AccessLevel.NONE)
        private FileHeader fileHeader;

        public void setEndOffs(long offs) {
            size = offs - this.offs;
        }

        public void setDigitalSignatureEndOffs(long offs) {
            digitalSignatureSize = offs - digitalSignatureOffs;
        }

        public void createFileHeader() {
            fileHeader = new FileHeader();
        }

        public void saveFileHeader(String fileName) {
            fileHeaders.put(fileName, fileHeader);
            fileHeader = null;
        }

        public FileHeader getFileHeader(String fileName) {
            return fileHeaders.get(fileName);
        }

        @Getter
        @Setter
        public static final class FileHeader {

            private long offs;
            private long size;
            private ExtraField extraField;

            public void setEndOffs(long offs) {
                size = offs - this.offs;
            }

            public void createExtraField() {
                extraField = new ExtraField();
            }
        }

    }

    @Getter
    @Setter
    public static final class ExtraField {

        private long offs;
        private long size;
        private final Map<Integer, Record> records = new LinkedHashMap<>();

        @Setter(AccessLevel.NONE)
        private Record record;

        public void setEndOffs(long offs) {
            size = offs - this.offs;
        }

        public void createRecord() {
            record = new Record();
        }

        public void saveRecord(int signature) {
            records.put(signature, record);
            record = null;
        }

        public Record getRecord(int signature) {
            return records.get(signature);
        }

        @Getter
        @Setter
        public static final class Record {

            private long offs;
            private long size;

            public void setEndOffs(long offs) {
                size = offs - this.offs;
            }
        }
    }

}
