package ru.olegcherednik.zip4jvm.assertj;

import org.assertj.core.api.AbstractStringAssert;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 14.11.2019
 */
public class StringLineAssert extends AbstractStringAssert<StringLineAssert> {

    protected final Path path;
    protected final int pos;

    public StringLineAssert(Path path, int pos, String actual) {
        super(actual, StringLineAssert.class);
        this.path = path;
        this.pos = pos;
    }

    @Override
    public StringLineAssert isEqualTo(Object expected) {
        try {
            return super.isEqualTo(expected);
        } catch(AssertionError e) {
            throw new AssertionError(String.format("%s (line %d) %s", path.toAbsolutePath(), pos, e.getMessage()), e);
        }
    }
}
