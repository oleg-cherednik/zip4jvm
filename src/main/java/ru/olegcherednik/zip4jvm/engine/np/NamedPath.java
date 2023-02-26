package ru.olegcherednik.zip4jvm.engine.np;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * @author Oleg Cherednik
 * @since 24.02.2023
 */
@Getter
public abstract class NamedPath {

    public static final Comparator<NamedPath> SORT_BY_NAME_ASC = (one, two) -> {
        String[] parts1 = one.name.split("/");
        String[] parts2 = two.name.split("/");

        if (parts1.length < parts2.length)
            return -1;
        if (parts1.length > parts2.length)
            return 1;

        for (int i = 0; i < parts1.length; i++) {
            int res = parts1[i].compareTo(parts2[i]);

            if (res != 0)
                return res;
        }

        return 0;
    };

    protected final String name;

    public static NamedPath create(Path path) {
        return create(path, PathUtils.getName(path));
    }

    public static NamedPath create(Path path, String pathName) {
        if (Files.isSymbolicLink(path))
            return new Symlink(path, pathName);
        if (Files.isDirectory(path))
            return new Directory(path, pathName);
        if (Files.isRegularFile(path))
            return new RegularFile(path, pathName);

        throw new Zip4jvmException(String.format("Unknown path '%s'", path));
    }

    public static NamedPath symlink(Path symlinkTarget, String symlinkTargetRelativePath, String symlinkName) {
        return new Symlink(symlinkTarget, symlinkTargetRelativePath, symlinkName);
    }

    protected NamedPath(String name) {
        this.name = name;
    }

    public abstract ZipFile.Entry createZipEntry();

    public Path getPath() {
        return null;
    }

    public boolean isSymlink() {
        return false;
    }

    public boolean isDirectory() {
        return false;
    }

    public boolean isRegularFile() {
        return false;
    }

    @Override
    public String toString() {
        return name;
    }
}
