package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.Version;

import java.io.PrintStream;
import java.util.Locale;

/**
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
@Builder
public class VersionView {

    @Builder.Default
    private final Version versionMadeBy = Version.NULL;
    @Builder.Default
    private final Version versionToExtract = Version.NULL;
    private final String prefix;

    public void print(PrintStream out) {
        printVersionMadeBy(out);
        printVersionToExtract(out);
    }

    private void printVersionMadeBy(PrintStream out) {
        Version.FileSystem fileSystem = versionMadeBy.getFileSystem();
        int zipVersion = versionMadeBy.getZipSpecificationVersion();

        if (versionMadeBy != Version.NULL)
            out.format("%sversion made by operating system (%02d):          %s\n", prefix, fileSystem.getCode(), fileSystem.getTitle());
        if (versionToExtract != Version.NULL)
            out.format(Locale.US, "%sversion made by zip software (%02d):              %s\n", prefix, zipVersion, zipVersion / 10.);
    }

    private void printVersionToExtract(PrintStream out) {
        Version.FileSystem fileSystem = versionToExtract.getFileSystem();
        int zipVersion = versionToExtract.getZipSpecificationVersion();

        out.format("%soperat. system version needed to extract (%02d):  %s\n", prefix, fileSystem.getCode(), fileSystem.getTitle());
        out.format(Locale.US, "%sunzip software version needed to extract (%02d):  %s\n", prefix, zipVersion, zipVersion / 10.);
    }

}
