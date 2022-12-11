package ru.olegcherednik.zip4jvm;

import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 19.10.2022
 */
public class Foo {

    public static void main(String[] args) throws IOException {
//        Path zip = Paths.get("d:/zip4jvm/aaa/APPNOTE-6.3.2.zip");
        Path zip = Paths.get("d:/zip4jvm/aaa/store-strong-aes256 - central.zip");
//        Path zip2 = Paths.get("d:/zip4jvm/aaa/store-strong-aes256 - central.zip");
        Path destDir = Paths.get("d:/zip4jvm/aaa/bbb");

//        for (Path zip : Arrays.asList(zip1, zip2)) {
        System.out.println(zip);
//        UnzipIt.zip(zip).destDir(destDir).password("1".toCharArray()).extract();
        ZipInfo.zip(zip).password("1".toCharArray()).printShortInfo();
//        }
    }

}
