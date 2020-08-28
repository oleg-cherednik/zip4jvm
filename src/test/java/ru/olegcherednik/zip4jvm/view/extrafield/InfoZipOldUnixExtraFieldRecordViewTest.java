package ru.olegcherednik.zip4jvm.view.extrafield;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.extrafield.InfoZipOldUnixExtraFieldRecord;

import java.io.IOException;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
@Test
@SuppressWarnings({ "NewClassNamingConvention", "FieldNamingConvention" })
public class InfoZipOldUnixExtraFieldRecordViewTest {

    private static final long lastModifiedTime = 1571903182001L;
    private static final long lastAccessTime = 1571703185000L;

    public void shouldRetrieveAllDataWhenAllDataSet() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(12L);
        when(block.getRelativeOffs()).thenReturn(5300395L);

        InfoZipOldUnixExtraFieldRecord record = InfoZipOldUnixExtraFieldRecord.builder()
                                                                              .dataSize(8)
                                                                              .lastAccessTime(lastAccessTime)
                                                                              .lastModificationTime(lastModifiedTime)
                                                                              .uid(111)
                                                                              .gid(222).build();

        String[] lines = Zip4jvmSuite.execute(InfoZipOldUnixExtraFieldRecordView.builder()
                                                                                .record(record)
                                                                                .block(block)
                                                                                .position(0, 52, 0).build());

        assertThat(lines).hasSize(6);
        assertThat(lines[0]).isEqualTo("(0x5855) old InfoZIP Unix/OS2/NT:                   5300395 (0x0050E0AB) bytes");
        assertThat(lines[1]).isEqualTo("  - size:                                           12 bytes");
        assertThat(lines[2]).isEqualTo("  Last Modified Date:                               2019-10-24 07:46:22");
        assertThat(lines[3]).isEqualTo("  Last Accessed Date:                               2019-10-22 00:13:05");
        assertThat(lines[4]).isEqualTo("  User identifier (UID):                            111");
        assertThat(lines[5]).isEqualTo("  Group Identifier (GID):                           222");
    }

    public void shouldRetrieveTimesOnlyWhenNoUserId() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(12L);
        when(block.getRelativeOffs()).thenReturn(5300395L);

        InfoZipOldUnixExtraFieldRecord record = InfoZipOldUnixExtraFieldRecord.builder()
                                                                              .dataSize(8)
                                                                              .lastAccessTime(lastAccessTime)
                                                                              .lastModificationTime(lastModifiedTime).build();

        String[] lines = Zip4jvmSuite.execute(InfoZipOldUnixExtraFieldRecordView.builder()
                                                                                .record(record)
                                                                                .block(block)
                                                                                .position(0, 52, 0).build());

        assertThat(lines).hasSize(4);
        assertThat(lines[0]).isEqualTo("(0x5855) old InfoZIP Unix/OS2/NT:                   5300395 (0x0050E0AB) bytes");
        assertThat(lines[1]).isEqualTo("  - size:                                           12 bytes");
        assertThat(lines[2]).isEqualTo("  Last Modified Date:                               2019-10-24 07:46:22");
        assertThat(lines[3]).isEqualTo("  Last Accessed Date:                               2019-10-22 00:13:05");
    }

    public void shouldRetrieveEmptyStringWhenRecordNull() throws IOException {
        PrintStream out = mock(PrintStream.class);
        InfoZipOldUnixExtraFieldRecordView view = InfoZipOldUnixExtraFieldRecordView.builder()
                                                                                    .record(InfoZipOldUnixExtraFieldRecord.NULL)
                                                                                    .block(mock(Block.class))
                                                                                    .position(0, 52, 0).build();
        assertThat(view.print(out)).isFalse();
    }

    public void shouldRetrieveAllDataWithDiskWhenSplit() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(12L);
        when(block.getRelativeOffs()).thenReturn(5300395L);
        when(block.getDisk()).thenReturn(5L);
        when(block.getFileName()).thenReturn("src.zip");

        InfoZipOldUnixExtraFieldRecord record = InfoZipOldUnixExtraFieldRecord.builder()
                                                                              .dataSize(8)
                                                                              .lastAccessTime(lastAccessTime)
                                                                              .lastModificationTime(lastModifiedTime)
                                                                              .uid(111)
                                                                              .gid(222).build();

        String[] lines = Zip4jvmSuite.execute(InfoZipOldUnixExtraFieldRecordView.builder()
                                                                                .record(record)
                                                                                .block(block)
                                                                                .position(0, 52, 5).build());

        assertThat(lines).hasSize(7);
        assertThat(lines[0]).isEqualTo("(0x5855) old InfoZIP Unix/OS2/NT:                   5300395 (0x0050E0AB) bytes");
        assertThat(lines[1]).isEqualTo("  - disk (0005):                                    src.zip");
        assertThat(lines[2]).isEqualTo("  - size:                                           12 bytes");
        assertThat(lines[3]).isEqualTo("  Last Modified Date:                               2019-10-24 07:46:22");
        assertThat(lines[4]).isEqualTo("  Last Accessed Date:                               2019-10-22 00:13:05");
        assertThat(lines[5]).isEqualTo("  User identifier (UID):                            111");
        assertThat(lines[6]).isEqualTo("  Group Identifier (GID):                           222");
    }
}
