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
