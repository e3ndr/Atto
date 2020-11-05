package xyz.e3ndr.atto;

import java.io.IOException;

import lombok.NonNull;
import xyz.e3ndr.atto.ui.Editor;
import xyz.e3ndr.consoleutil.ConsoleWindow;

public class Atto {
    public static final String VERSION = "1.1.1";

    public static final int BOTTOM_INDENT = 1;
    public static final int TOP_INDENT = 1;

    private ConsoleWindow window;
    private Editor editorScreen;

    public Atto(@NonNull Launcher config) throws IOException, InterruptedException {
        this.window = new ConsoleWindow();

        this.editorScreen = new Editor(this.window);

        this.editorScreen.load(config.getFile());
    }

}
