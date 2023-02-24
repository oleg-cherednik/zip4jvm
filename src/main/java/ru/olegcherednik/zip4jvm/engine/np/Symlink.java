package ru.olegcherednik.zip4jvm.engine.np;

import ru.olegcherednik.zip4jvm.ZipFile;

import java.nio.file.Path;

final class Symlink extends NamedPath {

    private final Path symlink;
    private final String symlinkTargetRelativePath;

    public Symlink(Path symlink, String symlinkName) {
        super(symlinkName);
        this.symlink = symlink;
        symlinkTargetRelativePath = null;
    }

    public Symlink(String symlinkTargetRelativePath, String symlinkName) {
        super(symlinkName);
        symlink = null;
        this.symlinkTargetRelativePath = symlinkTargetRelativePath;
    }

    @Override
    public ZipFile.Entry createZipEntry() {
        return ZipFile.Entry.symlink(symlinkTargetRelativePath, name);
    }

    @Override
    public Path getPath() {
        return symlink;
    }

    @Override
    public boolean isSymlink() {
        return true;
    }

    @Override
    public String toString() {
        return name + " (symlink)";
    }
}
