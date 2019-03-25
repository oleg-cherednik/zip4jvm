package net.lingala.zip4j.assertj;

import org.assertj.core.api.AbstractAssert;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
@SuppressWarnings("NewClassNamingConvention")
public abstract class AbstractZipEntryAssert<SELF extends AbstractZipEntryAssert<SELF>> extends AbstractAssert<SELF, ZipEntry> {

    protected final ZipFile zipFile;

    protected AbstractZipEntryAssert(ZipEntry actual, Class<?> selfType, ZipFile zipFile) {
        super(actual, selfType);
        this.zipFile = zipFile;
    }

    public SELF exists() {
        isNotNull();
        assertThat(entries()).contains(actual.getName());
        return myself;
    }

    protected Set<String> entries() {
        Set<String> res = new HashSet<>();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements())
            res.add(entries.nextElement().getName());

        return res;
    }

    protected Map<String, Set<String>> walk() {
        Map<String, Set<String>> map = new HashMap<>();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements())
            add(entries.nextElement().getName(), map);

        return map;
    }

    protected static void add(String entryName, Map<String, Set<String>> map) {
        int offs = 0;
        String parent = "/";

        while (parent != null) {
            map.computeIfAbsent(parent, val -> new HashSet<>());

            int pos = entryName.indexOf('/', offs);

            if (pos >= 0) {
                String part = entryName.substring(offs, pos + 1);
                String path = entryName.substring(0, pos + 1);

                map.computeIfAbsent(path, val -> new HashSet<>());
                map.get(parent).add(part);

                offs = pos + 1;
                parent = path;
            } else {
                if (offs < entryName.length())
                    map.get(parent).add(entryName.substring(offs));
                parent = null;
            }
        }
    }
}
