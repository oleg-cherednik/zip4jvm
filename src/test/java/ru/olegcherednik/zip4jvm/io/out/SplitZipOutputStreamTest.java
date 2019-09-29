package ru.olegcherednik.zip4jvm.io.out;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 29.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class SplitZipOutputStreamTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(SplitZipOutputStreamTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldWriteStreamWhenUsingDataOutput() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.data");
        ZipModel zipModel = new ZipModel(file);
        zipModel.setSplitSize(10);

        try (SplitZipOutputStream out = new SplitZipOutputStream(zipModel)) {
            assertThat(out.getOffs()).isEqualTo(4);

            out.writeWord(0x0201);
            assertThat(out.getOffs()).isEqualTo(6);

            out.writeDword(0x06050403);
            assertThat(out.getOffs()).isEqualTo(10);

            out.writeQword(0x0E0D0C0B0A090807L);
            assertThat(out.getOffs()).isEqualTo(8);
            assertThat(out.toString()).isEqualTo("offs: 8 (0x8)");

            out.writeBytes("oleg".getBytes(StandardCharsets.UTF_8));
            assertThat(out.getOffs()).isEqualTo(2);

            out.writeBytes((byte)0x11);
            assertThat(out.getOffs()).isEqualTo(3);

            out.writeBytes(new byte[] { 0x12, 0x13, 0x14 });
            assertThat(out.getOffs()).isEqualTo(6);
        }

        zipModel.setTotalDisks(5);

        byte[] buf = FileUtils.readFileToByteArray(zipModel.getPartFile(0).toFile());
        assertThat(Arrays.copyOfRange(buf, 0, 4)).isEqualTo(new byte[] { 0x50, 0x4B, 0x7, 0x8 });
        assertThat(Arrays.copyOfRange(buf, 4, 6)).isEqualTo(new byte[] { 0x1, 0x2 });
        assertThat(Arrays.copyOfRange(buf, 6, 10)).isEqualTo(new byte[] { 0x3, 0x4, 0x5, 0x6 });

        buf = FileUtils.readFileToByteArray(zipModel.getPartFile(1).toFile());
        assertThat(Arrays.copyOfRange(buf, 0, 8)).isEqualTo(new byte[] { 0x7, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE });
        assertThat(new String(Arrays.copyOfRange(buf, 8, 10), StandardCharsets.UTF_8)).isEqualTo("ol");

        buf = FileUtils.readFileToByteArray(zipModel.getPartFile(2).toFile());
        assertThat(new String(Arrays.copyOfRange(buf, 0, 2), StandardCharsets.UTF_8)).isEqualTo("eg");
        assertThat(Arrays.copyOfRange(buf, 2, 3)).isEqualTo(new byte[] { 0x11 });
        assertThat(Arrays.copyOfRange(buf, 3, 6)).isEqualTo(new byte[] { 0x12, 0x13, 0x14 });
    }

}
