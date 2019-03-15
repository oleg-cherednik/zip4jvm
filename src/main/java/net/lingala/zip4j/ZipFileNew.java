package net.lingala.zip4j;

import lombok.Builder;
import lombok.NonNull;
import net.lingala.zip4j.core.HeaderWriter;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.NoSplitOutputStream;
import net.lingala.zip4j.io.SplitOutputStream;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;

import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Builder
public final class ZipFileNew {

    @NonNull
    private final Path zipFile;
    @NonNull
    @Builder.Default
    private final Charset charset = Charset.defaultCharset();
    private final char[] password;

    public void setComment(String comment) throws ZipException {
        UnzipIt.checkZipFile(zipFile);

        ZipModel zipModel = ZipFile.createZipModel(zipFile, charset);

        if (comment.length() > InternalZipConstants.MAX_ALLOWED_ZIP_COMMENT_LENGTH)
            throw new ZipException("comment length exceeds maximum length");

        zipModel.getEndCentralDirectory().setComment(comment);

        try (SplitOutputStream out = new NoSplitOutputStream(zipModel.getZipFile())) {
            HeaderWriter headerWriter = new HeaderWriter();

            if (zipModel.isZip64Format())
                out.seek(zipModel.getZip64EndCentralDirectory().getOffsetStartCenDirWRTStartDiskNo());
            else
                out.seek(zipModel.getEndCentralDirectory().getOffOfStartOfCentralDir());

            headerWriter.finalizeZipFileWithoutValidations(zipModel, out);
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    /**
     * Returns the comment set for the Zip path
     *
     * @return String
     * @throws ZipException
     */
    public String getComment() throws ZipException {
        UnzipIt.checkZipFile(zipFile);
        return ZipFile.createZipModel(zipFile, charset).getEndCentralDirectory().getComment();
    }
}
