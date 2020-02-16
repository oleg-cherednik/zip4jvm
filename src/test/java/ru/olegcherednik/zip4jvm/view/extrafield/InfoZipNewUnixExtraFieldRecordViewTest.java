package ru.olegcherednik.zip4jvm.view.extrafield;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.extrafield.InfoZipNewUnixExtraFieldRecord;

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
@SuppressWarnings("NewClassNamingConvention")
public class InfoZipNewUnixExtraFieldRecordViewTest {

    public void shouldRetrieveVersionOneRecordWhenVersionOne() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(15L);
        when(block.getOffs()).thenReturn(5296740L);

        InfoZipNewUnixExtraFieldRecord.Payload payload = InfoZipNewUnixExtraFieldRecord.VersionOnePayload.builder()
                                                                                                         .uid("aaa")
                                                                                                         .gid("bbb").build();

        InfoZipNewUnixExtraFieldRecord record = InfoZipNewUnixExtraFieldRecord.builder()
                                                                              .dataSize(11)
                                                                              .payload(payload).build();

        String[] lines = Zip4jvmSuite.execute(InfoZipNewUnixExtraFieldRecordView.builder()
                                                                                .record(record)
                                                                                .block(block)
                                                                                .position(0, 52, 0).build());

        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo("(0x7875) new InfoZIP Unix/OS2/NT:                   5296740 (0x0050D264) bytes");
        assertThat(lines[1]).isEqualTo("  - size:                                           15 bytes");
        assertThat(lines[2]).isEqualTo("  version:                                          1");
        assertThat(lines[3]).isEqualTo("  User identifier (UID):                            aaa");
        assertThat(lines[4]).isEqualTo("  Group Identifier (GID):                           bbb");
    }

    public void shouldRetrieveUnknownVersionRecordWhenVersionNotOne() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(15L);
        when(block.getOffs()).thenReturn(5296740L);

        InfoZipNewUnixExtraFieldRecord.Payload payload = InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload.builder()
                                                                                                             .version(2)
                                                                                                             .data(new byte[] { 0x0, 0x1, 0x2, 0x3 })
                                                                                                             .build();

        InfoZipNewUnixExtraFieldRecord record = InfoZipNewUnixExtraFieldRecord.builder()
                                                                              .dataSize(11)
                                                                              .payload(payload).build();

        String[] lines = Zip4jvmSuite.execute(InfoZipNewUnixExtraFieldRecordView.builder()
                                                                                .record(record)
                                                                                .block(block)
                                                                                .position(0, 52, 0).build());

        assertThat(lines).hasSize(4);
        assertThat(lines[0]).isEqualTo("(0x7875) new InfoZIP Unix/OS2/NT:                   5296740 (0x0050D264) bytes");
        assertThat(lines[1]).isEqualTo("  - size:                                           15 bytes");
        assertThat(lines[2]).isEqualTo("  version:                                          2 (unknown)");
        assertThat(lines[3]).isEqualTo("00 01 02 03");
    }

    public void shouldRetrieveEmptyStringWhenRecordNull() throws IOException {
        PrintStream out = mock(PrintStream.class);
        InfoZipNewUnixExtraFieldRecordView view = InfoZipNewUnixExtraFieldRecordView.builder()
                                                                                    .record(InfoZipNewUnixExtraFieldRecord.NULL)
                                                                                    .block(mock(Block.class))
                                                                                    .position(0, 52, 0).build();
        assertThat(view.print(out)).isFalse();
    }

    public void shouldRetrieveVersionOneRecordWithDiskWhenSplit() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(15L);
        when(block.getOffs()).thenReturn(5296740L);
        when(block.getDisk()).thenReturn(5L);
        when(block.getFileName()).thenReturn("src.zip");

        InfoZipNewUnixExtraFieldRecord.Payload payload = InfoZipNewUnixExtraFieldRecord.VersionOnePayload.builder()
                                                                                                         .uid("aaa")
                                                                                                         .gid("bbb").build();

        InfoZipNewUnixExtraFieldRecord record = InfoZipNewUnixExtraFieldRecord.builder()
                                                                              .dataSize(11)
                                                                              .payload(payload).build();

        String[] lines = Zip4jvmSuite.execute(InfoZipNewUnixExtraFieldRecordView.builder()
                                                                                .record(record)
                                                                                .block(block)
                                                                                .position(0, 52, 5).build());

        assertThat(lines).hasSize(6);
        assertThat(lines[0]).isEqualTo("(0x7875) new InfoZIP Unix/OS2/NT:                   5296740 (0x0050D264) bytes");
        assertThat(lines[1]).isEqualTo("  - disk (0005):                                    src.zip");
        assertThat(lines[2]).isEqualTo("  - size:                                           15 bytes");
        assertThat(lines[3]).isEqualTo("  version:                                          1");
        assertThat(lines[4]).isEqualTo("  User identifier (UID):                            aaa");
        assertThat(lines[5]).isEqualTo("  Group Identifier (GID):                           bbb");
    }
}
