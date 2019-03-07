/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lingala.zip4j.util;

import lombok.NonNull;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class Zip4jUtil {

    public static boolean isStringNotNullAndNotEmpty(String str) {
        if (str == null || str.trim().length() <= 0) {
            return false;
        }

        return true;
    }

    public static boolean checkOutputFolder(String path) throws ZipException {
        if (!isStringNotNullAndNotEmpty(path)) {
            throw new ZipException(new NullPointerException("output path is null"));
        }

        File file = new File(path);

        if (file.exists()) {

            if (!file.isDirectory()) {
                throw new ZipException("output folder is not valid");
            }

            if (!file.canWrite()) {
                throw new ZipException("no write access to output folder");
            }
        } else {
            try {
                file.mkdirs();
                if (!file.isDirectory()) {
                    throw new ZipException("output folder is not valid");
                }

                if (!file.canWrite()) {
                    throw new ZipException("no write access to destination folder");
                }

//				SecurityManager manager = new SecurityManager();
//				try {
//					manager.checkWrite(file.getAbsolutePath());
//				} catch (Exception e) {
//					e.printStackTrace();
//					throw new ZipException("no write access to destination folder");
//				}
            } catch(Exception e) {
                throw new ZipException("Cannot create destination folder");
            }
        }

        return true;
    }

    public static boolean checkFileReadAccess(String path) throws ZipException {
        if (!isStringNotNullAndNotEmpty(path)) {
            throw new ZipException("path is null");
        }

        if (!checkFileExists(path)) {
            throw new ZipException("file does not exist: " + path);
        }

        try {
            File file = new File(path);
            return file.canRead();
        } catch(Exception e) {
            throw new ZipException("cannot read zip file");
        }
    }

    public static boolean checkFileWriteAccess(String path) throws ZipException {
        if (!isStringNotNullAndNotEmpty(path)) {
            throw new ZipException("path is null");
        }

        if (!checkFileExists(path)) {
            throw new ZipException("file does not exist: " + path);
        }

        try {
            File file = new File(path);
            return file.canWrite();
        } catch(Exception e) {
            throw new ZipException("cannot read zip file");
        }
    }

    public static boolean checkFileExists(String path) throws ZipException {
        if (!isStringNotNullAndNotEmpty(path)) {
            throw new ZipException("path is null");
        }

        File file = new File(path);
        return checkFileExists(file);
    }

    public static boolean checkFileExists(File file) throws ZipException {
        if (file == null) {
            throw new ZipException("cannot check if file exists: input file is null");
        }
        return file.exists();
    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win") >= 0);
    }

    public static void setFileReadOnly(File file) throws ZipException {
        if (file == null) {
            throw new ZipException("input file is null. cannot set read only file attribute");
        }

        if (file.exists()) {
            file.setReadOnly();
        }
    }

    public static void setFileHidden(File file) throws ZipException {
//		if (file == null) {
//			throw new ZipException("input file is null. cannot set hidden file attribute");
//		}
//
//		if (!isWindows()) {
//			return;
//		}
//
//		if (file.exists()) {
//			try {
//				Runtime.getRuntime().exec("attrib +H \"" + file.getAbsolutePath() + "\"");
//			} catch (IOException e) {
//				// do nothing as this is not of a higher priority
//				// add log statements here when logging is done
//			}
//		}
    }

    public static void setFileArchive(File file) throws ZipException {
//		if (file == null) {
//			throw new ZipException("input file is null. cannot set archive file attribute");
//		}
//
//		if (!isWindows()) {
//			return;
//		}
//
//		if (file.exists()) {
//			try {
//				if (file.isDirectory()) {
//					Runtime.getRuntime().exec("attrib +A \"" + file.getAbsolutePath() + "\"");
//				} else {
//					Runtime.getRuntime().exec("attrib +A \"" + file.getAbsolutePath() + "\"");
//				}
//
//			} catch (IOException e) {
//				// do nothing as this is not of a higher priority
//				// add log statements here when logging is done
//			}
//		}
    }

    public static void setFileSystemMode(File file) throws ZipException {
//		if (file == null) {
//			throw new ZipException("input file is null. cannot set archive file attribute");
//		}
//
//		if (!isWindows()) {
//			return;
//		}
//
//		if (file.exists()) {
//			try {
//				Runtime.getRuntime().exec("attrib +S \"" + file.getAbsolutePath() + "\"");
//			} catch (IOException e) {
//				// do nothing as this is not of a higher priority
//				// add log statements here when logging is done
//			}
//		}
    }

    public static long getLastModifiedFileTime(File file, TimeZone timeZone) throws ZipException {
        if (file == null) {
            throw new ZipException("input file is null, cannot read last modified file time");
        }

        if (!file.exists()) {
            throw new ZipException("input file does not exist, cannot read last modified file time");
        }

        return file.lastModified();
    }

    public static String getFileNameFromFilePath(File file) throws ZipException {
        if (file == null) {
            throw new ZipException("input file is null, cannot get file name");
        }

        if (file.isDirectory()) {
            return null;
        }

        return file.getName();
    }

    public static long getFileLengh(String file) throws ZipException {
        if (!isStringNotNullAndNotEmpty(file)) {
            throw new ZipException("invalid file name");
        }

        return getFileLengh(new File(file));
    }

    public static long getFileLengh(File file) throws ZipException {
        if (file == null) {
            throw new ZipException("input file is null, cannot calculate file length");
        }

        if (file.isDirectory()) {
            return -1;
        }

        return file.length();
    }

    /**
     * Converts input time from Java to DOS format
     *
     * @param time
     * @return time in DOS format
     */
    public static long javaToDosTime(long time) {

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);

        int year = cal.get(Calendar.YEAR);
        if (year < 1980) {
            return (1 << 21) | (1 << 16);
        }
        return (year - 1980) << 25 | (cal.get(Calendar.MONTH) + 1) << 21 |
                cal.get(Calendar.DATE) << 16 | cal.get(Calendar.HOUR_OF_DAY) << 11 | cal.get(Calendar.MINUTE) << 5 |
                cal.get(Calendar.SECOND) >> 1;
    }

    /**
     * Converts time in dos format to Java format
     *
     * @param dosTime
     * @return time in java format
     */
    public static long dosToJavaTme(int dosTime) {
        int sec = 2 * (dosTime & 0x1f);
        int min = (dosTime >> 5) & 0x3f;
        int hrs = (dosTime >> 11) & 0x1f;
        int day = (dosTime >> 16) & 0x1f;
        int mon = ((dosTime >> 21) & 0xf) - 1;
        int year = ((dosTime >> 25) & 0x7f) + 1980;

        Calendar cal = Calendar.getInstance();
        cal.set(year, mon, day, hrs, min, sec);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime().getTime();
    }

    public static int getIndexOfFileHeader(ZipModel zipModel,
            CentralDirectory.FileHeader fileHeader) throws ZipException {

        if (zipModel == null || fileHeader == null) {
            throw new ZipException("input parameters is null, cannot determine index of file header");
        }

        if (zipModel.getCentralDirectory() == null) {
            throw new ZipException("central directory is null, ccannot determine index of file header");
        }

        if (zipModel.getCentralDirectory().getFileHeaders() == null) {
            throw new ZipException("file Headers are null, cannot determine index of file header");
        }

        if (zipModel.getCentralDirectory().getFileHeaders().size() <= 0) {
            return -1;
        }
        String fileName = fileHeader.getFileName();

        if (!isStringNotNullAndNotEmpty(fileName)) {
            throw new ZipException("file name in file header is empty or null, cannot determine index of file header");
        }

        List<CentralDirectory.FileHeader> fileHeaders = zipModel.getCentralDirectory().getFileHeaders();
        for (int i = 0; i < fileHeaders.size(); i++) {
            CentralDirectory.FileHeader fileHeaderTmp = fileHeaders.get(i);
            String fileNameForHdr = fileHeaderTmp.getFileName();
            if (!isStringNotNullAndNotEmpty(fileNameForHdr)) {
                continue;
            }

            if (fileName.equalsIgnoreCase(fileNameForHdr)) {
                return i;
            }
        }
        return -1;
    }

    public static List<File> getFilesInDirectoryRec(File path,
            boolean readHiddenFiles) throws ZipException {

        if (path == null) {
            throw new ZipException("input path is null, cannot read files in the directory");
        }

        List<File> result = new ArrayList<>();
        File[] filesAndDirs = path.listFiles();
        List<File> filesDirs = Arrays.asList(filesAndDirs);

        if (!path.canRead()) {
            return result;
        }

        for (int i = 0; i < filesDirs.size(); i++) {
            File file = (File)filesDirs.get(i);
            if (file.isHidden() && !readHiddenFiles) {
                return result;
            }
            result.add(file);
            if (file.isDirectory()) {
                List<File> deeperList = getFilesInDirectoryRec(file, readHiddenFiles);
                result.addAll(deeperList);
            }
        }
        return result;
    }

    public static String getZipFileNameWithoutExt(String zipFile) throws ZipException {
        if (!isStringNotNullAndNotEmpty(zipFile)) {
            throw new ZipException("zip file name is empty or null, cannot determine zip file name");
        }
        String tmpFileName = zipFile;
        if (zipFile.indexOf(System.getProperty("file.separator")) >= 0) {
            tmpFileName = zipFile.substring(zipFile.lastIndexOf(System.getProperty("file.separator")));
        }

        if (tmpFileName.indexOf(".") > 0) {
            tmpFileName = tmpFileName.substring(0, tmpFileName.lastIndexOf("."));
        }
        return tmpFileName;
    }

