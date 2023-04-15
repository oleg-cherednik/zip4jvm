package ru.olegcherednik.zip4jvm.utils.quitely;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.utils.quitely.functions.IntSupplierWithException;
import ru.olegcherednik.zip4jvm.utils.quitely.functions.SupplierWithException;
import ru.olegcherednik.zip4jvm.utils.quitely.functions.TaskWithException;

/**
 * @author Oleg Cherednik
 * @since 15.04.2023
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Quietly {

    public static <T> T doQuietly(SupplierWithException<T> supplier) {
        try {
            return supplier.get();
        } catch (Zip4jvmException e) {
            throw e;
        } catch (Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    public static int doQuietly(IntSupplierWithException supplier) {
        try {
            return supplier.getAsInt();
        } catch (Zip4jvmException e) {
            throw e;
        } catch (Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    public static void doQuietly(TaskWithException task) {
        try {
            task.run();
        } catch (Zip4jvmException e) {
            throw e;
        } catch (Exception e) {
            throw new Zip4jvmException(e);
        }
    }

}
