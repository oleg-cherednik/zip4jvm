package ru.olegcherednik.zip4jvm.crypto.aes;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.model.Encryption;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 22.09.2019
 */
@Test
public class AesEngineTest {

    public void shouldRetrieveCorrectStrengthWhenEncryption() {
        for (Encryption encryption : Encryption.values()) {
            if (encryption == Encryption.AES_128)
                assertThat(AesEngine.getStrength(Encryption.AES_128)).isSameAs(AesStrength.S128);
            else if (encryption == Encryption.AES_192)
                assertThat(AesEngine.getStrength(Encryption.AES_192)).isSameAs(AesStrength.S192);
            else if (encryption == Encryption.AES_256)
                assertThat(AesEngine.getStrength(Encryption.AES_256)).isSameAs(AesStrength.S256);
            else
                assertThat(AesEngine.getStrength(encryption)).isSameAs(AesStrength.NULL);
        }
    }

    public void shouldRetrieveCorrectEncryptionWhenAesStrength() {
        assertThat(AesEngine.getEncryption(AesStrength.NULL)).isSameAs(Encryption.OFF);
        assertThat(AesEngine.getEncryption(AesStrength.S128)).isSameAs(Encryption.AES_128);
        assertThat(AesEngine.getEncryption(AesStrength.S192)).isSameAs(Encryption.AES_192);
        assertThat(AesEngine.getEncryption(AesStrength.S256)).isSameAs(Encryption.AES_256);
    }
}
