package ru.olegcherednik.zip4jvm.io.in;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.io.out.SplitZipOutputStream;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 28.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class SplitZipInputStreamTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(SplitZipInputStreamTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldReadStreamWhenUsingDataInput() throws IOException {
        Path z01 = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.z01");

        FileUtils.writeByteArrayToFile(z01.toFile(), convertDword(SplitZipOutputStream.SPLIT_SIGNATURE), true);
        FileUtils.writeByteArrayToFile(z01.toFile(), new byte[] { 0x1, 0x2 }, true);
        FileUtils.writeByteArrayToFile(z01.toFile(), new byte[] { 0x3, 0x4, 0x5, 0x6 }, true);
        FileUtils.writeByteArrayToFile(z01.toFile(), new byte[] { 0x7, 0x8, 0x9, 0xA }, true);

        Path z02 = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.z02");
        FileUtils.writeByteArrayToFile(z02.toFile(), new byte[] { 0xB, 0xC, 0xD, 0xE }, true);
        FileUtils.writeByteArrayToFile(z02.toFile(), new byte[] { 0xF, 0x10 }, true);

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        FileUtils.writeStringToFile(zip.toFile(), "oleg", StandardCharsets.UTF_8, true);
        FileUtils.writeByteArrayToFile(zip.toFile(), new byte[] { 0x11 }, true);
        FileUtils.writeByteArrayToFile(zip.toFile(), new byte[] { 0x12, 0x13, 0x14 }, true);

        ZipModel zipModel = new ZipModel(zip);
        zipModel.setTotalDisks(2);

        try (SplitZipInputStream in = SplitZipInputStream.create(zipModel, 0)) {
            assertThat(in.getOffs()).isEqualTo(4);

            assertThat(in.readWord()).isEqualTo(0x201);
            assertThat(in.getOffs()).isEqualTo(6);

            assertThat(in.readDword()).isEqualTo(0x06050403);
            assertThat(in.getOffs()).isEqualTo(10);

            assertThat(in.readQword()).isEqualTo(0x0E0D0C0B0A090807L);
            assertThat(in.getOffs()).isEqualTo(4);
            assertThat(in.toString()).isEqualTo("offs: 4 (0x4)");

            in.skip(2);
            assertThat(in.getOffs()).isEqualTo(6);

            assertThat(in.readString(4, StandardCharsets.UTF_8)).isEqualTo("oleg");
            assertThat(in.getOffs()).isEqualTo(4);

            assertThat(in.readByte()).isEqualTo(0x11);
            assertThat(in.getOffs()).isEqualTo(5);

            assertThat(in.readBytes(3)).isEqualTo(new byte[] { 0x12, 0x13, 0x14 });
            assertThat(in.getOffs()).isEqualTo(8);

            assertThat(in.getOffs()).isEqualTo(in.length());
        }
    }

    private static byte[] convertDword(long val) {
        byte[] buf = new byte[4];
        buf[0] = (byte)val;
        buf[1] = (byte)(val >> 8);
        buf[2] = (byte)(val >> 16);
        buf[3] = (byte)(val >> 24);
        return buf;
    }
}
