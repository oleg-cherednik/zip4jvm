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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import net.lingala.zip4j.io.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class CreateZipWithOutputStreamsStandardEnc {
	
	public CreateZipWithOutputStreamsStandardEnc() {
		
		// Input and OutputStreams are defined outside of the try/catch block
		// to use them in the finally block
		ZipOutputStream outputStream = null;
		InputStream inputStream = null;
		
		try {
			// Prepare the files to be added
			ArrayList filesToAdd = new ArrayList();
			filesToAdd.add(new File("c:\\ZipTest\\sample.txt"));
			filesToAdd.add(new File("c:\\ZipTest\\myvideo.avi"));
			filesToAdd.add(new File("c:\\ZipTest\\mysong.mp3"));
			
			//Initiate output stream with the path/file of the zip file
			//Please note that ZipOutputStream will overwrite zip file if it already exists 
			outputStream = new ZipOutputStream(new FileOutputStream(new File("c:\\ZipTest\\CreateZipWithOutputStreamsStandardEnc.zip")));
			
			// Initiate Zip Parameters which define various properties such
			// as compression method, etc. More parameters are explained in other
			// examples
			ZipParameters parameters = new ZipParameters();
			
			//Deflate compression or store(no compression) can be set below
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			
			// Set the compression level. This value has to be in between 0 to 9
			// Several predefined compression levels are available
			// DEFLATE_LEVEL_FASTEST - Lowest compression level but higher speed of compression
			// DEFLATE_LEVEL_FAST - Low compression level but higher speed of compression
			// DEFLATE_LEVEL_NORMAL - Optimal balance between compression level/speed
			// DEFLATE_LEVEL_MAXIMUM - High compression level with a compromise of speed
			// DEFLATE_LEVEL_ULTRA - Highest compression level but low speed
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			
			//This flag defines if the files have to be encrypted.
			//If this flag is set to false, setEncryptionMethod, as described below,
			//will be ignored and the files won't be encrypted
			parameters.setEncryptFiles(true);
			
			//Set encryption method to Standard Encryption
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
			
			//self descriptive
			parameters.setPassword("YourPassword");
			
			//Now we loop through each file, determine the file CRC and set it
			//in the zip parameters and then we read the input stream and write it
			//to the outputstream
			for (int i = 0; i < filesToAdd.size(); i++) {
				File file = (File)filesToAdd.get(i);
				
				//This will initiate ZipOutputStream to include the file
				//with the input parameters
				outputStream.putNextEntry(file,parameters);
				
				//If this file is a directory, then no further processing is required
				//and we close the entry (Please note that we do not close the outputstream yet)
				if (file.isDirectory()) {
					outputStream.closeEntry();
					continue;
				}
				
				//Initialize inputstream
				inputStream = new FileInputStream(file);
				byte[] readBuff = new byte[4096];
				int readLen = -1;
				
				//Read the file content and write it to the OutputStream
				while ((readLen = inputStream.read(readBuff)) != -1) {
					outputStream.write(readBuff, 0, readLen);
				}
				
				//Once the content of the file is copied, this entry to the zip file
				//needs to be closed. ZipOutputStream updates necessary header information
				//for this file in this step
				outputStream.closeEntry();
				
				inputStream.close();
			}
			
			//ZipOutputStream now writes zip header information to the zip file
			outputStream.finish();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new CreateZipWithOutputStreamsStandardEnc();
	}

}
