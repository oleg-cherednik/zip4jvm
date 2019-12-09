package ru.olegcherednik.zip4jvm.view;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.model.ExternalFileAttributes.MAC;
import static ru.olegcherednik.zip4jvm.model.ExternalFileAttributes.UNIX;
import static ru.olegcherednik.zip4jvm.model.ExternalFileAttributes.WIN;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT0;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT2;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT4;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT6;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT7;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
@Test
public class ExternalFileAttributesViewTest {

    public void shouldRetrieveWindowsAttributesWhenWin() throws IOException {
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.build(() -> WIN);
        externalFileAttributes.readFrom(new byte[] { (byte)(BIT0 | BIT2 | BIT4), 0x0, 0x0, 0x0 });

        String[] lines = Zip4jvmSuite.execute(new ExternalFileAttributesView(externalFileAttributes, 0, 52));

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo("external file attributes:                           0x00000015");
        assertThat(lines[1]).isEqualTo("  MS-DOS file attributes (0x15):                    rdo sys dir");
        assertThat(lines[2]).isEqualTo("  non-MSDOS file attributes (0x000000):             ?---------");
    }

    public void shouldRetrievePosixAttributesWhenMacOrUnix() throws IOException {
        for (Supplier<String> osNameProvider : Arrays.asList((Supplier<String>)() -> MAC, () -> UNIX)) {
            ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.build(osNameProvider);
            externalFileAttributes.readFrom(new byte[] { 0x0, 0x0, (byte)(BIT0 | BIT2 | BIT4 | BIT6), (byte)(BIT0 | BIT7) });

            String[] lines = Zip4jvmSuite.execute(new ExternalFileAttributesView(externalFileAttributes, 0, 52));

            assertThat(lines).hasSize(3);
            assertThat(lines[0]).isEqualTo("external file attributes:                           0x81550000");
            assertThat(lines[1]).isEqualTo("  MS-DOS file attributes (0x00):                    arc");
            assertThat(lines[2]).isEqualTo("  non-MSDOS file attributes (0xFF815500):           -r-x-w-r-x");
        }
    }
}
