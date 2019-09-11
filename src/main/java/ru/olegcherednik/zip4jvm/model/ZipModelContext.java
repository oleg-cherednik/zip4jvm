package ru.olegcherednik.zip4jvm.model;

import lombok.Builder;
import lombok.Getter;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;

/**
 * @author Oleg Cherednik
 * @since 10.09.2019
 */
@Getter
@Builder
public class ZipModelContext {

    private final ZipModel zipModel;
    private final DataOutput out;

}
