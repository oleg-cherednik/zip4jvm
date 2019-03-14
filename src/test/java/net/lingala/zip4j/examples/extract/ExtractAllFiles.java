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

package net.lingala.zip4j.examples.extract;

import net.lingala.zip4j.core.ZipFileUnzip;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.UnzipParameters;

import java.nio.file.Paths;

/**
 * Demonstrates extracting all files from a zip file
 *
 * @author Srikanth Reddy Lingala
 */
public class ExtractAllFiles {

    public ExtractAllFiles() throws ZipException {

        // Initiate ZipFile object with the path/name of the zip file.

        // Extracts all files to the path specified
        new ZipFileUnzip(Paths.get("c:\\ZipTest\\ExtractAllFiles.zip")).extract(Paths.get("c:\\ZipTest"), new UnzipParameters());
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws ZipException {
        new ExtractAllFiles();
    }

}
