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
    private Block digitalSignature = Block.NULL;

    public void addFileHeader(String fileName, FileHeaderBlock block) {
        fileHeaders.put(fileName, block);
    }

    public FileHeaderBlock getFileHeader(String fileName) {
        return fileHeaders.get(fileName);
    }

    @Getter
    public static class FileHeaderBlock extends Block {

        private final ExtraFieldBlock extraFieldBlock = new ExtraFieldBlock();

    }

}
