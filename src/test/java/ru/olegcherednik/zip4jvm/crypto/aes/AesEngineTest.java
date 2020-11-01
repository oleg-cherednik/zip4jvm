package ru.olegcherednik.zip4jvm.crypto.aes;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.utils.ReflectionUtils;

import javax.crypto.Cipher;
import javax.crypto.Mac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Oleg Cherednik
 * @since 22.09.2019
 */
@Test
public class AesEngineTest {

    public void shouldRetrieveCorrectStrengthWhenEncryption() {
        for (EncryptionMethod encryptionMethod : EncryptionMethod.values()) {
            if (encryptionMethod == EncryptionMethod.AES_128)
                assertThat(AesEngine.getStrength(EncryptionMethod.AES_128)).isSameAs(AesStrength.S128);
            else if (encryptionMethod == EncryptionMethod.AES_192)
                assertThat(AesEngine.getStrength(EncryptionMethod.AES_192)).isSameAs(AesStrength.S192);
            else if (encryptionMethod == EncryptionMethod.AES_256)
                assertThat(AesEngine.getStrength(EncryptionMethod.AES_256)).isSameAs(AesStrength.S256);
            else
                assertThat(AesEngine.getStrength(encryptionMethod)).isSameAs(AesStrength.NULL);
        }
    }

    public void shouldRetrieveCorrectEncryptionWhenAesStrength() {
        assertThat(AesEngine.getEncryption(AesStrength.NULL)).isSameAs(EncryptionMethod.OFF);
        assertThat(AesEngine.getEncryption(AesStrength.S128)).isSameAs(EncryptionMethod.AES_128);
        assertThat(AesEngine.getEncryption(AesStrength.S192)).isSameAs(EncryptionMethod.AES_192);
        assertThat(AesEngine.getEncryption(AesStrength.S256)).isSameAs(EncryptionMethod.AES_256);
    }

    public void shouldUpdateIv() throws Throwable {
        AesEngine engine = new AesEngine(mock(Cipher.class), mock(Mac.class));
        assertThat(getIv(engine)).isEqualTo(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

        ivUpdate(engine);
        assertThat(getIv(engine)).isEqualTo(new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

        ivUpdate(engine);
        assertThat(getIv(engine)).isEqualTo(new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

        setIv(engine, new byte[] { -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
        ivUpdate(engine);
        assertThat(getIv(engine)).isEqualTo(new byte[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

        setIv(engine, new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 });
        ivUpdate(engine);
        assertThat(getIv(engine)).isEqualTo(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
    }

    private static byte[] getIv(AesEngine engine) throws NoSuchFieldException, IllegalAccessException {
        return ReflectionUtils.getFieldValue(engine, "iv");
    }

    private static void ivUpdate(AesEngine engine) throws Throwable {
        ReflectionUtils.invokeMethod(engine, "ivUpdate");
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static void setIv(AesEngine engine, byte[] iv) throws NoSuchFieldException, IllegalAccessException {
        ReflectionUtils.setFieldValue(engine, "iv", iv);
    }
}
