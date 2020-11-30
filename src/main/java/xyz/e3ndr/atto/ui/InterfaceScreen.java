package xyz.e3ndr.atto.ui;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import xyz.e3ndr.atto.Atto;
import xyz.e3ndr.atto.config.AttoConfig.InterfaceTheme;
import xyz.e3ndr.atto.lang.LangProvider;
import xyz.e3ndr.atto.util.MiscUtil;
import xyz.e3ndr.consoleutil.ConsoleWindow;
import xyz.e3ndr.consoleutil.input.InputKey;
import xyz.e3ndr.consoleutil.input.KeyHook;
import xyz.e3ndr.consoleutil.input.KeyListener;

public class InterfaceScreen implements Screen, KeyListener {
    private StringBuilder buffer = new StringBuilder();
    private String query = "";

    // This handles pressing TAB to cycle through files.
    private File lastTab = new File("./"); // Set it so the program doesn't get confused.
    private int tabIndex = 0;

    private Atto atto;

    public InterfaceScreen(@NonNull Atto atto) {
        this.atto = atto;

        KeyHook.addListener(this);
    }

    @Override
    public void draw(@NonNull ConsoleWindow window, @NonNull Dimension size) throws Exception {
        // Write title bar
        InterfaceTheme theme = this.atto.getConfig().getInterfaceTheme();
        String middleText = String.format("Atto %s", Atto.VERSION);

        window.cursorTo(0, 0);
        window.setBackgroundColor(theme.getBackgroundColor());
        window.setTextColor(theme.getTextColor());
        window.setAttributes(theme.getTextAttributes());

        for (int line = 0; line != Atto.TOP_INDENT; line++) {
            window.clearLine(line);
        }

        window.write(makeTopBarText());
        window.writeAt(MiscUtil.getPaddingToCenter(middleText.length(), size.width), 0, middleText);

        if (this.atto.getScreenAction() == ScreenAction.EDITING_TEXT) {
            String lineEndings = this.atto.getTextEditorScreen().getLineEndings().toString();
            window.writeAt(size.width - lineEndings.length() - 1, 0, lineEndings);
        }

        // Write bottom bar
        window.cursorTo(0, size.height - Atto.BOTTOM_INDENT);
        window.setBackgroundColor(theme.getBackgroundColor());
        window.setTextColor(theme.getTextColor());
        window.setAttributes(theme.getTextAttributes());
        window.clearLine();

        if ((this.atto.getScreenAction() == ScreenAction.SAVE_QUERY) || (this.atto.getScreenAction() == ScreenAction.OPEN_QUERY)) {
            String[] splitQuery = this.query.split("%s", 2);

            window.write(splitQuery[0]);

            // We invert the color here
            window.setBackgroundColor(theme.getTextColor()).setTextColor(theme.getBackgroundColor());
            window.write(this.buffer).write(' ');

            window.setBackgroundColor(theme.getBackgroundColor()).setTextColor(theme.getTextColor());
            window.write(splitQuery[1]);

            // Move cursor
            window.cursorTo(splitQuery[0].length() + this.buffer.length(), size.height - Atto.BOTTOM_INDENT);
            window.saveCursorPosition();
        } else {
            String bottom;

            if (this.atto.getScreenAction() == ScreenAction.EDITING_TEXT) {
                bottom = LangProvider.get("hint.bottom.texteditor");
            } else if (this.atto.getScreenAction() == ScreenAction.EDITING_HEX) {
                bottom = LangProvider.get("hint.bottom.hexeditor");
            } else {
                return;
            }

            int padding = MiscUtil.getPaddingToCenter(bottom.length(), size.width);

            window.cursorTo(padding, size.height - Atto.BOTTOM_INDENT).write(bottom);
        }
    }

    public void triggerOpenDialog() throws IOException {
        if (this.atto.getScreenAction() == ScreenAction.EDITING_TEXT) {
            this.atto.setScreenAction(ScreenAction.OPEN_QUERY);

            this.buffer = new StringBuilder(new File("./").getCanonicalPath()).append(File.separatorChar);
            this.query = LangProvider.get("dialog.open");
        }
    }

    public void triggerSaveDialog() throws IOException {
        if (this.atto.getScreenAction() == ScreenAction.EDITING_TEXT) {
            this.atto.setScreenAction(ScreenAction.SAVE_QUERY);

            File file = this.atto.getEditorScreen().getFile();

            if (file != null) {
                this.buffer = new StringBuilder(file.getCanonicalPath());
            } else {
                this.buffer = new StringBuilder(new File("./").getCanonicalPath()).append(File.separatorChar);
            }

            this.query = LangProvider.get("dialog.save");
        }
    }

