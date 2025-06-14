package ru.olegcherednik.zip4jvm.model.src;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static ru.olegcherednik.zip4jvm.TestData.zipStoreSplit;

@Test
public class PkwareSplitSrcZipTest {

    public void shouldExcludeFileWithMinSizeWhenCalculateSplitSize() {
        List<SrcZip.Disk> disks = Arrays.asList(
                SrcZip.Disk.builder().size(20).build(),
                SrcZip.Disk.builder().size(24).build(),
                SrcZip.Disk.builder().size(28).build(),
                SrcZip.Disk.builder().size(5).build()
        );

        PkwareSplitSrcZip srcZip = new PkwareSplitSrcZip(zipStoreSplit, disks);
    }
}
