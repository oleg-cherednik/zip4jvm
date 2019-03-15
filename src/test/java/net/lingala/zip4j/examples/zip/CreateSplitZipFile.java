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
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.ZipParameters;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Demonstrated how to create a split zip file
 *
 * @author Srikanth Reddy Lingala
 */
public class CreateSplitZipFile {

    public CreateSplitZipFile() throws ZipException, IOException {
        // Initiate ZipFile object with the path/name of the zip file.
        ZipIt zip = ZipIt.builder().zipFile(Paths.get("c:\\ZipTest\\CreateSplitZipFile.zip")).build();

        // Build the list of files to be added in the array list
        // Objects of type File have to be added to the ArrayList
        List<Path> filesToAdd = Arrays.asList(
                Paths.get("c:\\ZipTest\\sample.txt"),
                Paths.get("c:\\ZipTest\\myvideo.avi"),
                Paths.get("c:\\ZipTest\\mysong.mp3"));

        // Initiate Zip Parameters which define various properties such
        // as compression method, etc.
        ZipParameters parameters = ZipParameters.builder()

                                                // set compression method to store compression
                                                .compressionMethod(CompressionMethod.DEFLATE)

                                                // Set the compression level. This value has to be in between 0 to 9
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .splitLength(10485760).build();

        // Create a split file by setting splitArchive parameter to true
        // and specifying the splitLength. SplitLenth has to be greater than
        // 65536 bytes
        // Please note: If the zip file already exists, then this method throws an
        // exception
        zip.add(filesToAdd, parameters);
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws ZipException, IOException {
        new CreateSplitZipFile();
    }

}
