package ru.olegcherednik.zip4jvm.assertj;

import org.assertj.core.api.AbstractStringAssert;

/**
 * @author Oleg Cherednik
 * @since 14.11.2019
 */
public class StringLineAssert extends AbstractStringAssert<StringLineAssert> {

    protected final int pos;

    public StringLineAssert(int pos, String actual) {
        super(actual, StringLineAssert.class);
        this.pos = pos;
    }

    @Override
    public StringLineAssert isEqualTo(Object expected) {
        try {
            return super.isEqualTo(expected);
        } catch(AssertionError e) {
            throw new AssertionError(String.format("(line %d) %s", pos, e.getMessage()), e);
        }
    }
}
