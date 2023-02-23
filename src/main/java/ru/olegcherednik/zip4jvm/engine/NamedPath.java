package ru.olegcherednik.zip4jvm.engine;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@Getter
final class NamedPath {

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

    private final Path path;
    private final String name;

    private final boolean symlink;
    private final boolean exist;
    private final boolean regularFile;
    private final boolean directory;

    public NamedPath(Path path) {
        this(path, PathUtils.getName(path));
    }

    public NamedPath(Path path, String name) {
        this.path = path;
        this.name = name;

        symlink = Files.isSymbolicLink(path);
        exist = Files.exists(path);
        regularFile = Files.isRegularFile(path);
        directory = Files.isDirectory(path);
    }

    public NamedPath(String name, boolean symlink) {
        path = null;
        this.name = name;

        this.symlink = symlink;
        exist = true;
        regularFile = false;
        directory = false;
    }

    @Override
    public String toString() {
        return symlink ? name + " (symlink)" : name;
    }
}
