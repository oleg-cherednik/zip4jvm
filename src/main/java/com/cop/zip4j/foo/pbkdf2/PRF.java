package com.cop.zip4j.foo.pbkdf2;

/*
 * Source referred from Matthias Gartner's PKCS#5 implementation -
 * see http://rtner.de/software/PBKDF2.html
 */

interface PRF {

    public void init(byte[] P);

    public byte[] doFinal(byte[] M);

    public int getHLen();
}
