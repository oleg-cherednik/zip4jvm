package ru.olegcherednik.zip4jvm.engine.np;

import ru.olegcherednik.zip4jvm.ZipFile;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 24.02.2023
 */
final class Symlink extends NamedPath {

    private final Path symlinkTarget;
    private final String symlinkTargetRelativePath;

    public Symlink(Path symlinkTarget, String symlinkName) {
        this(symlinkTarget, null, symlinkName);
    }

    public Symlink(Path symlinkTarget, String symlinkTargetRelativePath, String symlinkName) {
        super(symlinkName);
        this.symlinkTarget = symlinkTarget;
        this.symlinkTargetRelativePath = symlinkTargetRelativePath;
    }

    @Override
    public ZipFile.Entry createZipFileEntry() {
        return ZipFile.Entry.symlink(symlinkTarget, symlinkTargetRelativePath, name);
    }

    @Override
    public Path getPath() {
        return symlinkTarget;
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
