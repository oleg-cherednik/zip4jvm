package tangible.c;

import static tangible.c.Sha1.SHA1_NUM_BLOCK_WORDS;
import static tangible.c.Sha1.SHA1_NUM_DIGEST_WORDS;

// Sha1.h:17
public class CSha1 {

    public int[] state = new int[SHA1_NUM_DIGEST_WORDS];
    public long count;
    public int[] buffer = new int[SHA1_NUM_BLOCK_WORDS];
}
