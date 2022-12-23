package ru.olegcherednik.zip4jvm.model.password;

/**
 * @author Oleg Cherednik
 * @since 10.12.2022
 */
public interface PasswordProvider {

    char[] getFilePassword(String fileName);

    char[] getCentralDirectoryPassword();

}
