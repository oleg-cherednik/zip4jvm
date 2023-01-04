package ru.olegcherednik.zip4jvm.model.block;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 25.12.2022
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseCentralDirectoryBlock extends Block {

    public abstract void addFileHeader(String fileName, CentralDirectoryBlock.FileHeaderBlock block);

    public abstract void setDigitalSignature(Block block);

    public abstract Block getDigitalSignature();

    public abstract CentralDirectoryBlock.FileHeaderBlock getFileHeader(String fileName);

    public abstract CentralDirectoryBlock.FileHeaderBlock createFileHeaderBlock();

}
