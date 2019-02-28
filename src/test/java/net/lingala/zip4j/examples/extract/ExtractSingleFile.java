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

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * Demonstrates extraction of a single file from the zip file
 * 
 * @author Srikanth Reddy Lingala
 */

public class ExtractSingleFile {
	
	public ExtractSingleFile() {
		
		try {
			// Initiate ZipFile object with the path/name of the zip file.
			ZipFile zipFile = new ZipFile("c:\\ZipTest\\ExtractSingleFile.zip");
			
			// Check to see if the zip file is password protected 
			if (zipFile.isEncrypted()) {
				// if yes, then set the password for the zip file
				zipFile.setPassword("test123!");
			}
			
			// Specify the file name which has to be extracted and the path to which
			// this file has to be extracted
			zipFile.extractFile("Ronan_Keating_-_In_This_Life.mp3", "c:\\ZipTest\\");
			
			// Note that the file name is the relative file name in the zip file.
			// For example if the zip file contains a file "mysong.mp3" in a folder 
			// "FolderToAdd", then extraction of this file can be done as below:
			zipFile.extractFile("FolderToAdd\\myvideo.avi", "c:\\ZipTest\\");
			
		} catch (ZipException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ExtractSingleFile();
	}

}
