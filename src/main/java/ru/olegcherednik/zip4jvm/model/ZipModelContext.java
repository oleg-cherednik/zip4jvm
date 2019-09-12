package ru.olegcherednik.zip4jvm.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 10.09.2019
 */
@Getter
@Setter
@Builder
public class ZipModelContext {

    private final ZipModel zipModel;
    private final Map<String, ZipEntry> fileNameEntry = new LinkedHashMap<>();

    private Path tmpFile;
    private DataOutput out;

}
