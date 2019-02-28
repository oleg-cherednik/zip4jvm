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

import java.io.File;
import java.util.ArrayList;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;

/**
 * This example demonstrates ProgressMonitor usage. Progress Monitor
 * can be used to get the current progress information of the task
 * being performed by Zip4j.<br><br>
 * 
 * ProgressMonitor can be used to get information regarding any kind of task.
 * This example uses the adding files to Zip functionality. Information regarding
 * rest of the tasks is similar to this approach 
 */
public class ProgressInformation {
	
	public ProgressInformation() {
		
		try {
			// Initiate the ZipFile
			ZipFile zipFile = new ZipFile("c:\\ZipTest\\ProgressInformation.zip");
			
			// Set runInThread variable of ZipFile to true.
			// When this variable is set, Zip4j will run any task in a new thread
			// If this variable is not set, Zip4j will run all tasks in the current
			// thread. 
			zipFile.setRunInThread(true);
			
			// Initialize files to add
			ArrayList filesToAdd = new ArrayList();
			filesToAdd.add(new File("c:\\ZipTest\\sample.txt"));
			filesToAdd.add(new File("c:\\ZipTest\\myvideo.avi"));
			filesToAdd.add(new File("c:\\ZipTest\\mysong.mp3"));
			
			// Initialize Zip Parameters
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL); 
			
			// Add files to Zip
			zipFile.addFiles(filesToAdd, parameters);
			
			// Get progress monitor from ZipFile
			ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
			
			// PLEASE NOTE: Below code does a lot of Sysout's.
			
			// ProgressMonitor has two states, READY and BUSY. READY indicates that
			// Zip4j can now accept any new tasks. BUSY indicates that Zip4j is
			// currently performing some task and cannot accept any new task at the moment
			// Any attempt to perform any other task will throw an Exception
			while (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
				// ProgressMonitor has a lot of useful information like, the current
				// operation being performed by Zip4j, current file being processed,
				// percentage done, etc. Once an operation is completed, ProgressMonitor
				// also contains the result of the operation. If any exception is thrown
				// during an operation, this is also stored in this object and can be retrieved
				// as shown below
				
				// To get the percentage done
				System.out.println("Percent Done: " + progressMonitor.getPercentDone());
				
				// To get the current file being processed
				System.out.println("File: " + progressMonitor.getFileName());
				
				// To get current operation
				// Possible values are:
				// ProgressMonitor.OPERATION_NONE - no operation being performed
				// ProgressMonitor.OPERATION_ADD - files are being added to the zip file
				// ProgressMonitor.OPERATION_EXTRACT - files are being extracted from the zip file
				// ProgressMonitor.OPERATION_REMOVE - files are being removed from zip file
				// ProgressMonitor.OPERATION_CALC_CRC - CRC of the file is being calculated
				// ProgressMonitor.OPERATION_MERGE - Split zip files are being merged
				switch (progressMonitor.getCurrentOperation()) {
				case ProgressMonitor.OPERATION_NONE:
					System.out.println("no operation being performed");
					break;
				case ProgressMonitor.OPERATION_ADD:
					System.out.println("Add operation");
					break;
				case ProgressMonitor.OPERATION_EXTRACT:
					System.out.println("Extract operation");
					break;
				case ProgressMonitor.OPERATION_REMOVE:
					System.out.println("Remove operation");
					break;
				case ProgressMonitor.OPERATION_CALC_CRC:
					System.out.println("Calcualting CRC");
					break;
				case ProgressMonitor.OPERATION_MERGE:
					System.out.println("Merge operation");
					break;
				default:
					System.out.println("invalid operation");
					break;
				}
			}
			
			// Once Zip4j is done with its task, it changes the ProgressMonitor
			// state from BUSY to READY, so the above loop breaks.
			// To get the result of the operation:
			// Possible values:
			// ProgressMonitor.RESULT_SUCCESS - Operation was successful
			// ProgressMonitor.RESULT_WORKING - Zip4j is still working and is not 
			//									yet done with the current operation
			// ProgressMonitor.RESULT_ERROR - An error occurred during processing
			System.out.println("Result: " + progressMonitor.getResult());
			
			if (progressMonitor.getResult() == ProgressMonitor.RESULT_ERROR) {
				// Any exception can be retrieved as below:
				if (progressMonitor.getException() != null) {
					progressMonitor.getException().printStackTrace();
				} else {
					System.err.println("An error occurred without any exception");
				}
			}
			
		} catch (ZipException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new ProgressInformation();
	}

}
