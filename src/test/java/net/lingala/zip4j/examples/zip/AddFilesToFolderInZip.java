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

import net.lingala.zip4j.core.ZipFile;
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
 * Example demonstrates adding files to a folder in a zip file
 *
 * @author Srikanth Reddy Lingala
 */
public class AddFilesToFolderInZip {

    public AddFilesToFolderInZip() throws ZipException, IOException {
        ZipFile zipFile = new ZipFile(Paths.get("c:\\ZipTest\\AddFilesDeflateComp.zip"));

        // Build the list of files to be added in the array list
        List<Path> filesToAdd = Arrays.asList(
                Paths.get("c:/ZipTest/sample.txt"),
                Paths.get("c:/ZipTest/myvideo.avi"),
                Paths.get("c:/ZipTest/mysong.mp3"));

        // Initiate Zip Parameters
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE) // set compression method to deflate compression

                                                // Set the compression level.
                                                .compressionLevel(CompressionLevel.NORMAL)

                                                // Sets the folder in the zip file to which these new files will be added.
                                                // In this example, test2 is the folder to which these files will be added.
                                                // Another example: if files were to be added to a directory test2/test3, then
                                                // below statement should be parameters.setRootFolderInZip("test2/test3/");
                                                .rootFolderInZip("test2/").build();

        // Now add files to the zip file
        zipFile.addFiles(filesToAdd, parameters);
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws ZipException, IOException {
        new AddFilesToFolderInZip();
    }

}
