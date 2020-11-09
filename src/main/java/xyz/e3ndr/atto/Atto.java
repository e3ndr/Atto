package xyz.e3ndr.atto;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import xyz.e3ndr.atto.lang.LangProvider;
import xyz.e3ndr.atto.ui.EditorMode;
import xyz.e3ndr.atto.ui.InterfaceScreen;
import xyz.e3ndr.atto.ui.LineEndings;
import xyz.e3ndr.atto.ui.TextEditorScreen;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.consoleutil.ConsoleWindow;

@Getter
@Setter
public class Atto {
    public static final String VERSION = "2.1.1";
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final int BOTTOM_INDENT = 1;
    public static final int TOP_INDENT = 1;

    private @NonNull EditorMode mode = EditorMode.EDITING_TEXT;
    private @NonNull String status;

    private @NonNull @Setter(AccessLevel.NONE) Dimension size = new Dimension();
    private @NonNull @Setter(AccessLevel.NONE) InterfaceScreen interfaceScreen;
    private @NonNull @Setter(AccessLevel.NONE) TextEditorScreen editorScreen;
    private @NonNull @Setter(AccessLevel.NONE) ConsoleWindow window;

    public Atto(@Nullable File file, ConfigFile config) throws IOException, InterruptedException {
        this.window = new ConsoleWindow();

        if ((config.getWidth() > 0) && (config.getHeight() > 0)) {
            ConsoleUtil.setSize(config.getWidth(), config.getHeight());
        }

        LangProvider.setLanguage(config.getLanguage());

        this.interfaceScreen = new InterfaceScreen(this);
        this.editorScreen = new TextEditorScreen(this, LineEndings.fromString(config.getDefaultLineEndings()));

        this.editorScreen.load(file);

        this.draw();

        // Auto redraw on a size change.
        try {
            while (true) {
                TimeUnit.MILLISECONDS.sleep(10);

                boolean force = config.isForceSize() && ((config.getWidth() != this.size.width) || (config.getHeight() != this.size.height));

                if (!ConsoleUtil.getSize().equals(this.size) || force) {
                    if (force) {
                        ConsoleUtil.setSize(config.getWidth(), config.getHeight());
                    }

                    this.draw();
                }
            }
        } catch (InterruptedException e) {}
    }

    // We synchronize to make sure no race conditions cause graphical errors.
    public synchronized void draw() {
        try {
            this.size = ConsoleUtil.getSize();

            if (this.editorScreen.isEdited()) {
                ConsoleUtil.setTitle("Atto *" + this.status);
            } else {
                ConsoleUtil.setTitle("Atto " + this.status);
            }

            this.interfaceScreen.draw(this.window, this.size);
            this.editorScreen.draw(this.window, this.size);

            this.window.restoreCursor().update();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
