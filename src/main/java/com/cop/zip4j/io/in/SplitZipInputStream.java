package com.cop.zip4j.io.in;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.entry.EntryOutputStream;
import com.cop.zip4j.model.ZipModel;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SplitZipInputStream implements DataInput {

    @NonNull
    private final DataInput delegate;

    @NonNull
    public static SplitZipInputStream create(@NonNull ZipModel zipModel, int diskNumber) throws IOException {
        int counter = diskNumber + 1;

        LittleEndianReadFile in = new LittleEndianReadFile(zipModel.getPartFile(diskNumber));

        if (counter == 1) {
            int signature = in.readDword();

            if (signature != EntryOutputStream.SPLIT_SIGNATURE)
                throw new Zip4jException("Expected first part of split file signature (offs:" + in.getOffs() + ')');
        }

        return new SplitZipInputStream(in);
    }

    // -------------------
    @Override
    public long getOffs() {
        return delegate.getOffs();
    }

    @Override
    public int readWord() throws IOException {
        return delegate.readWord();
    }

    @Override
    public long readDwordLong() throws IOException {
        return delegate.readDwordLong();
    }

    @Override
    public long readQword() throws IOException {
        return delegate.readQword();
    }

    @Override
    public String readString(int length) throws IOException {
        return delegate.readString(length);
    }

    @Override
    public byte readByte() throws IOException {
        return delegate.readByte();
    }

    @Override
    public byte[] readBytes(int total) throws IOException {
        return delegate.readBytes(total);
    }

    @Override
    public void skip(int bytes) throws IOException {
        delegate.skip(bytes);
    }

    @Override
    public long length() throws IOException {
        return delegate.length();
    }

    @Override
    public void seek(long pos) throws IOException {
        delegate.seek(pos);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return delegate.read(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
