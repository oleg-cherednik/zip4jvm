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
 * Demonstrated adding a folder to zip file
 *
 * @author Srikanth Reddy Lingala
 */
public class AddFolder {

    public AddFolder() throws ZipException, IOException {
        // Initiate ZipFile object with the path/name of the zip file.

        // Folder to add
        String folderToAdd = "c:\\FolderToAdd";

        // Initiate Zip Parameters which define various properties such
        // as compression method, etc.
        ZipParameters parameters = ZipParameters.builder()

        // set compression method to store compression
        .compressionMethod(CompressionMethod.DEFLATE)

        // Set the compression level
        .compressionLevel(CompressionLevel.NORMAL).build();

        // Add folder to the zip file
        new ZipFileDir(Paths.get("c:\\ZipTest\\AddFolder.zip")).addFolder(folderToAdd, parameters);
    }

    public static void main(String[] args) throws ZipException, IOException {
        new AddFolder();
    }

}
