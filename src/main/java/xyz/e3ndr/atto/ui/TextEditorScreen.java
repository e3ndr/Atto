package xyz.e3ndr.atto.ui;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import xyz.e3ndr.atto.Atto;
import xyz.e3ndr.atto.ThreadHelper;
import xyz.e3ndr.atto.config.AttoConfig.TextEditorTheme;
import xyz.e3ndr.atto.lang.LangProvider;
import xyz.e3ndr.atto.util.CharMap;
import xyz.e3ndr.atto.util.EnumUtil;
import xyz.e3ndr.atto.util.Vector2;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.consoleutil.ConsoleWindow;
import xyz.e3ndr.consoleutil.input.InputKey;
import xyz.e3ndr.consoleutil.input.KeyHook;
import xyz.e3ndr.consoleutil.input.KeyListener;

public class TextEditorScreen implements Screen, KeyListener {
    private Vector2 cursor = new Vector2(0, 0);
    private Vector2 scroll = new Vector2(0, 0);

    private @NonNull @Getter @Setter LineEndings lineEndings;
    private @Nullable @Getter File file;
    private CharMap map = new CharMap();

    private @Getter boolean overwriting = false;
    private @Getter boolean edited = false;

    private Atto atto;

    public TextEditorScreen(@NonNull Atto atto, @NonNull LineEndings lineEndings) {
        this.atto = atto;
        this.lineEndings = lineEndings;

        KeyHook.addListener(this);
    }

