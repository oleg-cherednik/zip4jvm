package ru.olegcherednik.zip4jvm.model.diagnostic;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Oleg Cherednik
 * @since 19.10.2019
 */
public class Block {

    public static final Block NULL = new Block();

    @Getter
    @Setter
    private long offs;
    @Getter
    private long size;

    public void setEndOffs(long offs) {
        size = offs - this.offs;
    }

}
