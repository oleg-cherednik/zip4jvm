package ru.olegcherednik.zip4jvm.engine.np;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@Getter
public class NamedPath {

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
            return symlink(path, pathName);
        if (Files.isDirectory(path))
            return directory(path, pathName);
        if (Files.isRegularFile(path))
            return regularFile(path, pathName);

        throw new Zip4jvmException("Unknown path type");
    }

    public static NamedPath directory(Path dir, String dirName) {
        return new Directory(dir, dirName);
    }

    public static NamedPath regularFile(Path file, String fileName) {
        return new RegularFile(file, fileName);
    }

    public static NamedPath symlink(Path symlink, String symlinkName) {
        return new Symlink(symlink, symlinkName);
    }

    public static NamedPath symlink(String symlinkTargetRelativePath, String symlinkName) {
        return new Symlink(symlinkTargetRelativePath, symlinkName);
    }

    protected NamedPath(String name) {
        this.name = name;
    }

    public ZipFile.Entry createZipEntry() {
        return ZipFile.Entry.of(getPath(), name);
    }

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
