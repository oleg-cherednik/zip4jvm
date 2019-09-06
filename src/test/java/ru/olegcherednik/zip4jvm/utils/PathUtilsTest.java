package ru.olegcherednik.zip4jvm.utils;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jSuite;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 06.09.2019
 */
@Test
public class PathUtilsTest {

    public void shouldRetrieveUnique() throws IOException {
        List<Path> paths = new ArrayList<>();
        paths.add(Zip4jSuite.carsDir);
        paths.add(Zip4jSuite.starWarsDir);
        paths.add(Zip4jSuite.emptyDir);
        paths.add(Zip4jSuite.srcDir.resolve("empty_file.txt"));
        paths.add(Zip4jSuite.srcDir.resolve("mcdonnell-douglas-f15-eagle.jpg"));
        paths.add(Zip4jSuite.srcDir.resolve("Oleg Cherednik.txt"));
        paths.add(Zip4jSuite.srcDir.resolve("saint-petersburg.jpg"));
        paths.add(Zip4jSuite.srcDir.resolve("sig-sauer-pistol.jpg"));

        List<Path> res = PathUtils.getRelativeContent(paths);

        int a = 0;
        a++;
    }

}
