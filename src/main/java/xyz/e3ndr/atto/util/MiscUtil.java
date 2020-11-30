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

    public static String subStringWithColor(@NonNull String contents, int x, int y, int width, int height) {
        StringBuilder sb = new StringBuilder();
        char[] arr = contents.toCharArray();
        int currentLine = 0;
        int lineIndex = 0;

        width += x;
        height += y;

        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == ANSI_START) {
                // Sequences look like ^[30m (len 5)
                sb.append(arr[i++]);
                sb.append(arr[i++]);
                sb.append(arr[i++]);
                sb.append(arr[i++]);
                sb.append(arr[i]);
            } else {
                if (arr[i] == '\n') {
                    lineIndex = 0;
                    currentLine++;
                }

                if (currentLine == height - 2) {
                    break;
                } else if (currentLine >= y) {
                    if ((lineIndex < width) && (lineIndex >= x)) {
                        sb.append(arr[i]);
                    }

                    lineIndex++;
                }
            }
        }

        if ((y == 0) || (sb.length() == 0)) {
            return sb.toString();
        } else {
            return sb.toString().replaceFirst("\n", "");
        }
    }

}
