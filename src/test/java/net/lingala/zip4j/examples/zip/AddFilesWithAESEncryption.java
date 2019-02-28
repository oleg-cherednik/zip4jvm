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
 * Demonstrates adding files to zip file with AES Encryption
 *
 * @author Srikanth Reddy Lingala
 */
public class AddFilesWithAESEncryption {
	
	public AddFilesWithAESEncryption() {
		
		try {
			// Initiate ZipFile object with the path/name of the zip file.
			ZipFile zipFile = new ZipFile("c:\\ZipTest\\AddFilesWithAESZipEncryption.zip");
			
			// Build the list of files to be added in the array list
			// Objects of type File have to be added to the ArrayList
			ArrayList filesToAdd = new ArrayList();
			filesToAdd.add(new File("c:\\ZipTest\\sample.txt"));
			filesToAdd.add(new File("c:\\ZipTest\\myvideo.avi"));
			filesToAdd.add(new File("c:\\ZipTest\\mysong.mp3"));
			
			// Initiate Zip Parameters which define various properties such
			// as compression method, etc. More parameters are explained in other
			// examples
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression
			
			// Set the compression level. This value has to be in between 0 to 9
			// Several predefined compression levels are available
			// DEFLATE_LEVEL_FASTEST - Lowest compression level but higher speed of compression
			// DEFLATE_LEVEL_FAST - Low compression level but higher speed of compression
			// DEFLATE_LEVEL_NORMAL - Optimal balance between compression level/speed
			// DEFLATE_LEVEL_MAXIMUM - High compression level with a compromise of speed
			// DEFLATE_LEVEL_ULTRA - Highest compression level but low speed
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL); 
			
			// Set the encryption flag to true
			// If this is set to false, then the rest of encryption properties are ignored
			parameters.setEncryptFiles(true);
			
			// Set the encryption method to AES Zip Encryption
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
			
			// Set AES Key strength. Key strengths available for AES encryption are:
			// AES_STRENGTH_128 - For both encryption and decryption
			// AES_STRENGTH_192 - For decryption only
			// AES_STRENGTH_256 - For both encryption and decryption
			// Key strength 192 cannot be used for encryption. But if a zip file already has a
			// file encrypted with key strength of 192, then Zip4j can decrypt this file
			parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
			
			// Set password
			parameters.setPassword("test123!");
			
			// Now add files to the zip file
			// Note: To add a single file, the method addFile can be used
			// Note: If the zip file already exists and if this zip file is a split file
			// then this method throws an exception as Zip Format Specification does not 
			// allow updating split zip files
			zipFile.addFiles(filesToAdd, parameters);
		} catch (ZipException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new AddFilesWithAESEncryption();
	}
	
}
