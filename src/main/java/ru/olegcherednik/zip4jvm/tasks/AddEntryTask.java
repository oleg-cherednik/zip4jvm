package ru.olegcherednik.zip4jvm.tasks;

import ru.olegcherednik.zip4jvm.model.ZipModelContext;

/**
 * @author Oleg Cherednik
 * @since 10.09.2019
 */
public final class AddEntryTask implements Task {

    public static void main(String... args) {
        int[] arr = { 0, 1, 2, 3, 4, 5 };
        int[] res = removeItem(arr, 2);
        int a = 0;
        a++;
    }

    public static int[] removeItem(int[] dices, int i) {
        if (dices == null || dices.length == 0)
            return dices;
        if (i < 0 || i >= dices.length)
            throw new ArrayIndexOutOfBoundsException();

        // 0 1 2 3 4 5 -> 2 -> 0 1 - 3 4 5

        int[] res = new int[dices.length - 1];
        System.arraycopy(dices, 0, res, 0, i + 1);
        System.arraycopy(dices, i, res, i, i + 1);
        return res;
    }

    @Override
    public void accept(ZipModelContext context) {

    }
}

class Dice {

}

