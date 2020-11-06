package xyz.e3ndr.atto;

import java.awt.Dimension;
import java.io.IOException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import xyz.e3ndr.atto.ui.EditorMode;
import xyz.e3ndr.atto.ui.EditorScreen;
import xyz.e3ndr.atto.ui.InterfaceScreen;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.consoleutil.ConsoleWindow;

@Getter
@Setter
public class Atto {
    public static final String VERSION = "1.2.0";

    public static final int BOTTOM_INDENT = 1;
    public static final int TOP_INDENT = 1;

    private @NonNull EditorMode mode = EditorMode.EDITING_TEXT;
    private @NonNull String status;

    private @NonNull @Setter(AccessLevel.NONE) InterfaceScreen interfaceScreen;
    private @NonNull @Setter(AccessLevel.NONE) EditorScreen editorScreen;
    private @NonNull @Setter(AccessLevel.NONE) ConsoleWindow window;

    public Atto(@NonNull Launcher config) throws IOException, InterruptedException {
        this.window = new ConsoleWindow();

        this.interfaceScreen = new InterfaceScreen(this);
        this.editorScreen = new EditorScreen(this);

        this.editorScreen.load(config.getFile());

        this.draw();
    }

    // We synchronize to make sure no race conditions cause graphical errors.
    public synchronized void draw() {
        try {
            Dimension size = ConsoleUtil.getSize();

            if (this.editorScreen.isEdited()) {
                ConsoleUtil.setTitle("Atto *" + this.status);
            } else {
                ConsoleUtil.setTitle("Atto " + this.status);
            }

            this.interfaceScreen.draw(this.window, size);
            this.editorScreen.draw(this.window, size);

            this.window.restoreCursor().update();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
