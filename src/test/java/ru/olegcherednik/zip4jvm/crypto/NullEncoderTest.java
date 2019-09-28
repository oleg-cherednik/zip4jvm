package ru.olegcherednik.zip4jvm.crypto;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 15.09.2019
 */
@Test
public class NullEncoderTest {

    public void shouldRetrieveNullWhenToString() {
        assertThat(Encoder.NULL.toString()).isEqualTo("<null>");
    }

}
