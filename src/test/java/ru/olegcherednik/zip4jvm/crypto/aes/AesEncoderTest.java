package ru.olegcherednik.zip4jvm.crypto.aes;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.ReflectionUtils;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.ShortBufferException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * @author Oleg Cherednik
 * @since 03.10.2019
 */
@SuppressWarnings("MethodCanBeVariableArityMethod")
@Test
public class AesEncoderTest {

    public void shouldThrowZip4jvmExceptionWhenCreateAndException() {
        assertThatThrownBy(() -> AesEncoder.create(mock(ZipEntry.class))).isExactlyInstanceOf(Zip4jvmException.class);
    }

    public void shouldThrowZip4jvmExceptionWhenEncryptAndException() throws ShortBufferException {
        Cipher cipher = mock(Cipher.class);
        Mac mac = mock(Mac.class);
        byte[] salt = { 0, 0, 0 };
        byte[] passwordChecksum = { 0, 0, 0 };

        AesEncoder encoder = createAesEncoder(cipher, mac, salt, passwordChecksum);
        assertThatThrownBy(() -> encoder.encrypt(ArrayUtils.EMPTY_BYTE_ARRAY, 0, 10)).isExactlyInstanceOf(Zip4jvmException.class);
    }

    private static AesEncoder createAesEncoder(Cipher cipher, Mac mac, byte[] salt, byte[] passwordChecksum) {
        return ReflectionUtils.invokeConstructor(AesEncoder.class, new Class<?>[] { Cipher.class, Mac.class, byte[].class, byte[].class },
                cipher, mac, salt, passwordChecksum);
    }
}
