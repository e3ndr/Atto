package xyz.e3ndr.atto.highlighting;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import xyz.e3ndr.consoleutil.ansi.ConsoleColor;

@Getter
public class HighlighterConfig {
    private List<HighlighterRule> rules;
    private ConsoleColor override;
    private List<String> aliases;

    public void sort() {
        Collections.sort(this.rules, (o1, o2) -> {
            return (o1.getPriority() > o2.getPriority()) ? 1 : -1;
        });
    }

}