//    public static byte[] convertCharset(String str) throws UnsupportedEncodingException {
//        return str.getBytes(detectCharset(str));
//    }

    /**
     * Decodes file name based on encoding. If file name is UTF 8 encoded
     * returns an UTF8 encoded string, else return Cp850 encoded String. If
     * appropriate charset is not supported, then returns a System default
     * charset encoded String
     *
     * @param data
     * @param isUTF8
     * @return String
     */
    public static String decodeFileName(byte[] data, boolean isUTF8) {
        return new String(data, isUTF8 ? StandardCharsets.UTF_8 : Charset.defaultCharset());
    }

    /**
     * Returns an absoulte path for the given file path
     *
     * @param filePath
     * @return String
     */
    public static String getAbsoluteFilePath(String filePath) throws ZipException {
        if (!isStringNotNullAndNotEmpty(filePath)) {
            throw new ZipException("filePath is null or empty, cannot get absolute file path");
        }

        File file = new File(filePath);
        return file.getAbsolutePath();
    }

    /**
     * Detects the encoding charset for the input string
     *
     * @return String - charset for the String
     * @throws ZipException - if input string is null. In case of any other exception
     *                      this method returns default System charset
     */
    @NonNull
    public static Charset detectCharset(@NonNull byte[] buf) throws UnsupportedEncodingException {
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(buf, 0, buf.length);
        detector.dataEnd();

        String charsetName = detector.getDetectedCharset();
        return charsetName != null ? Charset.forName(charsetName) : Charset.defaultCharset();
//        if (checkCharset(str, Charset.forName("Cp850")))
//            return Charset.forName("Cp850");
//        if (checkCharset(str, Charset.forName("Windows-1251")))
//            return Charset.forName("Windows-1251");
//        if (checkCharset(str, StandardCharsets.UTF_8))
//            return StandardCharsets.UTF_8;
//        return Charset.defaultCharset();
    }

