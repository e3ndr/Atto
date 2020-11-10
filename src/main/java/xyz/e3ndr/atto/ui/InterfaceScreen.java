package xyz.e3ndr.atto.ui;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import xyz.e3ndr.atto.Atto;
import xyz.e3ndr.atto.lang.LangProvider;
import xyz.e3ndr.consoleutil.ConsoleWindow;
import xyz.e3ndr.consoleutil.ansi.ConsoleColor;
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
    public void draw(@NonNull ConsoleWindow window, @NonNull Dimension size) throws IOException, InterruptedException {
        // Write title bar
        String lineEndings = this.atto.getEditorScreen().getLineEndings().toString();
        String middleText = String.format("Atto %s", Atto.VERSION);

        window.cursorTo(0, 0).setBackgroundColor(ConsoleColor.GRAY).setTextColor(ConsoleColor.BLACK).clearLine();
        window.write(makeTopBarText());
        window.writeAt(getPaddingToCenter(middleText.length(), size.width), 0, middleText);
        window.writeAt(size.width - lineEndings.length() - 1, 0, lineEndings);

        // Write bottom bar
        window.cursorTo(0, size.height - Atto.BOTTOM_INDENT).setBackgroundColor(ConsoleColor.GRAY).setTextColor(ConsoleColor.BLACK).clearLine();

        if (this.atto.getMode() == EditorMode.EDITING_TEXT) {
            String bottom = LangProvider.get("hint.bottom");
            int padding = getPaddingToCenter(bottom.length(), size.width);

            window.cursorTo(padding, size.height - Atto.BOTTOM_INDENT).write(bottom);
        } else if ((this.atto.getMode() == EditorMode.SAVE_QUERY) || (this.atto.getMode() == EditorMode.OPEN_QUERY)) {
            String[] splitQuery = this.query.split("%s", 2);

            window.write(splitQuery[0]);

            window.setBackgroundColor(ConsoleColor.BLACK).setTextColor(ConsoleColor.GRAY);
            window.write(this.buffer).write(" ");

            window.setBackgroundColor(ConsoleColor.GRAY).setTextColor(ConsoleColor.BLACK);
            window.write(splitQuery[1]);

            // Move cursor
            window.cursorTo(splitQuery[0].length() + this.buffer.length(), size.height - Atto.BOTTOM_INDENT);
            window.saveCursorPosition();
        }
    }

    public void triggerOpenDialog() throws IOException {
        if (this.atto.getMode() == EditorMode.EDITING_TEXT) {
            this.atto.setMode(EditorMode.OPEN_QUERY);

            this.buffer = new StringBuilder(new File("./").getCanonicalPath()).append(File.separatorChar);
            this.query = LangProvider.get("dialog.open");
        }
    }

    public void triggerSaveDialog() throws IOException {
        if (this.atto.getMode() == EditorMode.EDITING_TEXT) {
            this.atto.setMode(EditorMode.SAVE_QUERY);

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
                    case 'o': { // ^O
                        this.atto.getInterfaceScreen().triggerOpenDialog();
                        this.atto.draw();

                        return;
                    }

                    case 's': { // ^S
                        this.atto.getInterfaceScreen().triggerSaveDialog();
                        this.atto.draw();

                        return;
                    }

                    case 'w': // ^W
                        System.exit(0);

                    default:
                        return;

                }
            } else {
                if ((this.atto.getMode() == EditorMode.SAVE_QUERY) || (this.atto.getMode() == EditorMode.OPEN_QUERY)) {
                    this.buffer.append(key);
                    this.atto.draw();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onKey(InputKey key) {
        try {
            if ((this.atto.getMode() == EditorMode.SAVE_QUERY) || (this.atto.getMode() == EditorMode.OPEN_QUERY)) {
                switch (key) {

                    case ENTER: {
                        try {
                            if (this.atto.getMode() == EditorMode.SAVE_QUERY) {
                                this.atto.getEditorScreen().save(this.getFileFromBuffer());
                            } else if (this.atto.getMode() == EditorMode.OPEN_QUERY) {
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
                        this.atto.setMode(EditorMode.EDITING_TEXT);
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
            e.printStackTrace();
        }
    }

    private File getFileFromBuffer() {
        return new File(this.buffer.toString());
    }

    public static int getPaddingToCenter(int length, int width) {
        return (width / 2) - (length / 2);
    }

}
