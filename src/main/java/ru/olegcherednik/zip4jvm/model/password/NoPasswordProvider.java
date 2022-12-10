package ru.olegcherednik.zip4jvm.model.password;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 10.12.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NoPasswordProvider implements PasswordProvider {

    public static final PasswordProvider INSTANCE = new NoPasswordProvider();

    @Override
    public char[] getFilePassword(String fileName) {
        return null;
    }

    @Override
    public char[] getCentralDirectoryPassword() {
        return null;
    }
}
