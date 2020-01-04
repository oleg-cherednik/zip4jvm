package ru.olegcherednik.zip4jvm.assertj;

import org.assertj.core.api.PathAssert;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 13.11.2019
 */
public class PathExtAssert extends PathAssert {

    public PathExtAssert(Path actual) {
        super(actual);
    }
}
