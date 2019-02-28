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

import java.io.File;
import java.util.ArrayList;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

/**
 * Example demonstrates adding files to a folder in a zip file
 * @author Srikanth Reddy Lingala
 *
 */
public class AddFilesToFolderInZip {

	public AddFilesToFolderInZip() {
		try {
			ZipFile zipFile = new ZipFile("c:\\ZipTest\\AddFilesDeflateComp.zip");
			
			// Build the list of files to be added in the array list
			ArrayList filesToAdd = new ArrayList();
			filesToAdd.add(new File("c:\\ZipTest\\sample.txt"));
			filesToAdd.add(new File("c:\\ZipTest\\myvideo.avi"));
			filesToAdd.add(new File("c:\\ZipTest\\mysong.mp3"));
			
			// Initiate Zip Parameters 
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression
			
			// Set the compression level.
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			
			// Sets the folder in the zip file to which these new files will be added.
			// In this example, test2 is the folder to which these files will be added.
			// Another example: if files were to be added to a directory test2/test3, then
			// below statement should be parameters.setRootFolderInZip("test2/test3/");
			parameters.setRootFolderInZip("test2/");
			
			// Now add files to the zip file
			zipFile.addFiles(filesToAdd, parameters);
		} catch (ZipException e) {
			e.printStackTrace();
		} 
		
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new AddFilesToFolderInZip();
	}

}
