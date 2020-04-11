package ru.olegcherednik.zip4jvm.crypto.strong;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Oleg Cherednik
 * @since 31.03.2020
 */
@Getter
@Setter
public class Recipient {

    // size:2 = combined size of followed fields (w)
    private int size;
    // size:p - hash of Public Key
    private byte[] hash;
    // size:(w - p) - Simple Key Blob
    private byte[] simpleKeyBlob;
}
