package ru.olegcherednik.zip4jvm.utils;

import lombok.NonNull;
import org.mozilla.universalchardet.UniversalDetector;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 18.03.2019
 */
public final class CreateStringFunc implements Function<byte[], String> {

    @Override
    public String apply(byte[] buf) {
        return new String(buf, detectCharset(buf));
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static Charset detectCharset(@NonNull byte[] buf) {
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(buf, 0, buf.length);
        detector.dataEnd();

        String charsetName = detector.getDetectedCharset();
        return charsetName != null ? Charset.forName(charsetName) : StandardCharsets.UTF_8;
    }
}
