package ru.olegcherednik.zip4jvm.model.block;

import lombok.Getter;

/**
 * @author Oleg Cherednik
 * @since 16.11.2019
 */
@Getter
public class Zip64Block {

    private final Block endCentralDirectoryLocatorBlock = new Block();
    private final Block endCentralDirectoryBlock = new Block();

}
