package ru.olegcherednik.zip4jvm.io.in;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.FileNotFoundException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 12.12.2019
 */
@Getter
public abstract class BaseZipModelDataInput extends BaseDataInput implements ZipModelDataInput {

    protected final ZipModel zipModel;

    protected BaseZipModelDataInput(ZipModel zipModel, Path file) throws FileNotFoundException {
        this.zipModel = zipModel;
        delegate = new LittleEndianReadFile(file);
    }
}
