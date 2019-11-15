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

    private final Map<String, FileHeaderBlock> fileHeaders = new LinkedHashMap<>();
    private Block digitalSignatureBlock = Block.NULL;

    @Setter(AccessLevel.NONE)
    private FileHeaderBlock fileHeader;

    public void addFileHeader() {
        fileHeader = new FileHeaderBlock();
    }

    public void saveFileHeader(String fileName) {
        fileHeaders.put(fileName, fileHeader);
        fileHeader = null;
    }

    public FileHeaderBlock getFileHeader(String fileName) {
        return fileHeaders.get(fileName);
    }

    @Getter
    @Setter
    public static final class FileHeaderBlock extends Block {

        private final ExtraFieldBlock extraFields = new ExtraFieldBlock();

    }

}
