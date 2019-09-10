package ru.olegcherednik.zip4jvm.tasks;

import ru.olegcherednik.zip4jvm.model.ZipModelContext;

import java.util.function.Consumer;

/**
 * @author Oleg Cherednik
 * @since 10.09.2019
 */
public interface Task extends Consumer<ZipModelContext> {

}