//    private static boolean checkCharset(@NonNull String str, @NonNull Charset charset) {
//        return str.equals(new String(str.getBytes(charset), charset));
//    }

    /**
     * returns the length of the string by wrapping it in a byte buffer with
     * the appropriate charset of the input string and returns the limit of the
     * byte buffer
     *
     * @param str
     * @return length of the string
     * @throws ZipException
     */
//    public static int getEncodedStringLength(@NonNull String str) throws ZipException, UnsupportedEncodingException {
//        return getEncodedStringLength(str, detectCharset(str));
//    }

    /**
     * returns the length of the string in the input encoding
     *
     * @param str
     * @param charset
     * @return int
     */
    public static int getEncodedStringLength(String str, @NonNull Charset charset) {
        return StringUtils.isBlank(str) ? 0 : ByteBuffer.wrap(str.getBytes(charset)).limit();
    }

    /**
     * Checks if the input charset is supported
     *
     * @param charset
     * @return boolean
     * @throws ZipException
     */
    public static boolean isSupportedCharset(String charset) throws ZipException {
        if (!isStringNotNullAndNotEmpty(charset)) {
            throw new ZipException("charset is null or empty, cannot check if it is supported");
        }

        try {
            new String("a".getBytes(), charset);
            return true;
        } catch(UnsupportedEncodingException e) {
            return false;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    public static List<File> getSplitZipFiles(ZipModel zipModel) throws ZipException {
        if (zipModel == null) {
            throw new ZipException("cannot get split zip files: zipmodel is null");
        }

        if (zipModel.getEndCentralDirectory() == null) {
            return null;
        }

        List<File> retList = new ArrayList<>();
        String currZipFile = zipModel.getZipFile().toString();
        String zipFileName = new File(currZipFile).getName();
        String partFile = null;

        if (!isStringNotNullAndNotEmpty(currZipFile)) {
            throw new ZipException("cannot get split zip files: zipfile is null");
        }

        if (!zipModel.isSplitArchive()) {
            retList.add(zipModel.getZipFile().toFile());
            return retList;
        }

        int numberOfThisDisk = zipModel.getEndCentralDirectory().getNoOfDisk();

        if (numberOfThisDisk == 0) {
            retList.add(zipModel.getZipFile().toFile());
            return retList;
        } else {
            for (int i = 0; i <= numberOfThisDisk; i++) {
                if (i == numberOfThisDisk) {
                    retList.add(zipModel.getZipFile().toFile());
                } else {
                    String fileExt = ".z0";
                    if (i > 9) {
                        fileExt = ".z";
                    }
                    partFile = (zipFileName.indexOf(".") >= 0) ? currZipFile.substring(0, currZipFile.lastIndexOf(".")) : currZipFile;
                    partFile = partFile + fileExt + (i + 1);
                    retList.add(new File(partFile));
                }
            }
        }
        return retList;
    }

    public static String getRelativeFileName(Path file, ZipParameters parameters) throws ZipException {
        return getRelativeFileName(file.toString(), parameters.getRootFolderInZip(), parameters.getDefaultFolderPath());
    }

    public static String getRelativeFileName(String file, String rootFolderInZip, String rootFolderPath) throws ZipException {
        Path entryPath = Paths.get(file).toAbsolutePath();
        Path rootPath = rootFolderPath != null ? Paths.get(rootFolderPath).toAbsolutePath() : entryPath.getParent();

        String path = rootPath.relativize(entryPath).toString();

        if (Files.isDirectory(entryPath))
            path += File.separator;

        if (rootFolderInZip != null)
            path = FilenameUtils.concat(path, rootFolderInZip);

        return path;
    }

    public static long[] getAllHeaderSignatures() {
        long[] allSigs = new long[11];

        allSigs[0] = InternalZipConstants.LOCSIG;
        allSigs[1] = InternalZipConstants.EXTSIG;
        allSigs[2] = InternalZipConstants.CENSIG;
        allSigs[3] = InternalZipConstants.ENDSIG;
        allSigs[4] = InternalZipConstants.DIGSIG;
        allSigs[5] = InternalZipConstants.ARCEXTDATREC;
        allSigs[6] = InternalZipConstants.SPLITSIG;
        allSigs[7] = InternalZipConstants.ZIP64_ENDSIG_LOC;
        allSigs[8] = InternalZipConstants.ZIP64_ENDSIG;
        allSigs[9] = InternalZipConstants.EXTRAFIELDZIP64LENGTH;
        allSigs[10] = InternalZipConstants.AESSIG;

        return allSigs;
    }
}
