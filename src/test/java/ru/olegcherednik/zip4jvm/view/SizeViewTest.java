package ru.olegcherednik.zip4jvm.view;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 29.12.2019
 */
@Test
public class SizeViewTest {

    public void shouldPrintBytesWhenNameNotBlankAnaManyBytes() throws IOException {
        String[] lines = Zip4jvmSuite.execute(new SizeView("compressed size:", 666, 0, 52));
        assertThat(lines).hasSize(1);
        assertThat(lines[0]).isEqualTo("compressed size:                                    666 bytes");
    }

    public void shouldPrintByteWhenNameNotBlankAnaOneByte() throws IOException {
        String[] lines = Zip4jvmSuite.execute(new SizeView("compressed size:", 1, 0, 52));
        assertThat(lines).hasSize(1);
        assertThat(lines[0]).isEqualTo("compressed size:                                    1 byte");
    }

    public void shouldThrowIllegalArgumentExceptionWhenNameBlank() {
        for (String name : Arrays.asList(null, "", "  "))
            assertThatThrownBy(() -> new SizeView(name, 666, 4, 52)).isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
