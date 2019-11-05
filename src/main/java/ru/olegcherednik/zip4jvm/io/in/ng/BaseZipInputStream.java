package ru.olegcherednik.zip4jvm.io.in.ng;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 27.10.2019
 */
public abstract class BaseZipInputStream extends FilterInputStream {

    protected BaseZipInputStream(InputStream in) {
        super(in);
    }

//    @Override
//    public int read() throws IOException {
//        return 0;
//    }

}
