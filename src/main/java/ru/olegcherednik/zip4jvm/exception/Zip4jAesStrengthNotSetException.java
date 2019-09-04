package ru.olegcherednik.zip4jvm.exception;

/**
 * @author Oleg Cherednik
 * @since 15.08.2019
 */
public class Zip4jAesStrengthNotSetException extends Zip4jException {

    public Zip4jAesStrengthNotSetException() {
        super("AesStrength should be se for AES encryption", ErrorCode.AES_STRENGTH_NOT_SET);
    }
}
