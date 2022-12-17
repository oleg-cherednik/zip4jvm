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
//        Path zip = Paths.get("d:/zip4jvm/zip64/split/ferdinand.zip");
//        Path zip = Paths.get("d:/zip4jvm/aaa/split/ducati.zip");

//        Path zip = Paths.get("d:/zip4jvm/aaa/ducati-panigale-1199.zip");
//        Path zip = Paths.get("d:/zip4jvm/aaa/ducati.zipx");
//        Path zip = Paths.get("d:/zip4jvm/zip64/bzip2-aes256-strong.zip");

        Path zip = Paths.get("d:/zip4jvm/zip64/bzip2-aes256-strong.zip");

        Path destDir = Paths.get("d:/zip4jvm/aaa/bbb");

//        for (Path zip : Arrays.asList(zip1, zip2)) {
        System.out.println(zip);
        UnzipIt.zip(zip).destDir(destDir).password("1".toCharArray()).extract();
//        ZipInfo.zip(zip).password("1".toCharArray()).printShortInfo();
//        }
    }

}