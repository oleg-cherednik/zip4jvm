package net.lingala.zip4j;

import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.ZipParameters;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
public class ZipRemoveTest {

    private Path root;
    private Path srcDir;
    private Path destDir;
    private Path resDir;

    @BeforeMethod
    public void createDirectory() throws IOException {
        root = Paths.get("d:/zip4j");//Files.createTempDirectory("zip4j");
        srcDir = root.resolve("src");
        destDir = root.resolve("dest");
        resDir = destDir.resolve("res");

        Files.createDirectories(srcDir);
        Files.createDirectories(destDir);
//        Files.createDirectories(resDir);
    }

    @Test
    public void shouldRemoveGivenFilesFromExistedZip() throws IOException {
        Path zipFile = destDir.resolve("src.zip");

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .defaultFolderPath(srcDir).build();
        List<Path> files = Arrays.asList(
                srcDir.resolve("cars/bentley-continental.jpg"),
                srcDir.resolve("cars/ferrari-458-italia.jpg"),
                srcDir.resolve("cars/wiesmann-gt-mf5.jpg")
        );

//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//        zip.add(files, parameters);

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
//        assertThat(misc.getEntryNames()).hasSize(1);

        Collection<String> entries = Arrays.asList(
                "cars/bentley-continental.jpg",
                "cars/ferrari-458-italia.jpg"
//                "cars/wiesmann-gt-mf5.jpg"
        );
        misc.removeEntries(entries);
        assertThat(misc.getEntryNames()).hasSize(2);
    }
}
