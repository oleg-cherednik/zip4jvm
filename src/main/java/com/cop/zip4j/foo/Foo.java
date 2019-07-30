package com.cop.zip4j.foo;

/**
 * @author Oleg Cherednik
 * @since 30.07.2019
 */
public class Foo {

    public static void main(String... args) {
        String secret = "1";
        String originalString = "Oleg Cherednik";

        String encryptedString = AES.encrypt(originalString, secret) ;
        String decryptedString = AES.decrypt(encryptedString, secret) ;

        System.out.println(originalString);
        System.out.println(encryptedString);
        System.out.println(decryptedString);

    }

}
