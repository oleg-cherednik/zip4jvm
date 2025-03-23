package ru.olegcherednik.zip4jvm.crypto.aes;

import ru.olegcherednik.zip4jvm.utils.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Oleg Cherednik
 * @since 24.03.2025
 */
//@Test
public class WinZipCipherTest {

    public void shouldUpdateIv() throws Throwable {
        AesEngine engine = new AesEngine(mock(WinZipCipher.class));
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

    private static void setIv(AesEngine engine, byte[] iv) throws NoSuchFieldException, IllegalAccessException {
        ReflectionUtils.setFieldValue(engine, "iv", iv);
    }

}
