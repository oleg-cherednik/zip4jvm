package ru.olegcherednik.zip4jvm.model.password;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 10.12.2022
 */
@RequiredArgsConstructor
@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
public final class SinglePasswordProvider implements PasswordProvider {

    private final char[] password;

    @Override
    public char[] getFilePassword(String fileName) {
        return password;
    }

    @Override
    public char[] getCentralDirectoryPassword() {
        return password;
    }
}
