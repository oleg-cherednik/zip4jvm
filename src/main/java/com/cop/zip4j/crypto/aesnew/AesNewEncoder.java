package com.cop.zip4j.crypto.aesnew;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.io.SplitOutputStream;
import com.cop.zip4j.model.AesStrength;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 30.07.2019
 */
public class AesNewEncoder implements Encoder {

    public AesNewEncoder(char[] password, AesStrength aesKeyStrength) {

    }

    @Override
    public void encrypt(byte[] buf, int offs, int len) {

    }

    @Override
    public void writeHeader(SplitOutputStream out) throws IOException {

    }
}