    public void save(@NonNull File file) throws IOException, InterruptedException {
        if (this.atto.getScreenAction() == ScreenAction.SAVE_QUERY) {
            this.atto.setScreenAction(ScreenAction.WAITING);
            this.edited = false;
            this.file = file;

            String contents = String.join(this.lineEndings.getSeparator(), this.map.string(0, 0, this.map.height(), -1, true));

            Files.write(this.file.toPath(), contents.getBytes());

            this.atto.setStatus(String.format(LangProvider.get("status.savedfile"), this.file.getCanonicalPath()));
            this.atto.setScreenAction(ScreenAction.EDITING_TEXT);
            this.atto.draw();

            ThreadHelper.executeLater(() -> {
                try {
                    this.atto.setStatus(this.file.getCanonicalPath());

                    this.atto.draw();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, 2000);
        }
    }

    public void load(@Nullable File file) throws IOException, InterruptedException {
        this.file = file;
        this.map = new CharMap();

        if ((this.file != null) && file.exists() && file.isFile()) {
            this.atto.setStatus(String.format(LangProvider.get("status.readingfile"), this.file.getCanonicalPath()));
            this.atto.draw();

            if (this.file.isFile()) {
                String contents = new String(Files.readAllBytes(this.file.toPath())).replace("\t", "    ");
                String[] lines = contents.replace("\r", "\n").split("\n");

                this.lineEndings = LineEndings.detect(contents);

                for (int y = 0; y != lines.length; y++) {
                    char[] line = lines[y].toCharArray();

                    this.map.ensureCapacity(line.length, lines.length);

                    this.map.setLine(y, line);
                }
            }

            this.atto.setStatus(this.file.getCanonicalPath());
        } else {
            this.atto.setStatus("status.newfile");
        }

        this.atto.setScreenAction(ScreenAction.EDITING_TEXT);
        this.atto.draw();
    }

    @Override
    public void draw(@NonNull ConsoleWindow window, @NonNull Dimension size) throws Exception {
        if (this.atto.getScreenAction() != ScreenAction.OPTIONS) {
            TextEditorTheme theme = this.atto.getConfig().getTextEditorTheme();

            window.setBackgroundColor(theme.getBackgroundColor()).setTextColor(theme.getTextColor());
            window.setAttributes(theme.getTextAttributes()).cursorTo(0, Atto.TOP_INDENT); // Reset.

            // Write contents.
            String[] lines = this.map.string(this.scroll.x, this.scroll.y, (size.height - Atto.TOP_INDENT) - Atto.BOTTOM_INDENT, size.width, false);
            int num = 0;

            for (String line : lines) {
                window.cursorTo(0, Atto.TOP_INDENT + num).write(line);
                num++;
            }

            if (this.atto.getScreenAction() == ScreenAction.EDITING_TEXT) {
                window.cursorTo((this.cursor.x - this.scroll.x), (this.cursor.y - this.scroll.y) + Atto.TOP_INDENT);
                window.saveCursorPosition();
            }
        }
    }

    @SneakyThrows
    @Override
    public void onKey(char key, boolean alt, boolean control) {
        if (this.atto.getScreenAction() == ScreenAction.EDITING_TEXT) {
            if (control) {
                switch (key) {

                    case 'l': // ^L
                        this.map.clearLine(this.cursor.y);
                        this.cursor.x = 0;
                        this.atto.draw();

                        return;

                }
            } else {
                if (this.overwriting) {
                    this.map.set(this.cursor.x, this.cursor.y, key);
                } else {
                    this.map.insert(this.cursor.x, this.cursor.y, key);
                }

                this.edited = true;
                this.move(1, 0);

                return;
            }
        }
    }

    private void move(int dX, int dY) throws IOException, InterruptedException {
        Dimension size = ConsoleUtil.getSize();

        size.height -= Atto.BOTTOM_INDENT;
        size.height -= Atto.TOP_INDENT;

        // Convert to 0index
        size.height--;
        size.width--;

        if ((this.cursor.x == 0) && (dX < 0)) {
            dX = 0;
        }

        if ((this.cursor.y == 0) && (dY < 0)) {
            dY = 0;
        }

        int newX = (this.cursor.x - this.scroll.x) + dX;
        int newY = (this.cursor.y - this.scroll.y) + dY;

        if ((newX > size.width) || ((newX < 0) && (this.scroll.x > 0))) {
            this.scroll.x += dX;
            this.cursor.x += dX;
        } else {
            this.cursor.x += dX;
        }

        if ((newY > size.height) || ((newY < 0) && (this.scroll.y > 0))) {
            this.scroll.y += dY;
            this.cursor.y += dY;
        } else {
            this.cursor.y += dY;
        }

        this.atto.draw();
    }

    public char getCharAtCursor() {
        return this.map.getChar(this.cursor.x + this.scroll.x, this.cursor.y + this.scroll.y);
    }

    @SneakyThrows
    @Override
    public void onKey(InputKey key) {
        if (this.atto.getScreenAction() == ScreenAction.EDITING_TEXT) {
            switch (key) {

                case END: {
                    this.lineEndings = EnumUtil.getNext(this.lineEndings);
                    this.edited = true;
                    this.atto.draw();

                    return;
                }

                case PAGE_UP: {
                    if (this.atto.getScreenAction() == ScreenAction.EDITING_TEXT) {
                        if (this.scroll.y > 0) {
                            this.scroll.y--;
                            this.cursor.y--;
                            this.atto.draw();
                        } else {
                            ConsoleUtil.bell();
                        }
                    }

                    return;
                }

                case PAGE_DOWN: {
                    if (this.atto.getScreenAction() == ScreenAction.EDITING_TEXT) {
                        this.scroll.y++;
                        this.cursor.y++;
                        this.atto.draw();
                    }

                    return;
                }

                // Cursoring
                case UP: {
                    if (this.cursor.y > 0) {
                        this.move(0, -1);
                    } else {
                        ConsoleUtil.bell();
                    }

                    return;
                }

                case DOWN: {
                    this.move(0, 1);

                    return;
                }

                case LEFT: {
                    if (this.cursor.x > 0) {
                        this.move(-1, 0);
                    } else if (this.cursor.y > 0) {
                        this.cursor.x = this.map.getLength(this.cursor.y - 1);
                        this.move(0, -1);
                    } else {
                        ConsoleUtil.bell();
                    }

                    return;
                }

                case RIGHT: {
                    this.move(1, 0);
                    return;
                }

                // Editing
                case ENTER: {
                    if (!this.overwriting) {
                        this.cursor.x = this.map.getLength(this.cursor.y + 1);
                    }

                    this.move(0, 1);

                    return;
                }

                case BACK_SPACE: {
                    if (this.cursor.x > 0) {
                        if (this.overwriting) {
                            this.map.set(this.cursor.x - 1, this.cursor.y, ' ');
                        } else {
                            this.map.remove(this.cursor.x - 1, this.cursor.y);
                        }

                        this.edited = true;
                        this.move(-1, 0);
                    } else {
                        ConsoleUtil.bell();
                    }

                    return;
                }

                case INSERT: {
                    this.overwriting = !this.overwriting;
                    this.atto.draw();

                    return;
                }

                case TAB: {
                    this.onKey(' ', false, false);
                    this.onKey(' ', false, false);
                    this.onKey(' ', false, false);
                    this.onKey(' ', false, false);

                    return;
                }

                case DELETE: {
                    this.map.set(this.cursor.x, this.cursor.y, ' ');
                    this.edited = true;
                    this.move(-1, 0);

                    return;
                }

                case HOME: {
                    this.cursor.x = 0;
                    this.cursor.y = 0;
                    this.scroll.x = 0;
                    this.scroll.y = 0;
                    this.atto.draw();

                    return;
                }

                default:
                    return;

            }
        }
    }

}
