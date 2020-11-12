package xyz.e3ndr.atto;

import java.awt.Dimension;
import java.io.File;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import xyz.e3ndr.atto.config.AttoConfig;
import xyz.e3ndr.atto.lang.LangProvider;
import xyz.e3ndr.atto.ui.InterfaceScreen;
import xyz.e3ndr.atto.ui.OptionsScreen;
import xyz.e3ndr.atto.ui.ScreenAction;
import xyz.e3ndr.atto.ui.TextEditorScreen;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.consoleutil.ConsoleWindow;

@Getter
@Setter
public class Atto {
    public static final String VERSION = "2.2.0";
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final int BOTTOM_INDENT = 1;
    public static final int TOP_INDENT = 1;

    private @NonNull ScreenAction screenAction = ScreenAction.EDITING_TEXT;
    private @NonNull String status;

    private @NonNull @Setter(AccessLevel.NONE) Dimension size = new Dimension();
    private @NonNull @Setter(AccessLevel.NONE) InterfaceScreen interfaceScreen;
    private @NonNull @Setter(AccessLevel.NONE) TextEditorScreen editorScreen;
    private @NonNull @Setter(AccessLevel.NONE) OptionsScreen optionsScreen;
    private @NonNull @Setter(AccessLevel.NONE) ConsoleWindow window;
    private @NonNull @Setter(AccessLevel.NONE) AttoConfig config;

    private @Setter(AccessLevel.NONE) boolean debug;

    public Atto(@Nullable File file, boolean debug, AttoConfig config) throws Exception {
        this.window = new ConsoleWindow().setAutoFlushing(false);
        this.config = config;

        if ((this.config.getWidth() > 0) && (this.config.getHeight() > 0)) {
            ConsoleUtil.setSize(this.config.getWidth(), this.config.getHeight());
        }

        LangProvider.setLanguage(this.config.getLanguage());

        this.editorScreen = new TextEditorScreen(this, this.config.getDefaultLineEndings());
        this.interfaceScreen = new InterfaceScreen(this);
        this.optionsScreen = new OptionsScreen(this);

        this.editorScreen.load(file);

        this.draw();

        // Auto redraw on a size change.
        try {
            TimeUnit.MILLISECONDS.sleep(250);

            // Occasionally when first starting, JAnsi won't have the native
            // library loaded so the console gets filled with garbage.
            this.draw();

            while (true) {
                TimeUnit.MILLISECONDS.sleep(10);

                boolean force = this.config.isForceSize() && ((this.config.getWidth() != this.size.width) || (this.config.getHeight() != this.size.height));

                if (!ConsoleUtil.getSize().equals(this.size) || force) {
                    if (force) {
                        ConsoleUtil.setSize(this.config.getWidth(), this.config.getHeight());
                    }

                    this.draw();
                }
            }
        } catch (InterruptedException e) {}
    }

    public void exception(Exception e) {
        ConsoleUtil.getJLine().enableInterruptCharacter();

        e.printStackTrace();

        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException ignored) {}
    }

    // We synchronize to make sure no race conditions cause graphical errors.
    public synchronized void draw() {
        try {
            this.size = ConsoleUtil.getSize();

            if (this.editorScreen.isEdited()) {
                ConsoleUtil.setTitle("Atto *" + LangProvider.get(this.status));
            } else {
                ConsoleUtil.setTitle("Atto " + LangProvider.get(this.status));
            }

            this.interfaceScreen.draw(this.window, this.size);
            this.optionsScreen.draw(this.window, this.size);
            this.editorScreen.draw(this.window, this.size);

            this.window.restoreCursorPosition().update();
        } catch (Exception e) {
            this.exception(e);
        }
    }

}
