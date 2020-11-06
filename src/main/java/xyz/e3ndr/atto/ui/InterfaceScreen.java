package xyz.e3ndr.atto.ui;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import xyz.e3ndr.atto.Atto;
import xyz.e3ndr.atto.lang.LangProvider;
import xyz.e3ndr.consoleutil.ConsoleColor;
import xyz.e3ndr.consoleutil.ConsoleWindow;
import xyz.e3ndr.consoleutil.input.InputKey;
import xyz.e3ndr.consoleutil.input.KeyHook;
import xyz.e3ndr.consoleutil.input.KeyListener;

public class InterfaceScreen implements Screen, KeyListener {
    private @NonNull StringBuilder buffer = new StringBuilder();
    private @NonNull String query = "";

    private @NonNull Atto atto;

    public InterfaceScreen(@NonNull Atto atto) {
        this.atto = atto;

        KeyHook.addListener(this);
    }

    @Override
    public void draw(@NonNull ConsoleWindow window, @NonNull Dimension size) throws IOException, InterruptedException {
        // Write title bar
        window.cursorTo(0, 0).setBackgroundColor(ConsoleColor.WHITE).setTextColor(ConsoleColor.BLACK);
        window.replaceLine(makeTopBarText());

        // Write bottom bar
        window.cursorTo(0, size.height - Atto.BOTTOM_INDENT).setBackgroundColor(ConsoleColor.WHITE).setTextColor(ConsoleColor.BLACK);

        if (this.atto.getMode() == EditorMode.EDITING_TEXT) {
            window.replaceLine(LangProvider.get("hint.bottom"));
        } else if ((this.atto.getMode() == EditorMode.SAVE_QUERY) || (this.atto.getMode() == EditorMode.OPEN_QUERY)) {
            window.clearLine();

            String[] splitQuery = this.query.split("%s", 2);

            window.write(splitQuery[0]);

            window.setBackgroundColor(ConsoleColor.BLACK).setTextColor(ConsoleColor.WHITE);
            window.write(this.buffer).write(" ");

            window.setBackgroundColor(ConsoleColor.WHITE).setTextColor(ConsoleColor.BLACK);
            window.write(splitQuery[1]);

            // Move cursor
            window.cursorTo(splitQuery[0].length() + this.buffer.length(), size.height - Atto.BOTTOM_INDENT);
            window.saveCursor();
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

        if (this.atto.getEditorScreen().isInserting()) {
            topBar.add(LangProvider.get("editor.inserting"));
        }

        return String.format("Atto %s", String.join(LangProvider.get("misc.listseperator"), topBar));
    }

    @Override
    public void onKey(int key) {
        try {
            switch (key) {
                case 15: // ^O
                    this.atto.getInterfaceScreen().triggerOpenDialog();
                    this.atto.draw();

                    return;

                case 19: // ^S
                    this.atto.getInterfaceScreen().triggerSaveDialog();
                    this.atto.draw();

                    return;

                default: {
                    if ((this.atto.getMode() == EditorMode.SAVE_QUERY) || (this.atto.getMode() == EditorMode.OPEN_QUERY)) {
                        this.buffer.append((char) key);
                        this.atto.draw();
                    }

                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onKey(InputKey key) {
        if ((this.atto.getMode() == EditorMode.SAVE_QUERY) || (this.atto.getMode() == EditorMode.OPEN_QUERY)) {
            switch (key) {

                case ENTER: {
                    try {
                        if (this.atto.getMode() == EditorMode.SAVE_QUERY) {
                            this.atto.getEditorScreen().save(new File(this.buffer.toString()));
                        } else if (this.atto.getMode() == EditorMode.OPEN_QUERY) {
                            this.atto.getEditorScreen().load(new File(this.buffer.toString()));
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

                case PAGE_UP:
                case PAGE_DOWN:
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                case INSERT:
                case TAB:
                case DELETE:
                case HOME:
                case ALT:
                case END:
                default:
                    return;

            }
        }
    }

}
