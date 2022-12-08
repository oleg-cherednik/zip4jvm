package ru.olegcherednik.zip4jvm;

import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Oleg Cherednik
 * @since 19.10.2022
 */
public class Foo {

    public static void main(String[] args) throws IOException {
        Path zip = Paths.get("d:/zip4jvm/aaa/one_aes_256_deflate.zip");

        Path destDir = Paths.get("d:/zip4jvm/aaa/bbb");
        UnzipIt.zip(zip).destDir(destDir).password("1".toCharArray()).extract();
//        ZipInfo.zip(zip).printShortInfo();
    }

}
