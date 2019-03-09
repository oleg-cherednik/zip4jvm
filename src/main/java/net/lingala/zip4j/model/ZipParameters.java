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

package net.lingala.zip4j.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.Zip4jUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TimeZone;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class ZipParameters {

    @NonNull
    private CompressionMethod compressionMethod = CompressionMethod.DEFLATE;
    @NonNull
    private CompressionLevel compressionLevel = CompressionLevel.DEFAULT;
    private boolean encryptFiles;
    @NonNull
    private Encryption encryption = Encryption.OFF;
    private boolean readHiddenFiles;
    private char[] password;
    private int aesKeyStrength;
    private boolean includeRootFolder;
    private String rootFolderInZip;
    private TimeZone timeZone;
    private long sourceFileCRC;
    private String defaultFolderPath;
    private String fileNameInZip;
    private boolean isSourceExternalStream;

    public ZipParameters() {
        encryptFiles = false;
        readHiddenFiles = true;
        aesKeyStrength = -1;
        includeRootFolder = true;
        timeZone = TimeZone.getDefault();
    }

    public char[] getPassword() {
        return password;
    }

    /**
     * Sets the password for the zip file or the file being added<br>
     * <b>Note</b>: For security reasons, usage of this method is discouraged. Use
     * setPassword(char[]) instead. As strings are immutable, they cannot be wiped
     * out from memory explicitly after usage. Therefore, usage of Strings to store
     * passwords is discouraged. More info here:
     * http://docs.oracle.com/javase/1.5.0/docs/guide/security/jce/JCERefGuide.html#PBEEx
     *
     * @param password
     */
    public void setPassword(String password) {
        if (password == null) return;
        setPassword(password.toCharArray());
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public int getAesKeyStrength() {
        return aesKeyStrength;
    }

    public void setAesKeyStrength(int aesKeyStrength) {
        this.aesKeyStrength = aesKeyStrength;
    }

    public boolean isIncludeRootFolder() {
        return includeRootFolder;
    }

    public void setIncludeRootFolder(boolean includeRootFolder) {
        this.includeRootFolder = includeRootFolder;
    }

    public String getRootFolderInZip() {
        return rootFolderInZip;
    }

    public void setRootFolderInZip(String rootFolderInZip) {
        if (StringUtils.isNotBlank(rootFolderInZip)) {

            if (!Zip4jUtil.isDirectory(rootFolderInZip))
                rootFolderInZip += InternalZipConstants.FILE_SEPARATOR;

            rootFolderInZip = rootFolderInZip.replaceAll("\\\\", "/");

//			if (rootFolderInZip.endsWith("/")) {
//				rootFolderInZip = rootFolderInZip.substring(0, rootFolderInZip.length() - 1);
//				rootFolderInZip = rootFolderInZip + "\\";
//			}
        }
        this.rootFolderInZip = rootFolderInZip;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }


    public String getDefaultFolderPath() {
        return defaultFolderPath;
    }

    public void setDefaultFolderPath(String defaultFolderPath) {
        this.defaultFolderPath = defaultFolderPath;
    }

    public String getFileNameInZip() {
        return fileNameInZip;
    }

    public void setFileNameInZip(String fileNameInZip) {
        this.fileNameInZip = fileNameInZip;
    }

    public boolean isSourceExternalStream() {
        return isSourceExternalStream;
    }

    public void setSourceExternalStream(boolean isSourceExternalStream) {
        this.isSourceExternalStream = isSourceExternalStream;
    }

    public String getRelativeFileName(Path file) throws ZipException {
        Path entryPath = file.toAbsolutePath();
        Path rootPath = defaultFolderPath != null ? Paths.get(defaultFolderPath).toAbsolutePath() : entryPath.getParent();

        String path = rootPath.relativize(entryPath).toString();

        if (Files.isDirectory(entryPath))
            path += File.separator;

        if (rootFolderInZip != null)
            path = FilenameUtils.concat(path, rootFolderInZip);

        return path;
    }

}
