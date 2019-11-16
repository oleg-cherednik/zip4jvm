package ru.olegcherednik.zip4jvm.model.block;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 14.11.2019
 */
public class CentralDirectoryBlock extends Block {

    private final Map<String, FileHeaderBlock> fileHeaders = new LinkedHashMap<>();
    @Getter
    @Setter
    private Block digitalSignatureBlock = Block.NULL;

    public void addFileHeaderBlock(String fileName, FileHeaderBlock block) {
        fileHeaders.put(fileName, block);
    }

    public FileHeaderBlock getFileHeaderBlock(String fileName) {
        return fileHeaders.get(fileName);
    }

    @Getter
    public static final class FileHeaderBlock extends Block {

        private final ExtraFieldBlock extraFields = new ExtraFieldBlock();

    }

}
