package ru.olegcherednik.zip4jvm.crypto;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 15.09.2019
 */
@Test
public class NullEncoderTest {

    public void shouldRetrieveNullWhenToString() {
        assertThat(Encoder.NULL.toString()).isEqualTo("<null>");
    }

    public void shouldThrowNullPointerExceptionWhenNull() {
        assertThatThrownBy(() -> Encoder.NULL.close(null)).isExactlyInstanceOf(NullPointerException.class);
    }

}
