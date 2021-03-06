package ru.olegcherednik.zip4jvm.view.extrafield;

import lombok.Getter;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 03.12.2019
 */
@Getter
final class Builder<R extends ExtraField.Record, V extends ExtraFieldRecordView<R>> {

    private final Function<Builder<R, V>, V> sup;
    private R record;
    private GeneralPurposeFlag generalPurposeFlag;
    private byte[] data = ArrayUtils.EMPTY_BYTE_ARRAY;
    private Block block;
    private int offs;
    private int columnWidth;
    private long totalDisks;

    Builder(Function<Builder<R, V>, V> sup) {
        this.sup = sup;
    }

    public V build() {
        check();
        return sup.apply(this);
    }

    public void check() {
        Objects.requireNonNull(data, "'data' must not be null");
        Objects.requireNonNull(block, "'block' must not be null");
    }

    public Builder<R, V> record(R record) {
        this.record = record == null || record.isNull() ? null : record;
        return this;
    }

    public Builder<R, V> generalPurposeFlag(GeneralPurposeFlag generalPurposeFlag) {
        this.generalPurposeFlag = generalPurposeFlag;
        return this;
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public Builder<R, V> data(byte[] data) {
        this.data = ArrayUtils.isEmpty(data) ? ArrayUtils.EMPTY_BYTE_ARRAY : ArrayUtils.clone(data);
        return this;
    }

    public Builder<R, V> block(Block block) {
        this.block = block == Block.NULL ? null : block;
        return this;
    }

    public Builder<R, V> position(int offs, int columnWidth, long totalDisks) {
        this.offs = offs;
        this.columnWidth = columnWidth;
        this.totalDisks = totalDisks;
        return this;
    }

}
