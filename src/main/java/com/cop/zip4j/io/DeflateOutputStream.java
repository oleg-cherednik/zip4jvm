package com.cop.zip4j.io;

import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.utils.InternalZipConstants;
import lombok.NonNull;

import java.util.zip.Deflater;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public class DeflateOutputStream extends CipherOutputStream {

    public final byte[] buf = new byte[InternalZipConstants.BUFF_SIZE];
    public final Deflater deflater = new Deflater();

    public boolean firstBytesRead;

    public DeflateOutputStream(@NonNull SplitOutputStream out, @NonNull ZipModel zipModel) {
        super(out, zipModel);
    }


}
