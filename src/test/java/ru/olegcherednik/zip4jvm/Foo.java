package ru.olegcherednik.zip4jvm;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 19.10.2022
 */
public class Foo {

    public static void main(String[] args) throws IOException {
        int[][] token = new int[3][3];

//        Path zip = Paths.get("d:/zip4jvm/zip64/split/ferdinand.zip");
//        Path zip = Paths.get("d:/zip4jvm/aaa/split/ducati.zip");

//        Path zip = Paths.get("d:/zip4jvm/aaa/ducati-panigale-1199.zip");
        Path zip = Paths.get("d:/zip4jvm/aaa/ducati-panigale-1199-ecd.zip");
//        Path zip = Paths.get("d:/zip4jvm/aaa/ducati-panigale-1199.zip");
//        Path zip = Paths.get("d:/zip4jvm/zip64/bzip2-aes256-strong.zip");

//        Path zip = Paths.get("d:/zip4jvm/zip64/bzip2-aes256-strong.zip");
//        Path zip = Paths.get("d:/Programming/GitHub/zip4jvm/src/test/resources/secure-zip/strong/store_solid_aes256_strong_ecd.zip");

        Path destDir = Paths.get("d:/zip4jvm/aaa/bbb");

//        for (Path zip : Arrays.asList(zip1, zip2)) {
        System.out.println(zip);
//        UnzipIt.zip(zip).destDir(destDir).password("1".toCharArray()).extract();
//        ZipInfo.zip(zip).password("1".toCharArray()).printShortInfo();
        ZipInfo.zip(zip).password("1".toCharArray()).decompose(Paths.get(destDir.toString(), "store_solid_aes256_strong_ecd.zip"));
//        }
    }

}
