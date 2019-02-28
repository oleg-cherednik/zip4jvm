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

import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

/**
 * Demonstrates extraction of files from a zip file by looping through
 * all the files in the zip file
 * 
 * @author Srikanth Reddy Lingala
 *
 */

public class ExtractByLoopAllFiles {

	public ExtractByLoopAllFiles() {
		
		try {
			// Initiate ZipFile object with the path/name of the zip file.
			ZipFile zipFile = new ZipFile("c:\\ZipTest\\ExtractByLoopAllFiles.zip");
			
			// Check to see if the zip file is password protected 
			if (zipFile.isEncrypted()) {
				// if yes, then set the password for the zip file
				zipFile.setPassword("test123!");
			}
			
			// Get the list of file headers from the zip file
			List fileHeaderList = zipFile.getFileHeaders();
			
			// Loop through the file headers
			for (int i = 0; i < fileHeaderList.size(); i++) {
				FileHeader fileHeader = (FileHeader)fileHeaderList.get(i);
				// Extract the file to the specified destination
				zipFile.extractFile(fileHeader, "c:\\ZipTest\\");
			}
			
		} catch (ZipException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ExtractByLoopAllFiles();

	}

}
