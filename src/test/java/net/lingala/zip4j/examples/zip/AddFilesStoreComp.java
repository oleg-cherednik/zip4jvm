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
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.ZipParameters;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Class demonstrating creation of a zip file by adding files with store
 * compression (No Compression)
 *
 * @author Srikanth Reddy Lingala
 */
public class AddFilesStoreComp {

    public AddFilesStoreComp() throws ZipException, IOException {

        // Initiate ZipFile object with the path/name of the zip file
        // Zip file may not necessarily exist. If zip file exists, then
        // all these files are added to the zip file. If zip file does not
        // exist, then a new zip file is created with the files mentioned
        ZipIt zip = ZipIt.builder().zipFile(Paths.get("c:\\ZipTest\\AddFilesStoreComp.zip")).build();

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
                                                .compressionMethod(CompressionMethod.STORE).build(); // set compression method to store compression
        // Now add files to the zip file
        // Note: To add a single file, the method addFile can be used
        // Note: If the zip file already exists and if this zip file is a split file
        // then this method throws an exception as Zip Format Specification does not
        // allow updating split zip files
        zip.add(filesToAdd, parameters);
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws ZipException, IOException {
        new AddFilesStoreComp();
    }

}
