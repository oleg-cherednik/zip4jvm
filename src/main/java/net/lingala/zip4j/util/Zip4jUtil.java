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
import org.apache.commons.lang.StringUtils;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@SuppressWarnings("MethodCanBeVariableArityMethod")
public class Zip4jUtil {

    public static boolean checkFileReadAccess(String path) throws ZipException {
        if (StringUtils.isBlank(path)) {
            throw new ZipException("path is null");
        }

        if (!new File(path).exists()) {
            throw new ZipException("file does not exist: " + path);
        }

        try {
            File file = new File(path);
            return file.canRead();
        } catch(Exception e) {
            throw new ZipException("cannot read zip file");
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

        if (zipModel.isEmpty())
            return -1;

        String fileName = fileHeader.getFileName();

        if (StringUtils.isBlank(fileName)) {
            throw new ZipException("file name in file header is empty or null, cannot determine index of file header");
        }

        List<CentralDirectory.FileHeader> fileHeaders = zipModel.getFileHeaders();
        for (int i = 0; i < fileHeaders.size(); i++) {
            CentralDirectory.FileHeader fileHeaderTmp = fileHeaders.get(i);
            String fileNameForHdr = fileHeaderTmp.getFileName();
            if (StringUtils.isBlank(fileNameForHdr)) {
                continue;
            }

            if (fileName.equalsIgnoreCase(fileNameForHdr)) {
                return i;
            }
        }
        return -1;
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
    }

    /**
     * returns the length of the string in the input encoding
     *
     * @param str
     * @param charset
     * @return int
     */
    public static int getEncodedStringLength(String str, @NonNull Charset charset) {
        return StringUtils.isBlank(str) ? 0 : str.getBytes(charset).length;
    }

    /**
     * Checks if the input charset is supported
     *
     * @param charset
     * @return boolean
     * @throws ZipException
     */
    public static boolean isSupportedCharset(String charset) throws ZipException {
        if (StringUtils.isBlank(charset)) {
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

        if (StringUtils.isBlank(currZipFile)) {
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
                    partFile = (zipFileName.contains(".")) ? currZipFile.substring(0, currZipFile.lastIndexOf('.')) : currZipFile;
                    partFile = partFile + fileExt + (i + 1);
                    retList.add(new File(partFile));
                }
            }
        }
        return retList;
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

    public static boolean isDirectory(String fileName) {
        return fileName != null && (fileName.endsWith("/") || fileName.endsWith("\\"));
    }
}
