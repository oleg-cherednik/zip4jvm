package ru.olegcherednik.zip4jvm.engine.symlink;

import ru.olegcherednik.zip4jvm.engine.np.NamedPath;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Queue;

/**
 * @author Oleg Cherednik
 * @since 04.08.2026
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class IgnoreZipSymlinkStrategy extends ZipSymlinkStrategy {

    public static final IgnoreZipSymlinkStrategy INSTANCE = new IgnoreZipSymlinkStrategy();

    // ---------- ZipSymlinkStrategy ----------

    @Override
    protected void listSymlink(NamedPath namedPath, Queue<NamedPath> queue, List<NamedPath> res) {
        assert namedPath.isSymlink();
    }

}
