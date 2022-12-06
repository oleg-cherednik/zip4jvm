package ru.olegcherednik.zip4jvm;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Oleg Cherednik
 * @since 19.10.2022
 */
public class Foo {

    public static void main(String[] args) throws IOException {
//        Path zip = Paths.get("d:/zip4jvm/aaa/strong_aes_128.zip");
//        Path zip = Paths.get("d:/zip4jvm/aaa/strong_aes_192.zip");
//        Path zip = Paths.get("d:/zip4jvm/aaa/strong_aes_256_2.zip");
//        Path zip = Paths.get("d:/zip4jvm/aaa/strong_3des_168.zip");
//        Path zip = Paths.get("d:/zip4jvm/aaa/appnote.zip");
//        Path zip = Paths.get("d:/zip4jvm/aaa/aes256_strong_ducati.zip");
        Path zip = Paths.get("d:/zip4jvm/aaa/aes256_normal_honda.zip");



        Path destDir = Paths.get("d:/zip4jvm/aaa/bbb");
//        UnzipIt.zip(zip).destDir(destDir).password("1".toCharArray()).extract();
        ZipInfo.zip(zip).printShortInfo();
    }

}
