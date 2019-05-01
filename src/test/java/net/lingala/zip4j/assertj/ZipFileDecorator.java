package net.lingala.zip4j.assertj;

import lombok.Getter;
import lombok.NonNull;
import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Oleg Cherednik
 * @since 27.03.2019
 */
final class ZipFileDecorator {

    @Getter
    private final Path zipFile;
    private final Map<String, ZipEntry> entries;
    private final Map<String, Set<String>> map;
    private final String password;

    public ZipFileDecorator(Path zipFile) {
        this(zipFile, null);
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public ZipFileDecorator(Path zipFile, char[] password) {
        this.zipFile = zipFile;
        this.password = password != null ? new String(password) : null;
        entries = entries(zipFile);
        map = walk(entries.keySet());
    }

    public boolean containsEntry(String entryName) {
        return entries.containsKey(entryName) || map.containsKey(entryName);
    }

    public Set<String> getSubEntries(String entryName) {
        return map.containsKey(entryName) ? Collections.unmodifiableSet(map.get(entryName)) : Collections.emptySet();
    }

    public InputStream getInputStream(@NonNull ZipEntry entry) {
        if (password == null) {
            try {
                return new ZipFile(zipFile.toFile()).getInputStream(entry);
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try (IInStream in = new RandomAccessFileInStream(new RandomAccessFile(zipFile.toFile(), "r"));
                 IInArchive zip = SevenZip.openInArchive(ArchiveFormat.ZIP, in)) {

                for (ISimpleInArchiveItem item : zip.getSimpleInterface().getArchiveItems()) {
                    String name = getItemName(item);

                    if (!name.equals(entry.getName()))
                        continue;

                    return getInputStream(item);
                }

                throw new RuntimeException("Entry '" + entry + "' was not found");
            } catch(RuntimeException e) {
                throw e;
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private InputStream getInputStream(ISimpleInArchiveItem item) throws SevenZipException {
        List<byte[]> tmp = new ArrayList<>();

        if (item.getSize() == 0)
            tmp.add(ArrayUtils.EMPTY_BYTE_ARRAY);
        else {
            ExtractOperationResult res = item.extractSlow(data -> {
                tmp.add(data);
                return ArrayUtils.getLength(data);
            }, password);

            if (tmp.isEmpty() || res != ExtractOperationResult.OK)
                throw new RuntimeException("Cannot extract zip entry");
        }

        int size = tmp.stream().mapToInt(buf -> buf.length).sum();
        byte[] buf = new byte[size];
        int offs = 0;

        for (byte[] data : tmp) {
            System.arraycopy(data, 0, buf, offs, data.length);
            offs += data.length;
        }

        return new ByteArrayInputStream(buf);
    }

    public String getComment() {
        try (ZipFile zipFile = new ZipFile(this.zipFile.toFile())) {
            return zipFile.getComment();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, ZipEntry> entries(Path path) {
        try (IInStream in = new RandomAccessFileInStream(new RandomAccessFile(path.toFile(), "r"));
             IInArchive zip = SevenZip.openInArchive(ArchiveFormat.ZIP, in)) {
            Map<String, ZipEntry> map = new LinkedHashMap<>();

            for (ISimpleInArchiveItem item : zip.getSimpleInterface().getArchiveItems()) {
                String name = getItemName(item);
                map.put(name, new ZipEntry(name));
            }

            return map;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getItemName(ISimpleInArchiveItem item) throws SevenZipException {
        String name = FilenameUtils.normalize(item.getPath(), true);
        return item.isFolder() ? name + '/' : name;
    }

    private static Map<String, Set<String>> walk(Set<String> entries) {
        Map<String, Set<String>> map = new HashMap<>();
        entries.forEach(entryName -> add(entryName, map));
        return map;
    }

    private static void add(String entryName, Map<String, Set<String>> map) {
        if ("/".equals(entryName))
            return;
        if (entryName.charAt(0) == '/')
            entryName = entryName.substring(1);

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
