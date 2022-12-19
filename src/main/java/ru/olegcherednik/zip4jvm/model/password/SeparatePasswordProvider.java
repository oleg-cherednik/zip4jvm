package ru.olegcherednik.zip4jvm.model.password;

import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 11.12.2022
 */
@RequiredArgsConstructor
public final class SeparatePasswordProvider implements PasswordProvider {

    private final Map<String, char[]> fileNamePassword;
    private final char[] centralDirectoryPassword;

    @Override
    public char[] getFilePassword(String fileName) {
        return fileNamePassword.get(fileName);
    }

    @Override
    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
    public char[] getCentralDirectoryPassword() {
        return centralDirectoryPassword;
    }
}
