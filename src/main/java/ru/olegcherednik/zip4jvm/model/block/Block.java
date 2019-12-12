package ru.olegcherednik.zip4jvm.model.block;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.ZipModelDataInput;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.utils.function.LocalSupplier;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 19.10.2019
 */
@NoArgsConstructor
public class Block {

    public static final Block NULL = new Block();

    @Getter
    private long size;
    @Getter
    @Setter
    private long offs;

    @Getter
    @Setter
    private ZipModel zipModel;

    public <T> T calc(DataInput in, LocalSupplier<T> task) throws IOException {
        try {
            zipModel = in instanceof ZipModelDataInput ? ((ZipModelDataInput)in).getZipModel() : null;
            offs = in.getOffs();
            return task.get();
        } finally {
            calc(in.getOffs());
        }
    }

    public void calc(long offs) {
        size = offs - this.offs;
    }

    public byte[] getData() {
        if (zipModel == null || size > Integer.MAX_VALUE)
            return ArrayUtils.EMPTY_BYTE_ARRAY;

        try (DataInput in = zipModel.createDataInput()) {
            in.skip(offs);
            return in.readBytes((int)size);
        } catch(Exception e) {
            e.printStackTrace();
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
    }

}
