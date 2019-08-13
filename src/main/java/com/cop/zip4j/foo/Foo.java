package com.cop.zip4j.foo;

import de.idyl.winzipaes.AesZipFileDecrypter;
import de.idyl.winzipaes.AesZipFileEncrypter;
import de.idyl.winzipaes.impl.AESDecrypterJCA;
import de.idyl.winzipaes.impl.AESEncrypterJCA;
import de.idyl.winzipaes.impl.ExtZipEntry;

import java.io.File;

/**
 * @author Oleg Cherednik
 * @since 30.07.2019
 */
public class Foo {

    public static void main(String... args) throws Exception {
        String password = "1";
        String expected = "foo";

        byte[] buf = {
                (byte)0xC6, (byte)0xF4, (byte)0x97, (byte)0xA0, (byte)0x6,
                (byte)0x76, (byte)0x93, (byte)0x30, (byte)0x2C, (byte)0x34 };


        File zipFile = new File("d:/zip4j/tmp/aes/aes.zip");
        AesZipFileEncrypter encrypter = new AesZipFileEncrypter(zipFile, new AESEncrypterJCA());

        encrypter.add(new File("d:/zip4j/tmp/tmp.txt"), "tmp.txt", "1");
        encrypter.close();

        AesZipFileDecrypter decrypter = new AesZipFileDecrypter(zipFile, new AESDecrypterJCA());

        for (ExtZipEntry zipEntry : decrypter.getEntryList())
            decrypter.extractEntry(zipEntry, new File(zipFile.getParentFile(), zipEntry.getName()), "1");

        //AES.encrypt(expected, secret) ;
//        String actual = AES.decryptOld(buf, password);
//        String actual = AES.decryptNew(password);
//
//        System.out.println(expected);
//        System.out.println(Arrays.toString(buf));
//        System.out.println(actual);

    }
}
