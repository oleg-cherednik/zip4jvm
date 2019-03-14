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

package net.lingala.zip4j.unzip;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipModel;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@RequiredArgsConstructor
public final class Unzip {

    @NonNull
    private final ZipModel zipModel;

    public void extract(@NonNull Path destDir, @NonNull UnzipParameters parameters) throws ZipException {
        for (String entryName : zipModel.getEntryNames())
            extractFile(entryName, destDir, parameters);
    }

    public void extractFile(@NonNull String entryName, @NonNull Path destDir, UnzipParameters parameters) throws ZipException {
        CentralDirectory.FileHeader fileHeader = zipModel.getCentralDirectory().getFileHeaderByName(entryName);

        if (fileHeader == null)
            throw new ZipException("Entry '" + entryName + "' was not found");

//        if (fileHeader.isDirectory()) {
//            try {
//                Path completePath = destDir.resolve(fileHeader.getFileName());
//
//                if (!Files.exists(completePath))
//                    Files.createDirectories(completePath);
//            } catch(Exception e) {
//                throw new ZipException(e);
//            }
//        } else {
        new UnzipEngine(zipModel, fileHeader).unzipFile(destDir, parameters);
//        }
    }

    public ZipInputStream getInputStream(CentralDirectory.FileHeader fileHeader) throws ZipException {
        UnzipEngine unzipEngine = new UnzipEngine(zipModel, fileHeader);
        try {
            return unzipEngine.getInputStream();
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
