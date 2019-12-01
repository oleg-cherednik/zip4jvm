package ru.olegcherednik.zip4jvm.model.block;

import lombok.Getter;
import lombok.Setter;

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

}
