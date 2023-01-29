/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.assertj;

import lombok.Getter;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;

/**
 * @author Oleg Cherednik
 * @since 27.03.2019
 */
abstract class ZipFileDecorator {

    @Getter
    protected final Path zip;
    protected final Map<String, ZipArchiveEntry> entries;
    protected final Map<String, Set<String>> map;

    protected ZipFileDecorator(Path zip) {
        this(zip, entries(zip));
    }

    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
    protected ZipFileDecorator(Path zip, Map<String, ZipArchiveEntry> entries) {
        this.zip = zip;
        this.entries = entries;
        map = walk(entries.keySet());
    }

    public boolean containsEntry(String entryName) {
        return entries.containsKey(entryName) || map.containsKey(entryName);
    }

    public ZipArchiveEntry getEntry(String entryName) {
        return entries.get(entryName);
    }

    public Set<String> getSubEntries(String entryName) {
        return map.containsKey(entryName) ? Collections.unmodifiableSet(map.get(entryName)) : Collections.emptySet();
    }

    public abstract InputStream getInputStream(ZipEntry entry);

    public String getComment() {
        try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(zip.toFile())) {
            return zipFile.getComment();
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    private static Map<String, ZipArchiveEntry> entries(Path path) {
        try (ZipFile zipFile = new ZipFile(path.toFile())) {
            Map<String, ZipArchiveEntry> map = new HashMap<>();
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                zipFile.getRawInputStream(entry);
                map.put(entry.getName(), entry);
            }

            return map;
        } catch(Zip4jvmException e) {
            throw e;
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    private static Map<String, Set<String>> walk(Set<String> entries) {
        Map<String, Set<String>> map = new HashMap<>();
        entries.forEach(entryName -> add(entryName, map));
        return map;
    }

    @SuppressWarnings("PMD.AvoidReassigningParameters")
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
