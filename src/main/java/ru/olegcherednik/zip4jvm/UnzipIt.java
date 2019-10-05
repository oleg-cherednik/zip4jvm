package ru.olegcherednik.zip4jvm;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.engine.UnzipEngine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class UnzipIt {

    private static final Function<String, char[]> DEFAULT_PASSWORD_PROVIDER = fileName -> null;

    private final Path zip;
    private Path destDir;
    private Function<String, char[]> passwordProvider = DEFAULT_PASSWORD_PROVIDER;

    public static UnzipIt zip(Path zip) {
        return new UnzipIt(zip).destDir(zip.getParent());
    }

    public UnzipIt destDir(Path destDir) {
        this.destDir = destDir;
        return this;
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public UnzipIt password(char[] password) {
        passwordProvider = ArrayUtils.isEmpty(password) ? DEFAULT_PASSWORD_PROVIDER : fileName -> password;
        return this;
    }

    public UnzipIt passwordProvider(Function<String, char[]> passwordProvider) {
        this.passwordProvider = Optional.ofNullable(passwordProvider).orElse(DEFAULT_PASSWORD_PROVIDER);
        return this;
    }

    public void extract() throws IOException {
        new UnzipEngine(zip, passwordProvider).extract(destDir);
    }

    public void extract(@NonNull String fileName) throws IOException {
        extract(Collections.singleton(fileName));
    }

    public void extract(Collection<String> fileNames) throws IOException {
        ZipFile.Reader zipFile = ZipFile.reader(zip, passwordProvider);

        for (String fileName : fileNames)
            zipFile.extract(destDir, fileName);
    }

    public InputStream stream(String fileName) throws IOException {
        return ZipFile.reader(zip, passwordProvider).extract(fileName).getInputStream();
    }

    public ZipFile.Reader open() throws IOException {
        return ZipFile.reader(zip, passwordProvider);
    }

}
