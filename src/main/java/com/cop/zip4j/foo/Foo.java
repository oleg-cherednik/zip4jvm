package com.cop.zip4j.foo;

import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 30.07.2019
 */
public class Foo {

    public static void main(String... args) {
        String secret = "1";
        String expected = "foo";

        byte[] buf = { (byte)0x71, (byte)0x6E, (byte)0xFF };//AES.encrypt(expected, secret) ;
        String actual = AES.decrypt(buf, secret);

        System.out.println(expected);
        System.out.println(Arrays.toString(buf));
        System.out.println(actual);

    }

}
