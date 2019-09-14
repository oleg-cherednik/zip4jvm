package ru.olegcherednik.zip4jvm.tasks;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.in.SplitZipInputStream;
import ru.olegcherednik.zip4jvm.io.in.entry.CopyEntryInputStream;
import ru.olegcherednik.zip4jvm.io.in.entry.EntryInputStream;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.entry.EntryOutputStream;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.ZipModelContext;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 12.09.2019
 */
@RequiredArgsConstructor
public class CopyExistedEntryTask implements Task {

    private final String entryName;

    @Override
    public void accept(ZipModelContext context) throws IOException {
        ZipEntry entry = context.getZipModel().getEntryByFileName(entryName);
        DataOutput out = context.getOut();

        ZipModel zipModel = context.getZipModel();

        DataInput di = zipModel.isSplit() ? SplitZipInputStream.create(zipModel, entry.getDisk()) : SingleZipInputStream.create(zipModel);

        try (CopyEntryInputStream in = EntryInputStream.copy(entry, di); OutputStream os = EntryOutputStream.copy(entry, out)) {
            entry.setLocalFileHeaderOffs(out.getOffs());

            in.copyLocalFileHeader(out);
            in.copyEncryptionHeaderAndData(out);
            in.copyDataDescriptor(out);
        }
    }

    @Override
    public String toString() {
        return '~' + entryName;
    }
}
