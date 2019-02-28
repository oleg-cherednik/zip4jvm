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

import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

/**
 * Lists all the files in a zip file including the properties of the file
 * @author Srikanth Reddy Lingala
 *
 */
public class ListAllFilesInZipFile {

	public ListAllFilesInZipFile() {
		
		try {
			// Initiate ZipFile object with the path/name of the zip file.
			ZipFile zipFile = new ZipFile("c:\\ZipTest\\ListAllFilesInZipFile.zip");
			
			// Get the list of file headers from the zip file
			List fileHeaderList = zipFile.getFileHeaders();
			
			// Loop through the file headers
			for (int i = 0; i < fileHeaderList.size(); i++) {
				FileHeader fileHeader = (FileHeader)fileHeaderList.get(i);
				// FileHeader contains all the properties of the file
				System.out.println("****File Details for: " + fileHeader.getFileName() + "*****");
				System.out.println("Name: " + fileHeader.getFileName());
				System.out.println("Compressed Size: " + fileHeader.getCompressedSize());
				System.out.println("Uncompressed Size: " + fileHeader.getUncompressedSize());
				System.out.println("CRC: " + fileHeader.getCrc32());
				System.out.println("************************************************************");
				
				// Various other properties are available in FileHeader. Please have a look at FileHeader
				// class to see all the properties
			}
			
		} catch (ZipException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ListAllFilesInZipFile();
	}

}
