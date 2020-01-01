package ru.olegcherednik.zip4jvm.io.in;

import lombok.Getter;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.FileNotFoundException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 12.12.2019
 */
@Getter
public abstract class BaseZipDataInput extends BaseDataInput implements ZipDataInput {

    protected final ZipModel zipModel;
    @Setter
    protected String fileName;

    protected BaseZipDataInput(ZipModel zipModel, Path file) throws FileNotFoundException {
        this.zipModel = zipModel;
        delegate = new LittleEndianReadFile(file);
        fileName = file.getFileName().toString();
    }

    @Override
    public long getTotalDisks() {
        return zipModel.getTotalDisks();
    }

}
