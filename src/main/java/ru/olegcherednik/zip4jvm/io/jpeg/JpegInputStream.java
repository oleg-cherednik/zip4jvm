package ru.olegcherednik.zip4jvm.io.jpeg;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 04.09.2020
 */
public class JpegInputStream extends InputStream {

    private final DataInput in;
    private final PropertiesHeader propertiesHeader;

    public JpegInputStream(DataInput in) throws IOException {
        this.in = in;
        propertiesHeader = PropertiesHeader.read(in);
    }


    @Override
    public int read() throws IOException {
        Bundle bundle = Bundle.read(in);
        return 0;
    }
}
