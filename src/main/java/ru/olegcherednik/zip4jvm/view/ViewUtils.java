package ru.olegcherednik.zip4jvm.view;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 13.10.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ViewUtils {

    public static String signature(int signature) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            byte code = (byte)signature;

            if (Character.isAlphabetic((char)code) || Character.isDigit((char)code))
                buf.append((char)code);
            else
                buf.append(code < 10 ? "0" + code : code);

            signature >>= 8;
        }

        return buf.toString();
    }

}
