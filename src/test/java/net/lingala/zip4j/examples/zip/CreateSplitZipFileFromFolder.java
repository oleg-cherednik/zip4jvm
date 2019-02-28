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
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

/**
 * Example showing creation of split zip file and adding a folder to this zip file
 * 
 * @author Srikanth Reddy Lingala
 *
 */
public class CreateSplitZipFileFromFolder {

	public CreateSplitZipFileFromFolder() {
		
		try {
			// Initiate ZipFile object with the path/name of the zip file.
			ZipFile zipFile = new ZipFile("c:\\ZipTest\\CreateSplitZipFileFromFolder.zip");
			
			// Initiate Zip Parameters which define various properties such
			// as compression method, etc.
			ZipParameters parameters = new ZipParameters();
			
			// set compression method to store compression
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			
			// Set the compression level. This value has to be in between 0 to 9
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			
			// Create a split file by setting splitArchive parameter to true
			// and specifying the splitLength. SplitLenth has to be greater than
			// 65536 bytes
			// Please note: If the zip file already exists, then this method throws an 
			// exception
			zipFile.createZipFileFromFolder("C:\\ZipTest", parameters, true, 10485760);
		} catch (ZipException e) {
			e.printStackTrace();
		}
	
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new CreateSplitZipFileFromFolder();
	}

}