    private @NonNull String makeTopBarText() {
        List<String> topBar = new ArrayList<>();

        if (this.atto.getEditorScreen().isEdited()) {
            topBar.add(LangProvider.get("editor.unsaved"));
        }

        if (this.atto.getEditorScreen().isOverwriting()) {
            topBar.add(LangProvider.get("editor.overwriting"));
        } else {
            topBar.add(LangProvider.get("editor.inserting"));
        }

        return String.join(LangProvider.get("misc.listseperator"), topBar);
    }

    @Override
    public void onKey(char key, boolean alt, boolean control) {
        try {
            if (control) {
                switch (key) {
                    case 'e': {

                        return;
                    }

                    case 'o': { // ^O
                        this.triggerOpenDialog();
                        this.atto.draw();

                        return;
                    }

                    case 's': { // ^S
                        this.triggerSaveDialog();
                        this.atto.draw();

                        return;
                    }

                    case 'w': // ^W
                        System.exit(0);

                    default:
                        return;

                }
            } else {
                if ((this.atto.getScreenAction() == ScreenAction.SAVE_QUERY) || (this.atto.getScreenAction() == ScreenAction.OPEN_QUERY)) {
                    this.buffer.append(key);
                    this.atto.draw();
                }
            }
        } catch (IOException e) {
            this.atto.exception(e);
        }
    }

    @Override
    public void onKey(InputKey key) {
        try {
            if ((this.atto.getScreenAction() == ScreenAction.SAVE_QUERY) || (this.atto.getScreenAction() == ScreenAction.OPEN_QUERY)) {
                switch (key) {

                    case ENTER: {
                        try {
                            if (this.atto.getScreenAction() == ScreenAction.SAVE_QUERY) {
                                this.atto.getEditorScreen().save(this.getFileFromBuffer());
                            } else if (this.atto.getScreenAction() == ScreenAction.OPEN_QUERY) {
                                this.atto.getEditorScreen().load(this.getFileFromBuffer());
                            }
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }

                        return;
                    }

                    case BACK_SPACE: {
                        if (this.buffer.length() > 0) {
                            this.buffer.deleteCharAt(this.buffer.length() - 1);
                            this.atto.draw();
                        }

                        return;
                    }

                    case ESCAPE: {
                        this.atto.setScreenAction(ScreenAction.EDITING_TEXT);
                        this.atto.draw();

                        return;
                    }

                    case TAB: {
                        File file = this.getFileFromBuffer();

                        if (!file.exists()) {
                            file = this.lastTab;
                        } else if (file.getParentFile().equals(this.lastTab)) {
                            file = this.lastTab;
                        } else if (file.isFile()) {
                            file = file.getParentFile();
                        }

                        if (!file.equals(this.lastTab)) {
                            this.tabIndex = 0;
                        }

                        this.lastTab = file;

                        File[] possibilities = file.listFiles();

                        if (this.tabIndex >= possibilities.length) {
                            this.tabIndex = 0;
                        }

                        File selected = possibilities[this.tabIndex++];

                        this.buffer = new StringBuilder(selected.getCanonicalPath());

                        if (selected.isDirectory()) {
                            this.buffer.append(File.separator);
                        }

                        this.atto.draw();

                        return;
                    }

                    // Resets the lastTab, so you can tab through children
                    case DOWN: {
                        this.lastTab = this.getFileFromBuffer();
                        this.tabIndex = 0;

                        return;
                    }

                    // Jumps up a directory
                    case UP: {
                        char end = this.buffer.charAt(this.buffer.length() - 1);

                        if ((end == '\\') || (end == '/')) {
                            this.buffer.deleteCharAt(this.buffer.length() - 1);
                        }

                        int last = Math.max(this.buffer.lastIndexOf("/"), this.buffer.lastIndexOf("\\"));
                        this.tabIndex = 0;

                        if (last > -1) {
                            this.buffer.delete(last + 1, this.buffer.length());

                            this.atto.draw();
                        }

                        return;
                    }

                    case PAGE_UP:
                    case PAGE_DOWN:
                    case LEFT:
                    case RIGHT:
                    case INSERT:
                    case DELETE:
                    case HOME:
                    case END:
                    default:
                        return;

                }
            }
        } catch (IOException e) {
            this.atto.exception(e);
        }
    }

    private File getFileFromBuffer() {
        return new File(this.buffer.toString());
    }

}
