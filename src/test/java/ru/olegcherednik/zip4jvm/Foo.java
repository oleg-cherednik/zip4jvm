package ru.olegcherednik.zip4jvm;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;

import org.apache.commons.compress.archivers.zip.ZipFile;

/**
 * @author Oleg Cherednik
 * @since 19.10.2022
 */
public class Foo {

    public static void main(String[] args) throws IOException {
        Path zip = Paths.get("d:/zip4jvm/aaa/central.zip");
        Path destDir = Paths.get("d:/zip4jvm/aaa/bbb");

        UnzipIt.zip(zip).destDir(destDir).password("1".toCharArray()).extract();
//        ZipInfo.zip(zip).printShortInfo();
    }

}
