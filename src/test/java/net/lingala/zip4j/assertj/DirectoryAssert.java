package net.lingala.zip4j.assertj;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 27.03.2019
 */
public class DirectoryAssert extends AbstractDirectoryAssert<DirectoryAssert> {

    public DirectoryAssert(Path actual) {
        super(actual, DirectoryAssert.class);
    }
}
