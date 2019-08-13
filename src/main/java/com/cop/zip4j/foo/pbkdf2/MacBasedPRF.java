package com.cop.zip4j.foo.pbkdf2;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/*
 * Source referred from Matthias Gartner's PKCS#5 implementation -
 * see http://rtner.de/software/PBKDF2.html
 */

public class MacBasedPRF implements PRF {

    protected Mac mac;

    protected int hLen;

    protected String macAlgorithm;

    public MacBasedPRF(String macAlgorithm) {
        this.macAlgorithm = macAlgorithm;
        try {
            mac = Mac.getInstance(macAlgorithm);
            hLen = mac.getMacLength();
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public MacBasedPRF(String macAlgorithm, String provider) {
        this.macAlgorithm = macAlgorithm;
        try {
            mac = Mac.getInstance(macAlgorithm, provider);
            hLen = mac.getMacLength();
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch(NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] doFinal(byte[] M) {
        byte[] r = mac.doFinal(M);
        return r;
    }

    public byte[] doFinal() {
        byte[] r = mac.doFinal();
        return r;
    }

    public int getHLen() {
        return hLen;
    }

    public void init(byte[] P) {
        try {
            mac.init(new SecretKeySpec(P, macAlgorithm));
        } catch(InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(byte[] U) {

        try {
            mac.update(U);
        } catch(IllegalStateException e) {
            throw new RuntimeException(e);
        }

    }

    public void update(byte[] U, int start, int len) {
        try {
            mac.update(U, start, len);
        } catch(IllegalStateException e) {
            throw new RuntimeException(e);
        }
    }
}
