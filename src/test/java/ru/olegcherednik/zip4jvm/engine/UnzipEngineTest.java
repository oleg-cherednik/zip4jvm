package ru.olegcherednik.zip4jvm.engine;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
//@Test
//@SuppressWarnings("FieldNamingConvention")
public class UnzipEngineTest {

//    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(UnzipEngineTest.class);
//
//    @BeforeClass
//    public static void createDir() throws IOException {
//        Files.createDirectories(rootDir);
//        aa
//    }
//
//    @AfterClass(enabled = Zip4jvmSuite.clear)
//    public static void removeDir() throws IOException {
//        Zip4jvmSuite.removeDir(rootDir);
//    }
//
//    public void shouldUnzipZipFileIntoDestinationFolderWhenDeflateSolid() throws IOException {
//        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
//
//        ZipFile.Reader zipFile = ZipFile.read(zipDeflateSolid);
//        zipFile.extract(destDir, dirNameCars);
//
//        assertThatDirectory(destDir).exists().hasDirectories(1).hasFiles(0);
//        assertThatDirectory(destDir.resolve(dirNameCars)).matches(dirCarsAssert);
//    }
//
//    public void shouldUnzipZipFileIntoDestinationFolderWhenDeflateSolidPkware() throws IOException {
//        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
//
//        ZipFile.Reader zipFile = ZipFile.read(zipDeflateSolidPkware, fileName -> Zip4jvmSuite.password);
//        zipFile.extract(destDir, dirNameCars);
//
//        assertThatDirectory(destDir).exists().hasDirectories(1).hasFiles(0);
//        assertThatDirectory(destDir.resolve(dirNameCars)).matches(dirCarsAssert);
//    }
//
//    public void shouldUnzipZipFileIntoDestinationFolderWhenDeflateSolidAes() throws IOException {
//        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
//
//        ZipFile.Reader zipFile = ZipFile.read(zipDeflateSolidAes, String::toCharArray);
//        zipFile.extract(destDir, dirNameCars);
//
//        assertThatDirectory(destDir).exists().hasDirectories(1).hasFiles(0);
//        assertThatDirectory(destDir.resolve(dirNameCars)).matches(dirCarsAssert);
//    }
//
//    public void shouldIterateOverAllEntriesWhenStoreSolidPkware() throws IOException {
//        List<String> entryNames = new ArrayList<>();
//
//        for (ZipFile.Entry entry : ZipFile.read(zipStoreSolidPkware))
//            entryNames.add(entry.getFileName());
//
//        assertThat(entryNames).containsExactlyInAnyOrder(
//                dirNameBikes + '/' + fileNameDucati,
//                dirNameBikes + '/' + fileNameHonda,
//                dirNameBikes + '/' + fileNameKawasaki,
//                dirNameBikes + '/' + fileNameSuzuki,
//                dirNameCars + '/' + fileNameBentley,
//                dirNameCars + '/' + fileNameFerrari,
//                dirNameCars + '/' + fileNameWiesmann,
//                dirNameEmpty,
//                fileNameEmpty,
//                fileNameMcdonnelDouglas,
//                fileNameOlegCherednik,
//                fileNameSaintPetersburg,
//                fileNameSigSauer);
//    }
//
//    public void shouldRetrieveStreamWithAllEntriesWhenStoreSplitAes() throws IOException {
//        List<String> entryNames = ZipFile.read(zipStoreSplitAes).stream()
//                                         .map(ZipFile.Entry::getFileName)
//                                         .collect(Collectors.toList());
//
//        assertThat(entryNames).containsExactlyInAnyOrder(
//                dirNameBikes + '/' + fileNameDucati,
//                dirNameBikes + '/' + fileNameHonda,
//                dirNameBikes + '/' + fileNameKawasaki,
//                dirNameBikes + '/' + fileNameSuzuki,
//                dirNameCars + '/' + fileNameBentley,
//                dirNameCars + '/' + fileNameFerrari,
//                dirNameCars + '/' + fileNameWiesmann,
//                dirNameEmpty,
//                fileNameEmpty,
//                fileNameMcdonnelDouglas,
//                fileNameOlegCherednik,
//                fileNameSaintPetersburg,
//                fileNameSigSauer);
//    }
//
//    public void shouldThrowNullPointerExceptionWhenArgumentIsNull() {
//        assertThatThrownBy(() -> ZipFile.read(zipStoreSplitAes).extract((String)null)).isExactlyInstanceOf(NullPointerException.class);
//    }
//
//    public void shouldThrowExceptionWhenExtractNotExistedEntry() {
//        assertThatThrownBy(() -> ZipFile.read(zipStoreSplitAes).extract("<unknown>")).isExactlyInstanceOf(FileNotFoundException.class);
//    }
//
//    public void shouldCorrectlySetLastTimeStampWhenUnzip() throws IOException, ParseException {
//        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
//        Path file = destDir.resolve("foo.txt");
//        final String str = "2014.10.29T18:10:44";
//        FileUtils.writeStringToFile(file.toFile(), "oleg.cherednik", Charsets.UTF_8);
//
//        Files.setLastModifiedTime(file, FileTime.fromMillis(convert(str)));
//
//        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
//        ZipIt.zip(zip).add(file);
//
//        Path unzipDir = destDir.resolve("unzip");
//        UnzipIt.zip(zip).destDir(unzipDir).extract();
//
//        Path fileFooUnzip = unzipDir.resolve("foo.txt");
//        assertThat(convert(Files.getLastModifiedTime(fileFooUnzip).toMillis())).isEqualTo(str);
//    }
//
//    private static long convert(String str) throws ParseException {
//        return new SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss").parse(str).getTime();
//    }
//
//    private static String convert(long time) {
//        return new SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss").format(new Date(time));
//    }

}
