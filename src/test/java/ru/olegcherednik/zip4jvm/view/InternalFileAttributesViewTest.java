package ru.olegcherednik.zip4jvm.view;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.InternalFileAttributes;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
@Test
public class InternalFileAttributesViewTest {

    public void shouldRetrieveWindowsAttributesWhenWin() throws IOException {
        String[] lines = Zip4jvmSuite.execute(new InternalFileAttributesView(InternalFileAttributes.build(new byte[] { 0x1, 0x2 }), 0, 52));

        assertThat(lines).hasSize(2);
        assertThat(lines[0]).isEqualTo("internal file attributes:                           0x0201");
        assertThat(lines[1]).isEqualTo("  apparent file type:                               text");
    }

}
