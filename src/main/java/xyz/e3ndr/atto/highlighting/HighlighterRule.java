package xyz.e3ndr.atto.highlighting;

import lombok.Getter;
import xyz.e3ndr.consoleutil.ansi.ConsoleColor;

@Getter
public class HighlighterRule {
    private ConsoleColor color;
    private String match;
    private int priority;

}
