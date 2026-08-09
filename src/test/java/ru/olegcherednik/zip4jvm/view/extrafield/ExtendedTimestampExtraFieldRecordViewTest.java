/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.extrafield.records.ExtendedTimestampExtraFieldRecord;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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

    private static final String SIZE_17_BYTES = "  - size:                                           17 bytes";
    private static final String TITLE = "(0x5455) Universal time:            "
            + "                5296723 (0x0050D253) bytes";

    public void shouldRetrieveThreeTimesWhenAllTimesSet() {
        Block block = createBlock();

        ExtendedTimestampExtraFieldRecord record = createRecord(BIT0 | BIT1 | BIT2);
        String[] lines = Zip4jvmSuite.execute(createView(record, 0, block));

        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo(SIZE_17_BYTES);
        assertThat(lines[2]).isEqualTo("  Last Modified Date:                               2019-10-24 07:46:22");
        assertThat(lines[3]).isEqualTo("  Last Accessed Date:                               2019-10-22 00:13:05");
        assertThat(lines[4]).isEqualTo("  Creation Date:                                    2019-11-04 21:33:02");
    }

    public void shouldRetrieveLastModificationTimeWhenOnlyItSet() {
        Block block = createBlock();

        ExtendedTimestampExtraFieldRecord record = createRecord(BIT0);
        String[] lines = Zip4jvmSuite.execute(createView(record, 0, block));

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo(SIZE_17_BYTES);
        assertThat(lines[2]).isEqualTo("  Last Modified Date:                               2019-10-24 07:46:22");
    }

    public void shouldRetrieveLastAccessTimeWhenOnlyItSet() {
        Block block = createBlock();

        ExtendedTimestampExtraFieldRecord record = createRecord(BIT1);
        String[] lines = Zip4jvmSuite.execute(createView(record, 0, block));

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo(SIZE_17_BYTES);
        assertThat(lines[2]).isEqualTo("  Last Accessed Date:                               2019-10-22 00:13:05");
    }

    public void shouldRetrieveCreationTimeWhenOnlyItSet() {
        Block block = createBlock();

        ExtendedTimestampExtraFieldRecord record = createRecord(BIT2);
        String[] lines = Zip4jvmSuite.execute(createView(record, 0, block));

        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo(SIZE_17_BYTES);
        assertThat(lines[2]).isEqualTo("  Creation Date:                                    2019-11-04 21:33:02");
    }

    public void shouldRetrieveThreeTimesWithDiskWhenSplit() {
        Block block = createBlock();
        when(block.getDiskNo()).thenReturn(5);
        when(block.getFileName()).thenReturn("src.zip");

        ExtendedTimestampExtraFieldRecord record = createRecord(BIT0 | BIT1 | BIT2);
        String[] lines = Zip4jvmSuite.execute(createView(record, 5, block));

        assertThat(lines).hasSize(6);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo("  - disk (0005):                                    src.zip");
        assertThat(lines[2]).isEqualTo(SIZE_17_BYTES);
        assertThat(lines[3]).isEqualTo("  Last Modified Date:                               2019-10-24 07:46:22");
        assertThat(lines[4]).isEqualTo("  Last Accessed Date:                               2019-10-22 00:13:05");
        assertThat(lines[5]).isEqualTo("  Creation Date:                                    2019-11-04 21:33:02");
    }

    @Test(dataProvider = "lastModificationTime")
    public void shouldNotPrintWhenNoLastModificationTime(ExtendedTimestampExtraFieldRecord.Flag flag,
                                                         long lastModifiedTime) {
        ExtendedTimestampExtraFieldRecord record =
                ExtendedTimestampExtraFieldRecord.builder()
                                                 .flag(flag)
                                                 .lastModificationTime(lastModifiedTime)
                                                 .build();
        String[] lines = Zip4jvmSuite.execute(createView(record, 0, createBlock()));

        assertThat(lines).hasSize(2);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo(SIZE_17_BYTES);
    }

    @DataProvider(name = "lastModificationTime")
    public static Object[][] lastModificationTimeData() {
        return new Object[][] {
                { new ExtendedTimestampExtraFieldRecord.Flag(0x0), -1 },
                { new ExtendedTimestampExtraFieldRecord.Flag(BIT0), -1 },
                { new ExtendedTimestampExtraFieldRecord.Flag(0x0), System.currentTimeMillis() }
        };
    }

    @Test(dataProvider = "lastAccessTime")
    public void shouldNotPrintWhenNoLastAccessTime(ExtendedTimestampExtraFieldRecord.Flag flag,
                                                   long lastAccessTime) {
        ExtendedTimestampExtraFieldRecord record =
                ExtendedTimestampExtraFieldRecord.builder()
                                                 .flag(flag)
                                                 .lastAccessTime(lastAccessTime)
                                                 .build();
        String[] lines = Zip4jvmSuite.execute(createView(record, 0, createBlock()));

        assertThat(lines).hasSize(2);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo(SIZE_17_BYTES);
    }

    @DataProvider(name = "lastAccessTime")
    public static Object[][] lastAccessTimeData() {
        return new Object[][] {
                { new ExtendedTimestampExtraFieldRecord.Flag(0x0), -1 },
                { new ExtendedTimestampExtraFieldRecord.Flag(BIT1), -1 },
                { new ExtendedTimestampExtraFieldRecord.Flag(0x0), System.currentTimeMillis() }
        };
    }

    @Test(dataProvider = "creationTime")
    public void shouldNotPrintWhenNoCreationTime(ExtendedTimestampExtraFieldRecord.Flag flag,
                                                 long creationTime) {
        ExtendedTimestampExtraFieldRecord record =
                ExtendedTimestampExtraFieldRecord.builder()
                                                 .flag(flag)
                                                 .creationTime(creationTime)
                                                 .build();
        String[] lines = Zip4jvmSuite.execute(createView(record, 0, createBlock()));

        assertThat(lines).hasSize(2);
        assertThat(lines[0]).isEqualTo(TITLE);
        assertThat(lines[1]).isEqualTo(SIZE_17_BYTES);
    }

    @DataProvider(name = "creationTime")
    public static Object[][] creationTimeData() {
        return new Object[][] {
                { new ExtendedTimestampExtraFieldRecord.Flag(0x0), -1 },
                { new ExtendedTimestampExtraFieldRecord.Flag(BIT2), -1 },
                { new ExtendedTimestampExtraFieldRecord.Flag(0x0), System.currentTimeMillis() }
        };
    }

    private static Block createBlock() {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(17L);
        when(block.getDiskOffs()).thenReturn(5296723L);
        return block;
    }

    private static ExtendedTimestampExtraFieldRecord createRecord(int flag) {
        return ExtendedTimestampExtraFieldRecord.builder()
                                                .dataSize(13)
                                                .flag(new ExtendedTimestampExtraFieldRecord.Flag(flag))
                                                .lastModificationTime(lastModifiedTime)
                                                .lastAccessTime(lastAccessTime)
                                                .creationTime(creationTime).build();
    }

    private static ExtendedTimestampExtraFieldRecordView createView(ExtendedTimestampExtraFieldRecord record,
                                                                    long totalDisks,
                                                                    Block block) {
        return ExtendedTimestampExtraFieldRecordView.builder()
                                                    .offs(0)
                                                    .columnWidth(52)
                                                    .totalDisks(totalDisks)
                                                    .record(record)
                                                    .block(block).build();
    }

}
