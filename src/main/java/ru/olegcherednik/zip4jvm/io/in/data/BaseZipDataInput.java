package ru.olegcherednik.zip4jvm.io.in.data;

import lombok.Getter;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.io.in.file.SrcFile;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 12.12.2019
 */
@Getter
public abstract class BaseZipDataInput extends BaseDataInput implements ZipDataInput {

    protected final ZipModel zipModel;
    @Setter
    protected String fileName;

    protected BaseZipDataInput(ZipModel zipModel, SrcFile srcFile) throws IOException {
        this.zipModel = zipModel;
        delegate = srcFile.dataInputFile();
        fileName = srcFile.getPath().getFileName().toString();
    }

    @Override
    public long getTotalDisks() {
        return zipModel.getTotalDisks();
    }

}
