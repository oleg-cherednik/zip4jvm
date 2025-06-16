package ru.olegcherednik.zip4jvm.model.split;

/**
 * @author Oleg Cherednik
 * @since 14.06.2025
 */
public interface SplitTrigger {

    boolean doSplit(long offs, int diskNo);

}
