package xyz.e3ndr.atto.util;

import lombok.SneakyThrows;

public class EnumUtil {

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T extends Enum<?>> T getNext(T e) {
        T[] values = (T[]) e.getClass().getMethod("values").invoke(null);
        int index = e.ordinal() + 1;

        if (index < values.length) {
            return values[index];
        } else {
            return values[0];
        }
    }

    public static String prettify(Enum<?> e) {
        // Replace _'s with spaces, and capitalize the first letter.
        String name = e.toString().replace('_', ' ');

        return name.substring(0, 1) + name.substring(1).toLowerCase();
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T extends Enum<?>> T getPrevious(T e) {
        T[] values = (T[]) e.getClass().getMethod("values").invoke(null);
        int index = e.ordinal() - 1;

        if (index < 0) {
            return values[values.length - 1];
        } else {
            return values[index];
        }
    }

}
