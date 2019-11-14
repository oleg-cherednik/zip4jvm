package ru.olegcherednik.zip4jvm.model.block;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 14.11.2019
 */
@Getter
@Setter
public class CentralDirectoryBlock extends Block {

    public static final Diagnostic.CentralDirectory NULL = new Diagnostic.CentralDirectory();

    private Block digitalSignature = Block.NULL;

    private final Map<String, Diagnostic.CentralDirectory.FileHeader> fileHeaders = new LinkedHashMap<>();

    @Setter(AccessLevel.NONE)
    private Diagnostic.CentralDirectory.FileHeader fileHeader = Diagnostic.CentralDirectory.FileHeader.NULL;

    public void addDigitalSignature() {
        digitalSignature = new Block();
    }

    public void addFileHeader() {
        fileHeader = new Diagnostic.CentralDirectory.FileHeader();
    }

    public void saveFileHeader(String fileName) {
        fileHeaders.put(fileName, fileHeader);
        fileHeader = Diagnostic.CentralDirectory.FileHeader.NULL;
    }

    public Diagnostic.CentralDirectory.FileHeader getFileHeader(String fileName) {
        return fileHeaders.get(fileName);
    }

    @Getter
    @Setter
    public static final class FileHeader extends Diagnostic.ExtraFieldBlock {

        public static final Diagnostic.CentralDirectory.FileHeader NULL = new Diagnostic.CentralDirectory.FileHeader();

    }

}
