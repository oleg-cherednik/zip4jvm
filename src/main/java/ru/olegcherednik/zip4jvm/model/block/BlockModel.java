package ru.olegcherednik.zip4jvm.model.block;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 12.10.2019
 */
@Getter
public final class BlockModel {

    private final ZipModel zipModel;
    private final EndCentralDirectory endCentralDirectory;
    private final Zip64 zip64;
    private final CentralDirectory centralDirectory;

    private final Block endCentralDirectoryBlock;
    private final Zip64Block zip64Block;
    private final CentralDirectoryBlock centralDirectoryBlock;

    private final Map<String, ZipEntryBlock.Data> fileNameZipEntryBlock;

    public static Builder builder() {
        return new Builder();
    }

    private BlockModel(Builder builder) {
        zipModel = builder.zipModel;

        endCentralDirectory = builder.endCentralDirectory;
        zip64 = builder.zip64;
        centralDirectory = builder.centralDirectory;

        endCentralDirectoryBlock = builder.endCentralDirectoryBlock;
        zip64Block = builder.zip64Block;
        centralDirectoryBlock = builder.centralDirectoryBlock;

        fileNameZipEntryBlock = builder.zipEntries;
    }

    public ZipEntryBlock.Data getZipEntryBlock(String fileName) {
        return fileNameZipEntryBlock.get(fileName);
    }

    public boolean isEmpty() {
        return fileNameZipEntryBlock.isEmpty();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private ZipModel zipModel;

        private EndCentralDirectory endCentralDirectory;
        private Zip64 zip64;
        private CentralDirectory centralDirectory;

        private Block endCentralDirectoryBlock;
        private Zip64Block zip64Block;
        private CentralDirectoryBlock centralDirectoryBlock;
        private Map<String, ZipEntryBlock.Data> zipEntries = Collections.emptyMap();

        public BlockModel build() {
            return new BlockModel(this);
        }

        public Builder zipModel(ZipModel zipModel) {
            this.zipModel = zipModel;
            return this;
        }

        public Builder endCentralDirectory(EndCentralDirectory endCentralDirectory, Block block) {
            this.endCentralDirectory = endCentralDirectory;
            endCentralDirectoryBlock = block;
            return this;
        }

        public Builder zip64(Zip64 zip64, Zip64Block block) {
            this.zip64 = Optional.ofNullable(zip64).orElse(Zip64.NULL);
            zip64Block = block;
            return this;
        }

        public Builder centralDirectory(CentralDirectory centralDirectory, CentralDirectoryBlock block) {
            this.centralDirectory = centralDirectory;
            centralDirectoryBlock = block;
            return this;
        }

        public Builder zipEntries(Map<String, ZipEntryBlock.Data> zipEntries) {
            this.zipEntries = MapUtils.isEmpty(zipEntries) ? Collections.emptyMap() : Collections.unmodifiableMap(zipEntries);
            return this;
        }
    }
}


