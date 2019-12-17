package ru.olegcherednik.zip4jvm.view.extrafield;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.os.ExtendedTimestampExtraFieldRecord;

import java.io.IOException;
import java.io.PrintStream;

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
@SuppressWarnings({ "NewClassNamingConvention", "FieldNamingConvention" })
public class ExtendedTimestampExtraFieldRecordViewTest {

    private static final long lastModifiedTime = 1571903182001L;
    private static final long lastAccessTime = 1571703185000L;
    private static final long creationTime = 1572903182000L;

    public void shouldRetrieveThreeTimesWhenAllTimesSet() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(17L);
        when(block.getOffs()).thenReturn(5296723L);

        ExtendedTimestampExtraFieldRecord record =
                ExtendedTimestampExtraFieldRecord.builder()
                                                 .dataSize(13)
                                                 .flag(new ExtendedTimestampExtraFieldRecord.Flag(BIT0 | BIT1 | BIT2))
                                                 .lastModificationTime(lastModifiedTime)
                                                 .lastAccessTime(lastAccessTime)
                                                 .creationTime(creationTime).build();

        String[] lines = Zip4jvmSuite.execute(ExtendedTimestampExtraFieldRecordView.builder()
                                                                                   .record(record)
                                                                                   .block(block)
                                                                                   .position(0, 52).build());

        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo("(0x5455) Universal time:                            5296723 (0x0050D253) bytes");
        assertThat(lines[1]).isEqualTo("  - size:                                           17 bytes");
        assertThat(lines[2]).isEqualTo("  Last Modified Date:                               2019-10-24 07:46:22");
        assertThat(lines[3]).isEqualTo("  Last Accessed Date:                               2019-10-22 00:13:05");
        assertThat(lines[4]).isEqualTo("  Creation Date:                                    2019-11-04 21:33:02");
    }

    public void shouldRetrieveLastModificationTimeWhenOnlyItSet() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(17L);
        when(block.getOffs()).thenReturn(5296723L);

        ExtendedTimestampExtraFieldRecord record = ExtendedTimestampExtraFieldRecord.builder()
                                                                                    .dataSize(13)
                                                                                    .flag(new ExtendedTimestampExtraFieldRecord.Flag(BIT0))
                                                                                    .lastModificationTime(lastModifiedTime)
                                                                                    .lastAccessTime(lastAccessTime)
                                                                                    .creationTime(creationTime).build();

        String[] lines = Zip4jvmSuite.execute(ExtendedTimestampExtraFieldRecordView.builder()
                                                                                   .record(record)
                                                                                   .block(block)
                                                                                   .position(0, 52).build());

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo("(0x5455) Universal time:                            5296723 (0x0050D253) bytes");
        assertThat(lines[1]).isEqualTo("  - size:                                           17 bytes");
        assertThat(lines[2]).isEqualTo("  Last Modified Date:                               2019-10-24 07:46:22");
    }

    public void shouldRetrieveLastAccessTimeWhenOnlyItSet() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(17L);
        when(block.getOffs()).thenReturn(5296723L);

        ExtendedTimestampExtraFieldRecord record = ExtendedTimestampExtraFieldRecord.builder()
                                                                                    .dataSize(13)
                                                                                    .flag(new ExtendedTimestampExtraFieldRecord.Flag(BIT1))
                                                                                    .lastModificationTime(lastModifiedTime)
                                                                                    .lastAccessTime(lastAccessTime)
                                                                                    .creationTime(creationTime).build();

        String[] lines = Zip4jvmSuite.execute(ExtendedTimestampExtraFieldRecordView.builder()
                                                                                   .record(record)
                                                                                   .block(block)
                                                                                   .position(0, 52).build());

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo("(0x5455) Universal time:                            5296723 (0x0050D253) bytes");
        assertThat(lines[1]).isEqualTo("  - size:                                           17 bytes");
        assertThat(lines[2]).isEqualTo("  Last Accessed Date:                               2019-10-22 00:13:05");
    }

    public void shouldRetrieveCreationTimeWhenOnlyItSet() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(17L);
        when(block.getOffs()).thenReturn(5296723L);

        ExtendedTimestampExtraFieldRecord record = ExtendedTimestampExtraFieldRecord.builder()
                                                                                    .dataSize(13)
                                                                                    .flag(new ExtendedTimestampExtraFieldRecord.Flag(BIT2))
                                                                                    .lastModificationTime(lastModifiedTime)
                                                                                    .lastAccessTime(lastAccessTime)
                                                                                    .creationTime(creationTime).build();

        String[] lines = Zip4jvmSuite.execute(ExtendedTimestampExtraFieldRecordView.builder()
                                                                                   .record(record)
                                                                                   .block(block)
                                                                                   .position(0, 52).build());

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo("(0x5455) Universal time:                            5296723 (0x0050D253) bytes");
        assertThat(lines[1]).isEqualTo("  - size:                                           17 bytes");
        assertThat(lines[2]).isEqualTo("  Creation Date:                                    2019-11-04 21:33:02");
    }

    public void shouldRetrieveEmptyStringWhenRecordNull() throws IOException {
        PrintStream out = mock(PrintStream.class);
        ExtendedTimestampExtraFieldRecordView view = ExtendedTimestampExtraFieldRecordView.builder()
                                                                                          .record(ExtendedTimestampExtraFieldRecord.NULL)
                                                                                          .block(mock(Block.class))
                                                                                          .position(0, 52).build();
        assertThat(view.print(out)).isFalse();
    }

}
