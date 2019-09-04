package ru.olegcherednik.zip4jvm.model;

import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.Charset;

/**
 * see 4.3.16
 *
 * @author Oleg Cherednik
 * @since 26.04.2019
 */
@Getter
@Setter
public class EndCentralDirectory {

    public static final int SIGNATURE = 0x06054B50;

    // size (22) with comment length = 0
    public static final int MIN_SIZE = 4 + 2 + 2 + 2 + 2 + 4 + 4 + 2;
    // comment length depend on size of other structures; it is definitely possible to write 33k bytes
    // TODO I have test it with WinRar. But according to specification, 0xFFFF is max size
    public static final int MAX_COMMENT_LENGTH = 33_000;

    // size:4 - signature (0x06054b50)
    // size:2 - number of the disk
    private int totalDisks;
    // size:2 - number of the disk with the start of the central directory
    private int mainDisk;
    // size:2 - total number of entries in the central directory on this disk
    private int diskEntries;
    // size:2 - total number of entries in the central directory
    private int totalEntries;
    // size:4 - CentralDirectory size
    private long centralDirectorySize;
    // size:4 - CentralDirectory offs
    private long centralDirectoryOffs;
    // size:2 - file comment length (n)
    // size:n - file comment
    private String comment;

    public void setComment(String comment) {
        if (StringUtils.length(comment) > MAX_COMMENT_LENGTH)
            throw new Zip4jException("File comment should be " + MAX_COMMENT_LENGTH + " characters maximum");

        this.comment = comment;
    }

    @NonNull
    public byte[] getComment(@NonNull Charset charset) {
        return comment != null ? comment.getBytes(charset) : ArrayUtils.EMPTY_BYTE_ARRAY;
    }

}
