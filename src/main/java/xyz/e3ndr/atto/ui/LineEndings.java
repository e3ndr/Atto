package xyz.e3ndr.atto.ui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public enum LineEndings {
    MACINTOSH("\r", "Macintosh (CR)"),
    WINDOWS("\r\n", "Windows (CRLF)"),
    UNIX("\n", "Unix (LF)");

    private @Getter String separator;
    private String text;

    public static LineEndings detect(@NonNull String input) {
        boolean CR = input.contains("\r");
        boolean LF = input.contains("\n");

        if (CR && LF) {
            return WINDOWS;
        } else if (LF) {
            return UNIX;
        } else {
            return MACINTOSH;
        }
    }

    public static LineEndings fromString(@NonNull String in) {
        try {
            return LineEndings.valueOf(in.toUpperCase());
        } catch (Exception ignored) {
            return LineEndings.detect(System.lineSeparator());
        }
    }

    @Override
    public String toString() {
        return this.text;
    }

}
