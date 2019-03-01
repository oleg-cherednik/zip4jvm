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

package net.lingala.zip4j.core;

import lombok.NonNull;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.SplitOutputStream;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.Zip64EndCentralDirLocator;
import net.lingala.zip4j.model.Zip64EndCentralDirRecord;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.Raw;
import net.lingala.zip4j.util.Zip4jUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class HeaderWriter {

	private final int ZIP64_EXTRA_BUF = 50;

	public int writeLocalFileHeader(ZipModel zipModel, LocalFileHeader localFileHeader,
			OutputStream outputStream) throws ZipException {
		if (localFileHeader == null) {
			throw new ZipException("input parameters are null, cannot write local file header");
		}

		try {
			List<String> bytes = new ArrayList<>();

			byte[] shortByte = new byte[2];
			byte[] intByte = new byte[4];
			byte[] longByte = new byte[8];
			byte[] emptyLongByte = {0,0,0,0,0,0,0,0};

			Raw.writeIntLittleEndian(intByte, 0, localFileHeader.getSignature());
			copyByteArrayToArrayList(intByte, bytes);
			Raw.writeShortLittleEndian(shortByte, 0, (short)localFileHeader.getVersionNeededToExtract());
			copyByteArrayToArrayList(shortByte, bytes);
			//General Purpose bit flags
			copyByteArrayToArrayList(localFileHeader.getGeneralPurposeFlag(), bytes);
			//Compression Method
			Raw.writeShortLittleEndian(shortByte, 0, (short)localFileHeader.getCompressionMethod());
			copyByteArrayToArrayList(shortByte, bytes);
			//File modified time
			int dateTime = localFileHeader.getLastModFileTime();
			Raw.writeIntLittleEndian(intByte, 0, (int)dateTime);
			copyByteArrayToArrayList(intByte, bytes);
			//Skip crc for now - this field will be updated after data is compressed
			Raw.writeIntLittleEndian(intByte, 0, (int)localFileHeader.getCrc32());
			copyByteArrayToArrayList(intByte, bytes);
			boolean writingZip64Rec = false;

			//compressed & uncompressed size
			long uncompressedSize = localFileHeader.getUncompressedSize();
			if (uncompressedSize + ZIP64_EXTRA_BUF >= InternalZipConstants.ZIP_64_LIMIT) {
				Raw.writeLongLittleEndian(longByte, 0, InternalZipConstants.ZIP_64_LIMIT);
				System.arraycopy(longByte, 0, intByte, 0, 4);

				//Set the uncompressed size to ZipConstants.ZIP_64_LIMIT as
				//these values will be stored in Zip64 extra record
				copyByteArrayToArrayList(intByte, bytes);

				copyByteArrayToArrayList(intByte, bytes);
				zipModel.setZip64Format(true);
				writingZip64Rec = true;
				localFileHeader.setWriteComprSizeInZip64ExtraRecord(true);
			} else {
				Raw.writeLongLittleEndian(longByte, 0, localFileHeader.getCompressedSize());
				System.arraycopy(longByte, 0, intByte, 0, 4);
				copyByteArrayToArrayList(intByte, bytes);

				Raw.writeLongLittleEndian(longByte, 0, localFileHeader.getUncompressedSize());
				System.arraycopy(longByte, 0, intByte, 0, 4);
				//Raw.writeIntLittleEndian(intByte, 0, (int)localFileHeader.getUncompressedSize());
				copyByteArrayToArrayList(intByte, bytes);

				localFileHeader.setWriteComprSizeInZip64ExtraRecord(false);
			}
			Raw.writeShortLittleEndian(shortByte, 0, (short)localFileHeader.getFileNameLength());
			copyByteArrayToArrayList(shortByte, bytes);
			// extra field length
			int extraFieldLength = 0;
			if (writingZip64Rec) {
				extraFieldLength += 20;
			}
			if (localFileHeader.getAesExtraDataRecord() != null) {
				extraFieldLength += 11;
			}
			Raw.writeShortLittleEndian(shortByte, 0, (short)(extraFieldLength));
			copyByteArrayToArrayList(shortByte, bytes);
			if (Zip4jUtil.isStringNotNullAndNotEmpty(zipModel.getFileNameCharset())) {
				byte[] fileNameBytes = localFileHeader.getFileName().getBytes(zipModel.getFileNameCharset());
				copyByteArrayToArrayList(fileNameBytes,	bytes);
			} else {
				copyByteArrayToArrayList(Zip4jUtil.convertCharset(localFileHeader.getFileName()), bytes);
			}

			//Zip64 should be the first extra data record that should be written
			//This is NOT according to any specification but if this is changed
			//then take care of updateLocalFileHeader for compressed size
			if (writingZip64Rec) {


				//Zip64 header
				Raw.writeShortLittleEndian(shortByte, 0, (short)InternalZipConstants.EXTRAFIELDZIP64LENGTH);
				copyByteArrayToArrayList(shortByte, bytes);
				//Zip64 extra data record size
				//hardcoded it to 16 for local file header as we will just write
				//compressed and uncompressed file sizes
				Raw.writeShortLittleEndian(shortByte, 0, (short)16);
				copyByteArrayToArrayList(shortByte, bytes);
				//uncompressed size
				Raw.writeLongLittleEndian(longByte, 0, localFileHeader.getUncompressedSize());
				copyByteArrayToArrayList(longByte, bytes);
				//set compressed size to 0 for now
				copyByteArrayToArrayList(emptyLongByte, bytes);
			}

			if (localFileHeader.getAesExtraDataRecord() != null) {
				AESExtraDataRecord aesExtraDataRecord = localFileHeader.getAesExtraDataRecord();

				Raw.writeShortLittleEndian(shortByte, 0, (short)aesExtraDataRecord.getSignature());
				copyByteArrayToArrayList(shortByte, bytes);

				Raw.writeShortLittleEndian(shortByte, 0, (short)aesExtraDataRecord.getDataSize());
				copyByteArrayToArrayList(shortByte, bytes);

				Raw.writeShortLittleEndian(shortByte, 0, (short)aesExtraDataRecord.getVersionNumber());
				copyByteArrayToArrayList(shortByte, bytes);

				copyByteArrayToArrayList(aesExtraDataRecord.getVendorID().getBytes(), bytes);

				byte[] aesStrengthBytes = new byte[1];
				aesStrengthBytes[0] = (byte)aesExtraDataRecord.getAesStrength();
				copyByteArrayToArrayList(aesStrengthBytes, bytes);

				Raw.writeShortLittleEndian(shortByte, 0, (short)aesExtraDataRecord.getCompressionMethod());
				copyByteArrayToArrayList(shortByte, bytes);
			}
			byte[] lhBytes = byteArrayListToByteArray(bytes);
			outputStream.write(lhBytes);
			return lhBytes.length;
		} catch (ZipException e) {
			throw e;
		} catch (Exception e) {
			throw new ZipException(e);
		}
	}

	public int writeExtendedLocalHeader(LocalFileHeader localFileHeader,
			OutputStream outputStream) throws ZipException, IOException {
		if (localFileHeader == null || outputStream == null) {
			throw new ZipException("input parameters is null, cannot write extended local header");
		}

		List<String> bytes = new ArrayList<>();
		byte[] intByte = new byte[4];

		//Extended local file header signature
		Raw.writeIntLittleEndian(intByte, 0, (int)InternalZipConstants.EXTSIG);
		copyByteArrayToArrayList(intByte, bytes);

		//CRC
		Raw.writeIntLittleEndian(intByte, 0, (int)localFileHeader.getCrc32());
		copyByteArrayToArrayList(intByte, bytes);

		//compressed size
		long compressedSize = localFileHeader.getCompressedSize();
		if (compressedSize >= Integer.MAX_VALUE) {
			compressedSize = Integer.MAX_VALUE;
		}
		Raw.writeIntLittleEndian(intByte, 0, (int)compressedSize);
		copyByteArrayToArrayList(intByte, bytes);

		//uncompressed size
		long uncompressedSize = localFileHeader.getUncompressedSize();
		if (uncompressedSize >= Integer.MAX_VALUE) {
			uncompressedSize = Integer.MAX_VALUE;
		}
		Raw.writeIntLittleEndian(intByte, 0, (int)uncompressedSize);
		copyByteArrayToArrayList(intByte, bytes);

		byte[] extLocHdrBytes = byteArrayListToByteArray(bytes);
		outputStream.write(extLocHdrBytes);
		return extLocHdrBytes.length;
	}

	/**
	 * Processes zip header data and writes this data to the zip file
	 * @param zipModel
	 * @param outputStream
	 * @throws ZipException
	 */
	public void finalizeZipFile(ZipModel zipModel,
			OutputStream outputStream) throws ZipException {
		if (zipModel == null || outputStream == null) {
			throw new ZipException("input parameters is null, cannot finalize zip file");
		}

		try {
			processHeaderData(zipModel, outputStream);

			long offsetCentralDir = zipModel.getEndCentralDirRecord().getOffsetOfStartOfCentralDir();

			List<String> bytes = new ArrayList<>();

			int sizeOfCentralDir = writeCentralDirectory(zipModel, outputStream, bytes);

			if (zipModel.isZip64Format()) {
				if (zipModel.getZip64EndCentralDirRecord() == null) {
					zipModel.setZip64EndCentralDirRecord(new Zip64EndCentralDirRecord());
				}
				if (zipModel.getZip64EndCentralDirLocator() == null) {
					zipModel.setZip64EndCentralDirLocator(new Zip64EndCentralDirLocator());
				}

				zipModel.getZip64EndCentralDirLocator().setOffsetZip64EndOfCentralDirRec(offsetCentralDir + sizeOfCentralDir);
				if (outputStream instanceof SplitOutputStream) {
					zipModel.getZip64EndCentralDirLocator().setNoOfDiskStartOfZip64EndOfCentralDirRec(((SplitOutputStream)outputStream).getCurrSplitFileCounter());
					zipModel.getZip64EndCentralDirLocator().setTotNumberOfDiscs(((SplitOutputStream)outputStream).getCurrSplitFileCounter() + 1);
				} else {
					zipModel.getZip64EndCentralDirLocator().setNoOfDiskStartOfZip64EndOfCentralDirRec(0);
					zipModel.getZip64EndCentralDirLocator().setTotNumberOfDiscs(1);
				}

				writeZip64EndOfCentralDirectoryRecord(zipModel, outputStream, sizeOfCentralDir, offsetCentralDir, bytes);

				writeZip64EndOfCentralDirectoryLocator(zipModel, outputStream, bytes);
			}

			writeEndOfCentralDirectoryRecord(zipModel, outputStream, sizeOfCentralDir, offsetCentralDir, bytes);

			writeZipHeaderBytes(zipModel, outputStream, byteArrayListToByteArray(bytes));
		} catch (ZipException e) {
			throw e;
		} catch (Exception e) {
			throw new ZipException(e);
		}
	}

	/**
	 * Processes zip header data and writes this data to the zip file without any validations.
	 * This process is not intended to use for normal operations (adding, deleting, etc) of a zip file.
	 * This method is used when certain validations need to be skipped (ex: Merging split zip files,
	 * adding comment to a zip file, etc)
	 * @param zipModel
	 * @param outputStream
	 * @throws ZipException
	 */
	public void finalizeZipFileWithoutValidations(ZipModel zipModel, OutputStream outputStream) throws ZipException {
		if (zipModel == null || outputStream == null) {
			throw new ZipException("input parameters is null, cannot finalize zip file without validations");
		}

		try {

			List<String> bytes = new ArrayList<>();

			long offsetCentralDir = zipModel.getEndCentralDirRecord().getOffsetOfStartOfCentralDir();

			int sizeOfCentralDir = writeCentralDirectory(zipModel, outputStream, bytes);

			if (zipModel.isZip64Format()) {
				if (zipModel.getZip64EndCentralDirRecord() == null) {
					zipModel.setZip64EndCentralDirRecord(new Zip64EndCentralDirRecord());
				}
				if (zipModel.getZip64EndCentralDirLocator() == null) {
					zipModel.setZip64EndCentralDirLocator(new Zip64EndCentralDirLocator());
				}

				zipModel.getZip64EndCentralDirLocator().setOffsetZip64EndOfCentralDirRec(offsetCentralDir + sizeOfCentralDir);

				writeZip64EndOfCentralDirectoryRecord(zipModel, outputStream, sizeOfCentralDir, offsetCentralDir, bytes);
				writeZip64EndOfCentralDirectoryLocator(zipModel, outputStream, bytes);
			}

			writeEndOfCentralDirectoryRecord(zipModel, outputStream, sizeOfCentralDir, offsetCentralDir, bytes);

			writeZipHeaderBytes(zipModel, outputStream, byteArrayListToByteArray(bytes));
		} catch(ZipException e) {
			throw e;
		} catch (Exception e) {
			throw new ZipException(e);
		}
	}

	/**
	 * Writes the zip header data to the zip file
	 * @param outputStream
	 * @param buff
	 * @throws ZipException
	 */
	private void writeZipHeaderBytes(ZipModel zipModel, OutputStream outputStream, byte[] buff) throws ZipException {
		if (buff == null) {
			throw new ZipException("invalid buff to write as zip headers");
		}

		try {
			if (outputStream instanceof SplitOutputStream) {
				if (((SplitOutputStream)outputStream).checkBuffSizeAndStartNextSplitFile(buff.length)) {
					finalizeZipFile(zipModel, outputStream);
					return;
				}
			}

			outputStream.write(buff);
		} catch (IOException e) {
			throw new ZipException(e);
		}
	}

	/**
	 * Fills the header data in the zip model
	 * @param zipModel
	 * @param outputStream
	 * @throws ZipException
	 */
	private void processHeaderData(ZipModel zipModel, OutputStream outputStream) throws ZipException {
		try {
			int currSplitFileCounter = 0;
			if (outputStream instanceof SplitOutputStream) {
				zipModel.getEndCentralDirRecord().setOffsetOfStartOfCentralDir(
						((SplitOutputStream)outputStream).getFilePointer());
				currSplitFileCounter = ((SplitOutputStream)outputStream).getCurrSplitFileCounter();

			}

			if (zipModel.isZip64Format()) {
				if (zipModel.getZip64EndCentralDirRecord() == null) {
					zipModel.setZip64EndCentralDirRecord(new Zip64EndCentralDirRecord());
				}
				if (zipModel.getZip64EndCentralDirLocator() == null) {
					zipModel.setZip64EndCentralDirLocator(new Zip64EndCentralDirLocator());
				}

				zipModel.getZip64EndCentralDirLocator().setNoOfDiskStartOfZip64EndOfCentralDirRec(currSplitFileCounter);
				zipModel.getZip64EndCentralDirLocator().setTotNumberOfDiscs(currSplitFileCounter + 1);
			}
			zipModel.getEndCentralDirRecord().setNoOfThisDisk(currSplitFileCounter);
			zipModel.getEndCentralDirRecord().setNoOfThisDiskStartOfCentralDir(currSplitFileCounter);
		} catch (IOException e) {
			throw new ZipException(e);
		}
	}

	/**
	 * Writes central directory header data to an array list
	 * @param zipModel
	 * @param outputStream
	 * @param bytes
	 * @return size of central directory
	 * @throws ZipException
	 */
	private int writeCentralDirectory(ZipModel zipModel,
			OutputStream outputStream, List<String> bytes) throws ZipException {
		if (zipModel == null || outputStream == null) {
			throw new ZipException("input parameters is null, cannot write central directory");
		}

		if (zipModel.getCentralDirectory() == null ||
				zipModel.getCentralDirectory().getFileHeaders() == null ||
				zipModel.getCentralDirectory().getFileHeaders().size() <= 0) {
			return 0;
		}

		int sizeOfCentralDir = 0;
		for (int i = 0; i < zipModel.getCentralDirectory().getFileHeaders().size(); i++) {
			FileHeader fileHeader = zipModel.getCentralDirectory().getFileHeaders().get(i);
			int sizeOfFileHeader = writeFileHeader(zipModel, fileHeader, outputStream, bytes);
			sizeOfCentralDir += sizeOfFileHeader;
		}
		return sizeOfCentralDir;
	}

	private int writeFileHeader(ZipModel zipModel, FileHeader fileHeader,
			OutputStream outputStream, List<String> bytes) throws ZipException {

		if (fileHeader == null || outputStream == null) {
			throw new ZipException("input parameters is null, cannot write local file header");
		}

		try {
			int sizeOfFileHeader = 0;

			byte[] shortByte = new byte[2];
			byte[] intByte = new byte[4];
			byte[] longByte = new byte[8];
			final byte[] emptyShortByte = {0,0};
			final byte[] emptyIntByte = {0,0,0,0};

			boolean writeZip64FileSize = false;
			boolean writeZip64OffsetLocalHeader = false;

			Raw.writeIntLittleEndian(intByte, 0, fileHeader.getSignature());
			copyByteArrayToArrayList(intByte, bytes);
			sizeOfFileHeader += 4;

			Raw.writeShortLittleEndian(shortByte, 0, (short)fileHeader.getVersionMadeBy());
			copyByteArrayToArrayList(shortByte, bytes);
			sizeOfFileHeader += 2;

			Raw.writeShortLittleEndian(shortByte, 0, (short)fileHeader.getVersionNeededToExtract());
			copyByteArrayToArrayList(shortByte, bytes);
			sizeOfFileHeader += 2;

			copyByteArrayToArrayList(fileHeader.getGeneralPurposeFlag(), bytes);
			sizeOfFileHeader += 2;

			Raw.writeShortLittleEndian(shortByte, 0, (short)fileHeader.getCompressionMethod());
			copyByteArrayToArrayList(shortByte, bytes);
			sizeOfFileHeader += 2;

			int dateTime = fileHeader.getLastModFileTime();
			Raw.writeIntLittleEndian(intByte, 0, dateTime);
			copyByteArrayToArrayList(intByte, bytes);
			sizeOfFileHeader += 4;

			Raw.writeIntLittleEndian(intByte, 0, (int)(fileHeader.getCrc32()));
			copyByteArrayToArrayList(intByte, bytes);
			sizeOfFileHeader += 4;

			if (fileHeader.getCompressedSize() >= InternalZipConstants.ZIP_64_LIMIT ||
					fileHeader.getUncompressedSize() + ZIP64_EXTRA_BUF >= InternalZipConstants.ZIP_64_LIMIT) {
				Raw.writeLongLittleEndian(longByte, 0, InternalZipConstants.ZIP_64_LIMIT);
				System.arraycopy(longByte, 0, intByte, 0, 4);

				copyByteArrayToArrayList(intByte, bytes);
				sizeOfFileHeader += 4;

				copyByteArrayToArrayList(intByte, bytes);
				sizeOfFileHeader += 4;

				writeZip64FileSize = true;
			} else {
				Raw.writeLongLittleEndian(longByte, 0, fileHeader.getCompressedSize());
				System.arraycopy(longByte, 0, intByte, 0, 4);
//				Raw.writeIntLittleEndian(intByte, 0, (int)fileHeader.getCompressedSize());
				copyByteArrayToArrayList(intByte, bytes);
				sizeOfFileHeader += 4;

				Raw.writeLongLittleEndian(longByte, 0, fileHeader.getUncompressedSize());
				System.arraycopy(longByte, 0, intByte, 0, 4);
//				Raw.writeIntLittleEndian(intByte, 0, (int)fileHeader.getUncompressedSize());
				copyByteArrayToArrayList(intByte, bytes);
				sizeOfFileHeader += 4;
			}

			Raw.writeShortLittleEndian(shortByte, 0, (short)fileHeader.getFileNameLength());
			copyByteArrayToArrayList(shortByte, bytes);
			sizeOfFileHeader += 2;

			//Compute offset bytes before extra field is written for Zip64 compatibility
			//NOTE: this data is not written now, but written at a later point
			byte[] offsetLocalHeaderBytes = new byte[4];
			if (fileHeader.getOffsetLocalHeader() > InternalZipConstants.ZIP_64_LIMIT) {
				Raw.writeLongLittleEndian(longByte, 0, InternalZipConstants.ZIP_64_LIMIT);
				System.arraycopy(longByte, 0, offsetLocalHeaderBytes, 0, 4);
				writeZip64OffsetLocalHeader = true;
			} else {
				Raw.writeLongLittleEndian(longByte, 0, fileHeader.getOffsetLocalHeader());
				System.arraycopy(longByte, 0, offsetLocalHeaderBytes, 0, 4);
			}

			// extra field length
			int extraFieldLength = 0;
			if (writeZip64FileSize || writeZip64OffsetLocalHeader) {
				extraFieldLength += 4;
				if (writeZip64FileSize)
					extraFieldLength += 16;
				if (writeZip64OffsetLocalHeader)
					extraFieldLength += 8;
			}
			if (fileHeader.getAesExtraDataRecord() != null) {
				extraFieldLength += 11;
			}
			Raw.writeShortLittleEndian(shortByte, 0, (short)(extraFieldLength));
			copyByteArrayToArrayList(shortByte, bytes);
			sizeOfFileHeader += 2;

			//Skip file comment length for now
			copyByteArrayToArrayList(emptyShortByte, bytes);
			sizeOfFileHeader += 2;

			//Skip disk number start for now
			Raw.writeShortLittleEndian(shortByte, 0, (short)(fileHeader.getDiskNumberStart()));
			copyByteArrayToArrayList(shortByte, bytes);
			sizeOfFileHeader += 2;

			//Skip internal file attributes for now
			copyByteArrayToArrayList(emptyShortByte, bytes);
			sizeOfFileHeader += 2;

			//External file attributes
			if (fileHeader.getExternalFileAttr() != null) {
				copyByteArrayToArrayList(fileHeader.getExternalFileAttr(), bytes);
			} else {
				copyByteArrayToArrayList(emptyIntByte, bytes);
			}
			sizeOfFileHeader += 4;

			//offset local header
			//this data is computed above
			copyByteArrayToArrayList(offsetLocalHeaderBytes, bytes);
			sizeOfFileHeader += 4;

			if (Zip4jUtil.isStringNotNullAndNotEmpty(zipModel.getFileNameCharset())) {
				byte[] fileNameBytes = fileHeader.getFileName().getBytes(zipModel.getFileNameCharset());
				copyByteArrayToArrayList(fileNameBytes,	bytes);
				sizeOfFileHeader += fileNameBytes.length;
			} else {
				copyByteArrayToArrayList(Zip4jUtil.convertCharset(fileHeader.getFileName()), bytes);
				sizeOfFileHeader += Zip4jUtil.getEncodedStringLength(fileHeader.getFileName());
			}

			if (writeZip64FileSize || writeZip64OffsetLocalHeader) {
				zipModel.setZip64Format(true);

				//Zip64 header
				Raw.writeShortLittleEndian(shortByte, 0, (short)InternalZipConstants.EXTRAFIELDZIP64LENGTH);
				copyByteArrayToArrayList(shortByte, bytes);
				sizeOfFileHeader += 2;

				//Zip64 extra data record size
				int dataSize = 0;

				if (writeZip64FileSize) {
					dataSize += 16;
				}
				if (writeZip64OffsetLocalHeader) {
					dataSize += 8;
				}

				Raw.writeShortLittleEndian(shortByte, 0, (short)dataSize);
				copyByteArrayToArrayList(shortByte, bytes);
				sizeOfFileHeader += 2;

				if (writeZip64FileSize) {
					Raw.writeLongLittleEndian(longByte, 0, fileHeader.getUncompressedSize());
					copyByteArrayToArrayList(longByte, bytes);
					sizeOfFileHeader += 8;

					Raw.writeLongLittleEndian(longByte, 0, fileHeader.getCompressedSize());
					copyByteArrayToArrayList(longByte, bytes);
					sizeOfFileHeader += 8;
				}

				if (writeZip64OffsetLocalHeader) {
					Raw.writeLongLittleEndian(longByte, 0, fileHeader.getOffsetLocalHeader());
					copyByteArrayToArrayList(longByte, bytes);
					sizeOfFileHeader += 8;
				}
			}

			if (fileHeader.getAesExtraDataRecord() != null) {
				AESExtraDataRecord aesExtraDataRecord = fileHeader.getAesExtraDataRecord();

				Raw.writeShortLittleEndian(shortByte, 0, (short)aesExtraDataRecord.getSignature());
				copyByteArrayToArrayList(shortByte, bytes);

				Raw.writeShortLittleEndian(shortByte, 0, (short)aesExtraDataRecord.getDataSize());
				copyByteArrayToArrayList(shortByte, bytes);

				Raw.writeShortLittleEndian(shortByte, 0, (short)aesExtraDataRecord.getVersionNumber());
				copyByteArrayToArrayList(shortByte, bytes);

				copyByteArrayToArrayList(aesExtraDataRecord.getVendorID().getBytes(), bytes);

				byte[] aesStrengthBytes = new byte[1];
				aesStrengthBytes[0] = (byte)aesExtraDataRecord.getAesStrength();
				copyByteArrayToArrayList(aesStrengthBytes, bytes);

				Raw.writeShortLittleEndian(shortByte, 0, (short)aesExtraDataRecord.getCompressionMethod());
				copyByteArrayToArrayList(shortByte, bytes);

				sizeOfFileHeader += 11;
			}

//			outputStream.write(byteArrayListToByteArray(headerBytesList));

			return sizeOfFileHeader;
		} catch (Exception e) {
			throw new ZipException(e);
		}
	}

	private void writeZip64EndOfCentralDirectoryRecord(ZipModel zipModel,
			OutputStream outputStream, int sizeOfCentralDir,
			long offsetCentralDir, List<String> bytes) throws ZipException {
		if (zipModel == null || outputStream == null) {
			throw new ZipException("zip model or output stream is null, cannot write zip64 end of central directory record");
		}

		try {

			byte[] shortByte = new byte[2];
			byte[] emptyShortByte = {0,0};
			byte[] intByte = new byte[4];
			byte[] longByte = new byte[8];

			//zip64 end of central dir signature
			Raw.writeIntLittleEndian(intByte, 0, (int)InternalZipConstants.ZIP64ENDCENDIRREC);
			copyByteArrayToArrayList(intByte, bytes);

			//size of zip64 end of central directory record
			Raw.writeLongLittleEndian(longByte, 0, (long)44);
			copyByteArrayToArrayList(longByte, bytes);

			//version made by
			//version needed to extract
			if (zipModel.getCentralDirectory() != null &&
					zipModel.getCentralDirectory().getFileHeaders() != null &&
					zipModel.getCentralDirectory().getFileHeaders().size() > 0) {
				Raw.writeShortLittleEndian(shortByte, 0,
						(short)((FileHeader)zipModel.getCentralDirectory().getFileHeaders().get(0)).getVersionMadeBy());
				copyByteArrayToArrayList(shortByte, bytes);

				Raw.writeShortLittleEndian(shortByte, 0,
						(short)((FileHeader)zipModel.getCentralDirectory().getFileHeaders().get(0)).getVersionNeededToExtract());
				copyByteArrayToArrayList(shortByte, bytes);
			} else {
				copyByteArrayToArrayList(emptyShortByte, bytes);
				copyByteArrayToArrayList(emptyShortByte, bytes);
			}

			//number of this disk
			Raw.writeIntLittleEndian(intByte, 0, zipModel.getEndCentralDirRecord().getNoOfThisDisk());
			copyByteArrayToArrayList(intByte, bytes);

			//number of the disk with start of central directory
			Raw.writeIntLittleEndian(intByte, 0, zipModel.getEndCentralDirRecord().getNoOfThisDiskStartOfCentralDir());
			copyByteArrayToArrayList(intByte, bytes);

			//total number of entries in the central directory on this disk
			int numEntries = 0;
			int numEntriesOnThisDisk = 0;
			if (zipModel.getCentralDirectory() == null ||
					zipModel.getCentralDirectory().getFileHeaders() == null) {
				throw new ZipException("invalid central directory/file headers, " +
						"cannot write end of central directory record");
			} else {
				numEntries = zipModel.getCentralDirectory().getFileHeaders().size();
				if (zipModel.isSplitArchive()) {
					countNumberOfFileHeaderEntriesOnDisk(zipModel.getCentralDirectory().getFileHeaders(),
							zipModel.getEndCentralDirRecord().getNoOfThisDisk());
				} else {
					numEntriesOnThisDisk = numEntries;
				}
			}
			Raw.writeLongLittleEndian(longByte, 0, numEntriesOnThisDisk);
			copyByteArrayToArrayList(longByte, bytes);

			//Total number of entries in central directory
			Raw.writeLongLittleEndian(longByte, 0, numEntries);
			copyByteArrayToArrayList(longByte, bytes);

			//Size of central directory
			Raw.writeLongLittleEndian(longByte, 0, sizeOfCentralDir);
			copyByteArrayToArrayList(longByte, bytes);

			//offset of start of central directory with respect to the starting disk number
			Raw.writeLongLittleEndian(longByte, 0, offsetCentralDir);
			copyByteArrayToArrayList(longByte, bytes);

		} catch (ZipException zipException) {
			throw zipException;
		} catch (Exception e) {
			throw new ZipException(e);
		}
	}

	private static void writeZip64EndOfCentralDirectoryLocator(ZipModel zipModel,
			OutputStream outputStream, List<String> bytes) throws ZipException {
		if (zipModel == null || outputStream == null) {
			throw new ZipException("zip model or output stream is null, cannot write zip64 end of central directory locator");
		}

		try {

			byte[] intByte = new byte[4];
			byte[] longByte = new byte[8];

			//zip64 end of central dir locator  signature
			Raw.writeIntLittleEndian(intByte, 0, (int)InternalZipConstants.ZIP64ENDCENDIRLOC);
			copyByteArrayToArrayList(intByte, bytes);

			//number of the disk with the start of the zip64 end of central directory
			Raw.writeIntLittleEndian(intByte, 0, zipModel.getZip64EndCentralDirLocator().getNoOfDiskStartOfZip64EndOfCentralDirRec());
			copyByteArrayToArrayList(intByte, bytes);

			//relative offset of the zip64 end of central directory record
			Raw.writeLongLittleEndian(longByte, 0, zipModel.getZip64EndCentralDirLocator().getOffsetZip64EndOfCentralDirRec());
			copyByteArrayToArrayList(longByte, bytes);

			//total number of disks
			Raw.writeIntLittleEndian(intByte, 0, zipModel.getZip64EndCentralDirLocator().getTotNumberOfDiscs());
			copyByteArrayToArrayList(intByte, bytes);
		} catch (Exception e) {
			throw new ZipException(e);
		}
	}

	private void writeEndOfCentralDirectoryRecord(ZipModel zipModel,
			OutputStream outputStream,
			int sizeOfCentralDir,
			long offsetCentralDir,
			List<String> bytes) throws ZipException {
		if (zipModel == null || outputStream == null) {
			throw new ZipException("zip model or output stream is null, cannot write end of central directory record");
		}

		try {

			byte[] shortByte = new byte[2];
			byte[] intByte = new byte[4];
			byte[] longByte = new byte[8];

			//End of central directory signature
			Raw.writeIntLittleEndian(intByte, 0, (int)zipModel.getEndCentralDirRecord().getSignature());
			copyByteArrayToArrayList(intByte, bytes);

			//number of this disk
			Raw.writeShortLittleEndian(shortByte, 0, (short)(zipModel.getEndCentralDirRecord().getNoOfThisDisk()));
			copyByteArrayToArrayList(shortByte, bytes);

			//number of the disk with start of central directory
			Raw.writeShortLittleEndian(shortByte, 0, (short)(zipModel.getEndCentralDirRecord().getNoOfThisDiskStartOfCentralDir()));
			copyByteArrayToArrayList(shortByte, bytes);

			//Total number of entries in central directory on this disk
			int numEntries = 0;
			int numEntriesOnThisDisk = 0;
			if (zipModel.getCentralDirectory() == null ||
					zipModel.getCentralDirectory().getFileHeaders() == null) {
				throw new ZipException("invalid central directory/file headers, " +
						"cannot write end of central directory record");
			} else {
				numEntries = zipModel.getCentralDirectory().getFileHeaders().size();
				if (zipModel.isSplitArchive()) {
					numEntriesOnThisDisk = countNumberOfFileHeaderEntriesOnDisk(zipModel.getCentralDirectory().getFileHeaders(),
							zipModel.getEndCentralDirRecord().getNoOfThisDisk());
				} else {
					numEntriesOnThisDisk = numEntries;
				}
			}
			Raw.writeShortLittleEndian(shortByte, 0, (short)numEntriesOnThisDisk);
			copyByteArrayToArrayList(shortByte, bytes);

			//Total number of entries in central directory
			Raw.writeShortLittleEndian(shortByte, 0, (short)numEntries);
			copyByteArrayToArrayList(shortByte, bytes);

			//Size of central directory
			Raw.writeIntLittleEndian(intByte, 0, sizeOfCentralDir);
			copyByteArrayToArrayList(intByte, bytes);

			//Offset central directory
			if (offsetCentralDir > InternalZipConstants.ZIP_64_LIMIT) {
				Raw.writeLongLittleEndian(longByte, 0, InternalZipConstants.ZIP_64_LIMIT);
				System.arraycopy(longByte, 0, intByte, 0, 4);
				copyByteArrayToArrayList(intByte, bytes);
			} else {
				Raw.writeLongLittleEndian(longByte, 0, offsetCentralDir);
				System.arraycopy(longByte, 0, intByte, 0, 4);
//				Raw.writeIntLittleEndian(intByte, 0, (int)offsetCentralDir);
				copyByteArrayToArrayList(intByte, bytes);
			}

			//Zip File comment length
			int commentLength = 0;
			if (zipModel.getEndCentralDirRecord().getComment() != null) {
				commentLength = zipModel.getEndCentralDirRecord().getCommentLength();
			}
			Raw.writeShortLittleEndian(shortByte, 0, (short)commentLength);
			copyByteArrayToArrayList(shortByte, bytes);

			//Comment
			if (commentLength > 0) {
				copyByteArrayToArrayList(zipModel.getEndCentralDirRecord().getCommentBytes(), bytes);
			}

		} catch (Exception e) {
			throw new ZipException(e);
		}
	}

	public void updateLocalFileHeader(LocalFileHeader localFileHeader, long offset,
			int toUpdate, ZipModel zipModel, byte[] bytesToWrite, int noOfDisk, SplitOutputStream outputStream) throws ZipException {
		if (localFileHeader == null || offset < 0 || zipModel == null) {
			throw new ZipException("invalid input parameters, cannot update local file header");
		}

		try {
			boolean closeFlag = false;
			SplitOutputStream currOutputStream = null;

			if (noOfDisk != outputStream.getCurrSplitFileCounter()) {
				File zipFile = new File(zipModel.getZipFile());
				String parentFile = zipFile.getParent();
				String fileNameWithoutExt = Zip4jUtil.getZipFileNameWithoutExt(zipFile.getName());
				String fileName = parentFile + System.getProperty("file.separator");
				if (noOfDisk < 9) {
					fileName += fileNameWithoutExt + ".z0" + (noOfDisk + 1);
				} else {
					fileName += fileNameWithoutExt + ".z" + (noOfDisk + 1);
				}
				currOutputStream = new SplitOutputStream(new File(fileName));
				closeFlag = true;
			} else {
				currOutputStream = outputStream;
			}

			long currOffset = currOutputStream.getFilePointer();

			switch (toUpdate) {
			case InternalZipConstants.UPDATE_LFH_CRC:
				currOutputStream.seek(offset + toUpdate);
				currOutputStream.write(bytesToWrite);
				break;
			case InternalZipConstants.UPDATE_LFH_COMP_SIZE:
			case InternalZipConstants.UPDATE_LFH_UNCOMP_SIZE:
				updateCompressedSizeInLocalFileHeader(currOutputStream, localFileHeader,
						offset, toUpdate, bytesToWrite, zipModel.isZip64Format());
				break;
			default:
				break;
			}
			if (closeFlag) {
				currOutputStream.close();
			} else {
				outputStream.seek(currOffset);
			}

		} catch (Exception e) {
			throw new ZipException(e);
		}
	}

	private void updateCompressedSizeInLocalFileHeader(SplitOutputStream outputStream, LocalFileHeader localFileHeader,
			long offset, long toUpdate, byte[] bytesToWrite, boolean isZip64Format) throws ZipException {

		if (outputStream == null) {
			throw new ZipException("invalid output stream, cannot update compressed size for local file header");
		}

		try {
			if (localFileHeader.isWriteComprSizeInZip64ExtraRecord()) {
				if (bytesToWrite.length != 8) {
					throw new ZipException("attempting to write a non 8-byte compressed size block for a zip64 file");
				}

				//4 - compressed size
				//4 - uncomprssed size
				//2 - file name length
				//2 - extra field length
				//file name length
				//2 - Zip64 signature
				//2 - size of zip64 data
				//8 - crc
				//8 - compressed size
				//8 - uncompressed size
				long zip64CompressedSizeOffset = offset + toUpdate + 4 + 4 + 2 + 2 + localFileHeader.getFileNameLength() + 2 + 2 + 8;
				if (toUpdate == InternalZipConstants.UPDATE_LFH_UNCOMP_SIZE) {
					zip64CompressedSizeOffset += 8;
				}
				outputStream.seek(zip64CompressedSizeOffset);
				outputStream.write(bytesToWrite);
			} else {
				outputStream.seek(offset + toUpdate);
				outputStream.write(bytesToWrite);
			}
		} catch (IOException e) {
			throw new ZipException(e);
		}

	}

	private static void copyByteArrayToArrayList(@NonNull byte[] buf, @NonNull List<String> bytes) {
		for (int i = 0; i < buf.length; i++)
			bytes.add(Byte.toString(buf[i]));
	}

	private static byte[] byteArrayListToByteArray(@NonNull List<String> bytes) throws ZipException {
		if (bytes.isEmpty())
			return null;

		byte[] retBytes = new byte[bytes.size()];

		for (int i = 0; i < bytes.size(); i++)
			retBytes[i] = Byte.parseByte(bytes.get(i));

		return retBytes;
	}

	private int countNumberOfFileHeaderEntriesOnDisk(List<FileHeader> fileHeaders,
			int numOfDisk) throws ZipException {
		if (fileHeaders == null) {
			throw new ZipException("file headers are null, cannot calculate number of entries on this disk");
		}

		int noEntries = 0;
		for (int i = 0; i < fileHeaders.size(); i++) {
			FileHeader fileHeader = fileHeaders.get(i);
			if (fileHeader.getDiskNumberStart() == numOfDisk) {
				noEntries++;
			}
		}
		return noEntries;
	}

}
