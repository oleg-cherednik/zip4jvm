package ru.olegcherednik.zip4jvm.engine;

/**
 * @author Oleg Cherednik
 * @since 12.09.2019
 */
//@Test
//@SuppressWarnings("FieldNamingConvention")
public class ZipEngineSolidTest {

//    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipEngineSolidTest.class);
//    private static final Path solidFile = rootDir.resolve("solid/src.zip");
//
//    @BeforeClass
//    public static void createDir() throws IOException {
//        Files.createDirectories(rootDir);
//    }
//
//    @AfterClass(enabled = Zip4jvmSuite.clear)
//    public static void removeDir() throws IOException {
//        Zip4jvmSuite.removeDir(rootDir);
//    }
//
//    public void shouldThrowNullPointerExceptionWhenArgumentIsNull() {
//        assertThatThrownBy(() -> new ZipEngine(null, ZipFileSettings.DEFAULT)).isExactlyInstanceOf(NullPointerException.class);
//        assertThatThrownBy(() -> new ZipEngine(zipStoreSolid, null)).isExactlyInstanceOf(NullPointerException.class);
//    }
//
//    public void shouldCreateZipFileWhenSeparateSettingsForEachFile() throws IOException {
//        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
//            if (fileNameBentley.equals(fileName))
//                return ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
//            if (fileNameFerrari.equals(fileName))
//                return ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
//            if (fileNameWiesmann.equals(fileName))
//                return ZipEntrySettings.builder()
//                                       .encryption(Encryption.PKWARE, password)
//                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
//            if (fileNameHonda.equals(fileName))
//                return ZipEntrySettings.builder()
//                                       .encryption(Encryption.AES_256, fileNameHonda.toCharArray())
//                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
//            return ZipEntrySettings.DEFAULT;
//        };
//
//        try (ZipFile.Writer zipFile = ZipFile.write(solidFile, entrySettingsProvider)) {
//            zipFile.add(fileBentley);
//            zipFile.add(fileFerrari);
//            zipFile.add(fileWiesmann);
//            zipFile.add(fileHonda);
//        }
//
//        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(solidFile, password).exists().root().hasDirectories(0).hasFiles(4);
//        assertThatZipFile(solidFile, password).file(fileNameBentley).exists().hasSize(1_395_362);
//        assertThatZipFile(solidFile, password).file(fileNameFerrari).exists().hasSize(320_894);
//        assertThatZipFile(solidFile, password).file(fileNameWiesmann).exists().hasSize(729_633);
//        assertThatZipFile(solidFile, fileNameHonda.toCharArray()).file(fileNameHonda).exists().hasSize(154_591);
//    }
//
//    @Test(dependsOnMethods = "shouldCreateZipFileWhenSeparateSettingsForEachFile")
//    public void shouldAddFilesToExistedZipWhenUseZipFile() throws IOException {
//        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
//            if (fileNameKawasaki.equals(fileName))
//                return ZipEntrySettings.builder()
//                                       .compression(Compression.STORE, CompressionLevel.NORMAL)
//                                       .encryption(Encryption.PKWARE, password).build();
//            if (fileNameSuzuki.equals(fileName))
//                return ZipEntrySettings.builder()
//                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
//                                       .encryption(Encryption.AES_256, fileNameSuzuki.toCharArray()).build();
//            return ZipEntrySettings.DEFAULT;
//        };
//
//        try (ZipFile.Writer zipFile = ZipFile.write(solidFile, entrySettingsProvider)) {
//            zipFile.add(fileKawasaki);
//            zipFile.add(fileSuzuki);
//        }
//
//        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(solidFile, password).exists().root().hasDirectories(0).hasFiles(6);
//        assertThatZipFile(solidFile, password).file(fileNameBentley).exists().hasSize(1_395_362);
//        assertThatZipFile(solidFile, password).file(fileNameFerrari).exists().hasSize(320_894);
//        assertThatZipFile(solidFile, fileNameHonda.toCharArray()).file(fileNameHonda).exists().hasSize(154_591);
//        assertThatZipFile(solidFile, password).file(fileNameKawasaki).exists().hasSize(167_026);
//        assertThatZipFile(solidFile, fileNameSuzuki.toCharArray()).file(fileNameSuzuki).exists().hasSize(287_349);
//    }
//
//    @Test(dependsOnMethods = "shouldAddFilesToExistedZipWhenUseZipFile")
//    public void shouldThrowExceptionWhenAddDuplicateEntry() throws IOException {
//        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
//            if (fileNameKawasaki.equals(fileName))
//                return ZipEntrySettings.builder()
//                                       .compression(Compression.STORE, CompressionLevel.NORMAL)
//                                       .encryption(Encryption.PKWARE, password).build();
//            return ZipEntrySettings.DEFAULT;
//        };
//
//        assertThatThrownBy(() -> {
//            try (ZipFile.Writer zipFile = ZipFile.write(solidFile, entrySettingsProvider)) {
//                zipFile.add(fileKawasaki);
//            }
//        }).isExactlyInstanceOf(EntryDuplicationException.class);
//    }
//
//    @SuppressWarnings("ConstantConditions")
//    @Test(dependsOnMethods = "shouldThrowExceptionWhenAddDuplicateEntry")
//    public void shouldThrowExceptionWhenAddNullEntry() throws IOException {
//        assertThatThrownBy(() -> {
//            try (ZipEngine engine = new ZipEngine(solidFile, ZipFileSettings.DEFAULT)) {
//                engine.addEntry((ZipFile.Entry)null);
//            }
//        }).isExactlyInstanceOf(NullPointerException.class);
//    }
//
//    @Test(dependsOnMethods = "shouldThrowExceptionWhenAddNullEntry")
//    public void shouldThrowExceptionWhenRemoveWithNullPrefix() throws IOException {
//        assertThatThrownBy(() -> {
//            try (ZipEngine engine = new ZipEngine(solidFile, ZipFileSettings.DEFAULT)) {
//                engine.remove((String)null);
//            }
//        }).isExactlyInstanceOf(NullPointerException.class);
//    }
//
//    @Test(dependsOnMethods = "shouldThrowExceptionWhenRemoveWithNullPrefix")
//    public void shouldThrowExceptionWhenRemoveWithBlankPrefix() throws IOException {
//        for (String prefixEntryName : Arrays.asList("", "  ")) {
//            assertThatThrownBy(() -> {
//                try (ZipEngine engine = new ZipEngine(solidFile, ZipFileSettings.DEFAULT)) {
//                    engine.remove(prefixEntryName);
//                }
//            }).isExactlyInstanceOf(Zip4jvmException.class);
//        }
//    }
//
//    @Test(dependsOnMethods = "shouldThrowExceptionWhenRemoveWithBlankPrefix")
//    public void shouldRemoveExistedEntityWhenNormalizePrefix() throws IOException {
//        try (ZipFile.Writer zipFile = ZipFile.write(solidFile)) {
//            zipFile.remove(fileNameKawasaki);
//        }
//
//        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(solidFile, password).exists().root().hasDirectories(0).hasFiles(5);
//    }
//
//    @Test(dependsOnMethods = "shouldRemoveExistedEntityWhenNormalizePrefix")
//    public void shouldAddDirectoryWhenZipExists() throws IOException {
//        try (ZipFile.Writer zipFile = ZipFile.write(solidFile)) {
//            zipFile.add(dirBikes);
//            zipFile.add(dirCars);
//        }
//
//        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(solidFile, password).exists().directory(zipDirNameBikes).matches(zipDirBikesAssert);
//        assertThatZipFile(solidFile, password).exists().directory(zipDirNameCars).matches(zipDirCarsAssert);
//    }
//
//    @Test(dependsOnMethods = "shouldAddDirectoryWhenZipExists")
//    public void shouldRemoveEntryWhenNormalizePrefix() throws IOException {
//        try (ZipFile.Writer zipFile = ZipFile.write(solidFile)) {
//            zipFile.remove(dirNameBikes + '/' + fileNameHonda);
//        }
//
//        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(solidFile, password).exists().directory(zipDirNameBikes).hasFiles(3);
//    }
//
//    @Test(dependsOnMethods = "shouldRemoveEntryWhenNormalizePrefix")
//    public void shouldRemoveEntryWhenNotNormalizePrefix() throws IOException {
//        try (ZipFile.Writer zipFile = ZipFile.write(solidFile)) {
//            zipFile.remove(dirNameBikes + '\\' + fileNameKawasaki);
//        }
//
//        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(solidFile, password).exists().directory(zipDirNameBikes).hasFiles(2);
//    }
//
//    @Test(dependsOnMethods = "shouldRemoveEntryWhenNotNormalizePrefix")
//    public void shouldRemoveDirectoryWhenNoDirectoryMarker() throws IOException {
//        try (ZipFile.Writer zipFile = ZipFile.write(solidFile)) {
//            zipFile.remove(dirNameBikes);
//        }
//
//        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(solidFile, password).exists().directory(zipDirNameBikes).notExists();
//    }
//
//    @Test(dependsOnMethods = "shouldRemoveDirectoryWhenNoDirectoryMarker")
//    public void shouldThrowExceptionWhenRemoveNotExistedEntry() throws IOException {
//        assertThatThrownBy(() -> {
//            try (ZipFile.Writer zipFile = ZipFile.write(solidFile)) {
//                zipFile.remove(dirNameBikes);
//            }
//        }).isExactlyInstanceOf(FileNotFoundException.class);
//    }
//
//    @Test(dependsOnMethods = "shouldThrowExceptionWhenRemoveNotExistedEntry")
//    public void shouldThrowExceptionWhenCopyNullEntry() throws IOException {
//        assertThatThrownBy(() -> {
//            try (ZipEngine engine = new ZipEngine(solidFile, ZipFileSettings.DEFAULT)) {
//                engine.copy(null);
//            }
//        }).isExactlyInstanceOf(NullPointerException.class);
//    }

}
