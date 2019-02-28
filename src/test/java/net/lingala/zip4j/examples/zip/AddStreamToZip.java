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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

/**
 * Example demonstrating creation of a zip file with content 
 * from an inputstream. This inputstream may not be a file existing
 * on the disk. Notes:
 * <br>
 * <ul>
 * <li>Standard zip encryption cannot be used with this functionality unless you know 
 * CRC of the stream to be added in advance. If this is the case, then this CRC has to be set 
 * in ZipParameters.setSourceFileCRC(int) before calling ZipFile.addStream() method. There are no 
 * limitations for AES encryption</li>
 * <li>Zip4j wont work in threaded mode with this method. i.e, setting ZipFile.setRunInThread() will 
 * not have any effect and this method will execute in the same thread and not in a different thread</li>
 * <li>InputStream will NOT be closed after content is added to the zip file</li>
 * </ul>
 */
public class AddStreamToZip {
	
	public AddStreamToZip() {
		
		InputStream is = null;
		
		try {
			// Initiate ZipFile object with the path/name of the zip file.
			// Zip file may not necessarily exist. If zip file exists, then 
			// all these files are added to the zip file. If zip file does not
			// exist, then a new zip file is created with the files mentioned
			ZipFile zipFile = new ZipFile("c:\\ZipTest\\AddStreamToZip.zip");
			
			// Initiate Zip Parameters which define various properties such
			// as compression method, etc. More parameters are explained in other
			// examples
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			
			// below two parameters have to be set for adding content to a zip file 
			// directly from a stream
			
			// this would be the name of the file for this entry in the zip file
			parameters.setFileNameInZip("yourfilename.txt");
			
			// we set this flag to true. If this flag is true, Zip4j identifies that
			// the data will not be from a file but directly from a stream
			parameters.setSourceExternalStream(true);
			
			// For this example I use a FileInputStream but in practise this can be 
			// any inputstream
			is = new FileInputStream("filetoadd.txt");
			
			// Creates a new entry in the zip file and adds the content to the zip file
			zipFile.addStream(is, parameters);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		new AddStreamToZip();
	}
	
}
