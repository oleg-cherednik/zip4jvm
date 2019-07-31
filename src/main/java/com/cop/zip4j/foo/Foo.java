package com.cop.zip4j.foo;

import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 30.07.2019
 */
public class Foo {

    public static void main(String... args) throws Exception {
        String secret = "1";
        String expected = "foo";

        byte[] buf = {
                (byte)0xC6, (byte)0xF4, (byte)0x97, (byte)0xA0, (byte)0x6,
                (byte)0x76, (byte)0x93, (byte)0x30, (byte)0x2C, (byte)0x34 };

        //AES.encrypt(expected, secret) ;
        String actual = AES.decryptOld(buf, secret);

        System.out.println(expected);
        System.out.println(Arrays.toString(buf));
        System.out.println(actual);

    }
}
