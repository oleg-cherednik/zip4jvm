package ru.olegcherednik.zip4jvm.utils.function;

/**
 * @author Oleg Cherednik
 * @since 26.02.2023
 */
@FunctionalInterface
public interface SupplierWithException<T> {

    T get() throws Exception;

}
