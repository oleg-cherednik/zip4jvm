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

import net.lingala.zip4j.core.ZipFileDir;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.ZipParameters;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Example showing creation of split zip file and adding a folder to this zip file
 *
 * @author Srikanth Reddy Lingala
 */
public class CreateSplitZipFileFromFolder {

    public CreateSplitZipFileFromFolder() throws ZipException, IOException {
        // Initiate ZipFile object with the path/name of the zip file.

        // Initiate Zip Parameters which define various properties such
        // as compression method, etc.
        ZipParameters parameters = ZipParameters.builder()

                                                // set compression method to store compression
                                                .compressionMethod(CompressionMethod.DEFLATE)

                                                // Set the compression level. This value has to be in between 0 to 9
                                                .compressionLevel(CompressionLevel.NORMAL).build();

        // Create a split file by setting splitArchive parameter to true
        // and specifying the splitLength. SplitLenth has to be greater than
        // 65536 bytes
        // Please note: If the zip file already exists, then this method throws an
        // exception
        new ZipFileDir(Paths.get("c:\\ZipTest\\CreateSplitZipFileFromFolder.zip")).addFolder(Paths.get("C:\\ZipTest"), parameters, 10485760);
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws ZipException, IOException {
        new CreateSplitZipFileFromFolder();
    }

}
