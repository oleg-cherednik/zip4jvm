package ru.olegcherednik.zip4jvm.view;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.model.Version;

import java.io.PrintStream;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
public final class VersionView extends View {

    private final Version versionMadeBy;
    private final Version versionToExtract;

    public static Builder builder() {
        return new Builder();
    }

    private VersionView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        versionMadeBy = builder.versionMadeBy;
        versionToExtract = builder.versionToExtract;
    }

    @Override
    public void print(PrintStream out) {
        printVersionMadeBy(out);
        printVersionToExtract(out);
    }

    private void printVersionMadeBy(PrintStream out) {
        if (versionMadeBy == Version.NULL)
            return;

        Version.FileSystem fileSystem = versionMadeBy.getFileSystem();
        int zipVersion = versionMadeBy.getZipSpecificationVersion();

        printLine(out, String.format("version made by operating system (%02d):", fileSystem.getCode()), fileSystem.getTitle());
        printLine(out, String.format("version made by zip software (%02d):", zipVersion), String.valueOf(zipVersion / 10.));
    }

    private void printVersionToExtract(PrintStream out) {
        if (versionToExtract == Version.NULL)
            return;

        Version.FileSystem fileSystem = versionToExtract.getFileSystem();
        int zipVersion = versionToExtract.getZipSpecificationVersion();

        printLine(out, String.format("operat. system version needed to extract (%02d):", fileSystem.getCode()), fileSystem.getTitle());
        printLine(out, String.format("unzip software version needed to extract (%02d):", zipVersion), String.valueOf(zipVersion / 10.));
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private Version versionMadeBy = Version.NULL;
        private Version versionToExtract = Version.NULL;
        private int offs;
        private int columnWidth;

        public VersionView build() {
            return new VersionView(this);
        }

        public Builder versionMadeBy(Version versionMadeBy) {
            this.versionMadeBy = Optional.ofNullable(versionMadeBy).orElse(Version.NULL);
            return this;
        }

        public Builder versionToExtract(Version versionToExtract) {
            this.versionToExtract = Optional.ofNullable(versionToExtract).orElse(Version.NULL);
            return this;
        }

        public Builder offs(int offs) {
            this.offs = offs;
            return this;
        }

        public Builder columnWidth(int columnWidth) {
            this.columnWidth = columnWidth;
            return this;
        }

    }
}
