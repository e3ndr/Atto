package xyz.e3ndr.atto.ui;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.atto.Atto;
import xyz.e3ndr.atto.ThreadHelper;
import xyz.e3ndr.atto.lang.LangProvider;
import xyz.e3ndr.atto.util.CharMap;
import xyz.e3ndr.atto.util.Location;
import xyz.e3ndr.consoleutil.ConsoleColor;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.consoleutil.ConsoleWindow;
import xyz.e3ndr.consoleutil.input.InputKey;
import xyz.e3ndr.consoleutil.input.KeyHook;
import xyz.e3ndr.consoleutil.input.KeyListener;

public class EditorScreen implements Screen, KeyListener {
    private @NonNull Location cursor = new Location(0, 0);
    private @NonNull Location scroll = new Location(0, 0);

    private @NonNull CharMap map = new CharMap();
    private @Nullable @Getter File file;

    private @Getter boolean inserting = false;
    private @Getter boolean edited = false;

    private @NonNull Atto atto;

    public EditorScreen(@NonNull Atto atto) {
        this.atto = atto;

        KeyHook.addListener(this);
    }

    public void save(@NonNull File file) throws IOException, InterruptedException {
        if (this.atto.getMode() == EditorMode.SAVE_QUERY) {
            this.atto.setMode(EditorMode.WAITING);
            this.edited = false;
            this.file = file;

            String contents = String.join(System.lineSeparator(), this.map.string(0, 0, this.map.height(), -1, true));

            Files.write(this.file.toPath(), contents.getBytes());

            this.atto.setStatus(String.format(LangProvider.get("status.savedfile"), this.file.getCanonicalPath()));
            this.atto.setMode(EditorMode.EDITING_TEXT);

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

            if (this.file.exists()) {
                String contents = new String(Files.readAllBytes(this.file.toPath())).replace("\r", "").replace("\t", "    ");
                String[] lines = contents.split("\n");

                for (int y = 0; y != lines.length; y++) {
                    char[] line = lines[y].toCharArray();

                    this.map.ensureCapacity(line.length, lines.length);

                    this.map.setLine(y, line);
                }
            }

            this.atto.setStatus(this.file.getCanonicalPath());
        } else {
            this.atto.setStatus(LangProvider.get("status.newfile"));
        }

        this.atto.setMode(EditorMode.EDITING_TEXT);
    }

    @Override
    public void draw(@NonNull ConsoleWindow window, @NonNull Dimension size) throws IOException, InterruptedException {

        // Write contents.
        window.setBackgroundColor(ConsoleColor.BLACK).setTextColor(ConsoleColor.WHITE).cursorTo(0, Atto.TOP_INDENT); // Reset.

        String[] lines = this.map.string(this.scroll.x, this.scroll.y, (size.height - Atto.TOP_INDENT) - Atto.BOTTOM_INDENT, size.width, false);
        int num = 0;

        for (String line : lines) {
            window.cursorTo(0, Atto.TOP_INDENT + num).write(line);
            num++;
        }

        if (this.atto.getMode() == EditorMode.EDITING_TEXT) {
            window.cursorTo((this.cursor.x - this.scroll.x), (this.cursor.y - this.scroll.y) + Atto.TOP_INDENT);
            window.saveCursor();
        }
    }

    @SneakyThrows
    @Override
    public void onKey(int key) {
        if (this.atto.getMode() == EditorMode.EDITING_TEXT) {
            switch (key) {

                case 19: // ^S
                case 15: // ^O
                    return; // These are used in the InterfaceScreen.

                case 12: // ^L
                    this.map.clearLine(this.cursor.y);
                    this.atto.draw();

                    return;

                default: {
                    if (this.inserting) {
                        this.map.set(this.cursor.x, this.cursor.y, (char) key);
                    } else {
                        this.map.insert(this.cursor.x, this.cursor.y, (char) key);
                    }

                    this.edited = true;
                    this.move(1, 0);

                    return;
                }
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

    @SneakyThrows
    @Override
    public void onKey(InputKey key) {
        if (this.atto.getMode() == EditorMode.EDITING_TEXT) {
            switch (key) {

                case PAGE_UP: {
                    if (this.atto.getMode() == EditorMode.EDITING_TEXT) {
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
                    if (this.atto.getMode() == EditorMode.EDITING_TEXT) {
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
                    if (!this.inserting) {
                        this.cursor.x = this.map.getLength(this.cursor.y + 1);
                    }

                    this.move(0, 1);

                    return;
                }

                case BACK_SPACE: {
                    if (this.cursor.x > 0) {
                        if (this.inserting) {
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
                    this.inserting = !this.inserting;
                    this.atto.draw();

                    return;
                }

                case TAB: {
                    this.onKey(' ');
                    this.onKey(' ');
                    this.onKey(' ');
                    this.onKey(' ');

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

                    return;
                }

                case ALT:
                case END:
                default:
                    return;

            }
        }
    }

}
