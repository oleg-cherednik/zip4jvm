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

package net.lingala.zip4j.examples.misc;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

/**
 * Demonstrates how to remove a file from a zip file
 * 
 * @author Srikanth Reddy Lingala
 *
 */
public class RemoveFileFromZipFile {
	
	public RemoveFileFromZipFile() {
		
		try {
			// Initiate ZipFile object with the path/name of the zip file.
			ZipFile zipFile = new ZipFile("c:\\ZipTest\\AddFilesWithAESZipEncryption.zip");
			
			// Note: If this zip file is a split file then this method throws an exception as
			// Zip Format Specification does not allow updating split zip files
			
			// Please make sure that this zip file has more than one file to completely test
			// this example
			
			// Removal of a file from a zip file can be done in two ways:
			// 1. Specify the name of the relative file to the removed
			zipFile.removeFile("myvideo.avi");
			
			// 2. With the FileHeader
			if (zipFile.getFileHeaders() != null && zipFile.getFileHeaders().size() > 0) {
				zipFile.removeFile((FileHeader)zipFile.getFileHeaders().get(0));
			} else {
				System.out.println("This cannot be demonstrated as zip file does not have any files left");
			}
			
		} catch (ZipException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new RemoveFileFromZipFile();
	}

}
