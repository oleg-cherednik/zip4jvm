package ru.olegcherednik.zip4jvm.model;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 04.01.2020
 */
@Test
public class CharsetsTest {

    public void shouldRetrieveSystemCharsetWhenUseSystemCharsetFunction() {
        assertThat(Charsets.SYSTEM_CHARSET.apply(null)).isSameAs(Charsets.SYSTEM);
        assertThat(Charsets.SYSTEM_CHARSET.apply(Charsets.SYSTEM)).isSameAs(Charsets.SYSTEM);
        assertThat(Charsets.SYSTEM_CHARSET.apply(Charsets.UTF_8)).isSameAs(Charsets.SYSTEM);
    }

    public void shouldRetrieveUnmodifiedCharsetWhenUseUnmodifiedFunction() {
        assertThat(Charsets.UNMODIFIED.apply(null)).isNull();
        assertThat(Charsets.UNMODIFIED.apply(Charsets.UTF_8)).isSameAs(Charsets.UTF_8);
        assertThat(Charsets.UNMODIFIED.apply(Charsets.SYSTEM)).isSameAs(Charsets.SYSTEM);
    }
}
