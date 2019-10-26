package ru.olegcherednik.zip4jvm.model.block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;

import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@Getter
@RequiredArgsConstructor
public class BlockZipEntryModel {

    private final Diagnostic.ZipEntryBlock zipEntryBlock;
    private final Map<String, LocalFileHeader> localFileHeaders;
    private final Map<String, DataDescriptor> dataDescriptors;

}
