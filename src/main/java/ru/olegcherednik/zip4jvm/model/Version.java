package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
@Getter
@RequiredArgsConstructor
public final class Version {

    public static final Version NULL = new Version(FileSystem.UNKNOWN, 20);

    private final FileSystem fileSystem;
    private final int zipSpecificationVersion;

    @Override
    public String toString() {
        return fileSystem.title + " / " + zipSpecificationVersion / 10.;
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public enum FileSystem {
        MS_DOS_OS2_NT_FAT(0, "MS-DOS, OS/2, NT FAT"),
        AMIGA(1, "Amiga"),
        OPEN_VMS(2, "VMS"),
        UNIX(3, "Unix"),
        VM_CMS(4, "VM/CMS"),
        ATARI_ST(5, "Atari ST"),
        OS2_NT_HPFS(6, "OS/2, NT HPFS"),
        MACINTOSH_HFS(7, "Macintosh HFS"),
        Z_SYSTEM(8, "Z-System"),
        CP_M(9, "CP/M"),
        TOPS_20(10, "TOPS-20"),
        NTFS(11, "NTFS"),
        SMS_QDOS(12, "SMS/QDOS"),
        ACRON_RISC(13, "Acorn RISC OS"),
        WIN32_VFAT(14, "Win32 VFAT"),
        MVS(15, "MVS"),
        BE_OS(16, "BeOS"),
        TANDEM(17, "Tandem NSK"),
        TANDEM_NSK(18, "Tandem NSK"),
        MAC_OSX(19, "Mac OS X"),
        UNKNOWN(255, "unknown");

        private final int code;
        private final String title;

        public static FileSystem parseCode(int code) {
            for (FileSystem fileSystem : values())
                if (fileSystem.code == code)
                    return fileSystem;
            return UNKNOWN;
        }
    }
}
