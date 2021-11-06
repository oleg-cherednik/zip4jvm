package ru.olegcherednik.zip4jvm.zstd;

import ru.olegcherednik.zip4jvm.UnzipIt;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Oleg Cherednik
 * @since 06.11.2021
 */
public class Foo {

    public static void main(String... args) throws IOException {
        Path zip = Paths.get("d:/Programming/GitHub/zip4jvm/src/test/resources/seven-zip/zstd_solid_off.zip");
        Path destDir = Paths.get("d:/zip4jvm/zstd/out");
//        ZipInfo.zip(zip).settings(ZipInfoSettings.builder()
//                                                 .copyPayload(true)
//                                                 .build()).decompose(Paths.get("d:/zip4jvm/zstd/decompose"));
        UnzipIt.zip(zip).destDir(destDir).extract();
    }

}
