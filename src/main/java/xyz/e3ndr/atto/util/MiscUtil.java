package xyz.e3ndr.atto.util;

import lombok.NonNull;

public class MiscUtil {
    private static char ANSI_START = '\u001b';

    public static int getPaddingToCenter(int length, int width) {
        return (width / 2) - (length / 2);
    }

    public static String explode(int count) {
        char[] result = new char[count];

        for (int i = 0; i != count; i++) {
            result[i] = ' ';
        }

        return new String(result);
    }

    public static String subStringWithColor(@NonNull String str, int start, int length) {
        StringBuilder sb = new StringBuilder();
        char[] arr = str.toCharArray();
        int calculatedIndex = 0;

        length += start;

        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == ANSI_START) {
                // Sequences look like ^[30m (len 5)
                sb.append(arr[i++]);
                sb.append(arr[i++]);
                sb.append(arr[i++]);
                sb.append(arr[i++]);
                sb.append(arr[i]);
            } else {
                calculatedIndex++;

                if (calculatedIndex == length) {
                    break;
                } else if (calculatedIndex > start) {
                    sb.append(arr[i]);
                }
            }
        }

        return sb.toString();
    }

}
