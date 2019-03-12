package net.lingala.zip4j.io;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 12.03.2019
 */
@Getter
@Setter
@RequiredArgsConstructor
final class OutputStreamDecorator {
    private final OutputStream delegate;
    private long totalBytesWritten;

    public void addTotalBytesWritten(int delta) {
        totalBytesWritten += delta;
    }

    public int getCurrSplitFileCounter() {
        return delegate instanceof SplitOutputStream ? ((SplitOutputStream)delegate).getCurrSplitFileCounter() : 0;
    }

    public long getOffsLocalHeaderRelative() throws IOException {
        if (totalBytesWritten == 4)
            return 4;
        if (delegate instanceof SplitOutputStream)
            return ((SplitOutputStream)delegate).getFilePointer();
        return totalBytesWritten;
    }

    public long getSplitLength() {
        return delegate instanceof SplitOutputStream ? ((SplitOutputStream)delegate).getSplitLength() : 0;
    }

}
