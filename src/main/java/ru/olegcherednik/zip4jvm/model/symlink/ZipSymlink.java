package ru.olegcherednik.zip4jvm.model.symlink;

/**
 * @author Oleg Cherednik
 * @since 22.01.2023
 */
public enum ZipSymlink {

    /** Ignore symlink. **/
    IGNORE_SYMLINK,
    /** Include only symlink itself. Do not include linked file or directory. **/
    INCLUDE_LOCAL_RESOURCE_SYMLINK,
    /** Include only linked files. Do not include symlink itself. **/
    INCLUDE_LINKED_FILE

}
