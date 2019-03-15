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

package net.lingala.zip4j.examples.zip;

import net.lingala.zip4j.ZipIt;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.AESStrength;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.Encryption;
import net.lingala.zip4j.model.ZipParameters;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Demonstrates adding files to zip file with AES Encryption
 *
 * @author Srikanth Reddy Lingala
 */
public class AddFilesWithAESEncryption {

    public AddFilesWithAESEncryption() throws ZipException, IOException {

        // Initiate ZipFile object with the path/name of the zip file.
        ZipIt zip = ZipIt.builder().zipFile(Paths.get("c:\\ZipTest\\AddFilesWithAESZipEncryption.zip")).build();

        // Build the list of files to be added in the array list
        // Objects of type File have to be added to the ArrayList
        List<Path> filesToAdd = Arrays.asList(
                Paths.get("c:/ZipTest/sample.txt"),
                Paths.get("c:/ZipTest/myvideo.avi"),
                Paths.get("c:/ZipTest/mysong.mp3"));

        // Initiate Zip Parameters which define various properties such
        // as compression method, etc. More parameters are explained in other
        // examples
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE) // set compression method to deflate compression

                                                // Set the compression level. This value has to be in between 0 to 9
                                                // Several predefined compression levels are available
                                                // DEFLATE_LEVEL_FASTEST - Lowest compression level but higher speed of compression
                                                // DEFLATE_LEVEL_FAST - Low compression level but higher speed of compression
                                                // DEFLATE_LEVEL_NORMAL - Optimal balance between compression level/speed
                                                // DEFLATE_LEVEL_MAXIMUM - High compression level with a compromise of speed
                                                // DEFLATE_LEVEL_ULTRA - Highest compression level but low speed
                                                .compressionLevel(CompressionLevel.NORMAL)

                                                // Set the encryption flag to true
                                                // If this is set to false, then the rest of encryption properties are ignored
                                                // Set the encryption method to AES Zip Encryption
                                                .encryption(Encryption.AES)

                                                // Set AES Key strength. Key strengths available for AES encryption are:
                                                // STRENGTH_128 - For both encryption and decryption
                                                // STRENGTH_192 - For decryption only
                                                // STRENGTH_256 - For both encryption and decryption
                                                // Key strength 192 cannot be used for encryption. But if a zip file already has a
                                                // file encrypted with key strength of 192, then Zip4j can decrypt this file
                                                .aesKeyStrength(AESStrength.STRENGTH_256)

                                                // Set password
                                                .password("test123!".toCharArray()).build();

        // Now add files to the zip file
        // Note: To add a single file, the method addFile can be used
        // Note: If the zip file already exists and if this zip file is a split file
        // then this method throws an exception as Zip Format Specification does not
        // allow updating split zip files
        zip.add(filesToAdd, parameters);
    }

    public static void main(String[] args) throws ZipException, IOException {
        new AddFilesWithAESEncryption();
    }

}
