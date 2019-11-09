package ru.olegcherednik.zip4jvm.view.extrafield;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.os.ExtendedTimestampExtraField;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT0;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT1;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT2;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class ExtendedTimestampExtraFieldViewTest {

    public void shouldRetrieveThreeTimesWhenAllTimesSet() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(17L);
        when(block.getOffs()).thenReturn(5296723L);

        ExtendedTimestampExtraField record = ExtendedTimestampExtraField.builder()
                                                                        .dataSize(13)
                                                                        .flag(new ExtendedTimestampExtraField.Flag(BIT0 | BIT1 | BIT2))
                                                                        .lastModificationTime(1571903182000L)
                                                                        .lastAccessTime(1571903185000L)
                                                                        .creationTime(1571903182000L).build();

        String[] lines = Zip4jvmSuite.execute(ExtendedTimestampExtraFieldView.builder()
                                                                             .record(record)
                                                                             .block(block)
                                                                             .columnWidth(52).build());

        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo("(0x5455) Universal time:                            17 bytes");
        assertThat(lines[1]).isEqualTo("  - location:                                       17 (0x00000011) bytes");
        assertThat(lines[2]).isEqualTo("  Last Modified Date:                               2019-10-24 10:46:22");
        assertThat(lines[3]).isEqualTo("  Last Accessed Date:                               2019-10-24 10:46:25");
        assertThat(lines[4]).isEqualTo("  Creation Date:                                    2019-10-24 10:46:22");
    }

    public void shouldRetrieveLastModificationTimeWhenOnlyItSet() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(17L);
        when(block.getOffs()).thenReturn(5296723L);

        ExtendedTimestampExtraField record = ExtendedTimestampExtraField.builder()
                                                                        .dataSize(13)
                                                                        .flag(new ExtendedTimestampExtraField.Flag(BIT0))
                                                                        .lastModificationTime(1571903182000L)
                                                                        .lastAccessTime(1571903185000L)
                                                                        .creationTime(1571903182000L).build();

        String[] lines = Zip4jvmSuite.execute(ExtendedTimestampExtraFieldView.builder()
                                                                             .record(record)
                                                                             .block(block)
                                                                             .columnWidth(52).build());

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo("(0x5455) Universal time:                            17 bytes");
        assertThat(lines[1]).isEqualTo("  - location:                                       17 (0x00000011) bytes");
        assertThat(lines[2]).isEqualTo("  Last Modified Date:                               2019-10-24 10:46:22");
    }

    public void shouldRetrieveLastAccessTimeWhenOnlyItSet() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(17L);
        when(block.getOffs()).thenReturn(5296723L);

        ExtendedTimestampExtraField record = ExtendedTimestampExtraField.builder()
                                                                        .dataSize(13)
                                                                        .flag(new ExtendedTimestampExtraField.Flag(BIT1))
                                                                        .lastModificationTime(1571903182000L)
                                                                        .lastAccessTime(1571903185000L)
                                                                        .creationTime(1571903182000L).build();

        String[] lines = Zip4jvmSuite.execute(ExtendedTimestampExtraFieldView.builder()
                                                                             .record(record)
                                                                             .block(block)
                                                                             .columnWidth(52).build());

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo("(0x5455) Universal time:                            17 bytes");
        assertThat(lines[1]).isEqualTo("  - location:                                       17 (0x00000011) bytes");
        assertThat(lines[2]).isEqualTo("  Last Accessed Date:                               2019-10-24 10:46:25");
    }

    public void shouldRetrieveCreationTimeWhenOnlyItSet() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(17L);
        when(block.getOffs()).thenReturn(5296723L);

        ExtendedTimestampExtraField record = ExtendedTimestampExtraField.builder()
                                                                        .dataSize(13)
                                                                        .flag(new ExtendedTimestampExtraField.Flag(BIT2))
                                                                        .lastModificationTime(1571903182000L)
                                                                        .lastAccessTime(1571903185000L)
                                                                        .creationTime(1571903182000L).build();

        String[] lines = Zip4jvmSuite.execute(ExtendedTimestampExtraFieldView.builder()
                                                                             .record(record)
                                                                             .block(block)
                                                                             .columnWidth(52).build());

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo("(0x5455) Universal time:                            17 bytes");
        assertThat(lines[1]).isEqualTo("  - location:                                       17 (0x00000011) bytes");
        assertThat(lines[2]).isEqualTo("  Creation Date:                                    2019-10-24 10:46:22");
    }

    public void shouldRetrieveEmptyStringWhenRecordNull() throws IOException {
        String[] lines = Zip4jvmSuite.execute(ExtendedTimestampExtraFieldView.builder()
                                                                             .record(ExtendedTimestampExtraField.NULL)
                                                                             .block(mock(Block.class))
                                                                             .columnWidth(52).build());
        assertThat(lines).hasSize(1);
        assertThat(lines[0]).isEmpty();
    }

}
