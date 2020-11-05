package xyz.e3ndr.atto.ui;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;
import xyz.e3ndr.atto.Atto;
import xyz.e3ndr.atto.ThreadHelper;
import xyz.e3ndr.atto.util.CharMap;
import xyz.e3ndr.atto.util.Location;
import xyz.e3ndr.consoleutil.ConsoleColor;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.consoleutil.ConsoleWindow;
import xyz.e3ndr.consoleutil.input.InputKey;
import xyz.e3ndr.consoleutil.input.KeyHook;
import xyz.e3ndr.consoleutil.input.KeyListener;

public class Editor implements Screen, KeyListener {
    private StringBuilder buffer = new StringBuilder();
    private Location cursor = new Location(0, 0);
    private Location scroll = new Location(0, 0);
    private EditorMode mode = EditorMode.EDITING;
    private CharMap map = new CharMap();
    private boolean inserting = false;
    private ConsoleWindow window;
    private boolean edited;
    private String status;
    private File file;

    public Editor(ConsoleWindow window) {
        this.window = window;

        KeyHook.addListener(this);
    }

    public void save() throws IOException, InterruptedException {
        if (this.mode == EditorMode.EDITING) {
            this.mode = EditorMode.SAVE_QUERY;

            if ((this.file != null)) {
                this.buffer = new StringBuilder(this.file.getCanonicalPath());
            } else {
                this.buffer = new StringBuilder();
            }

            this.draw();
        } else if (this.mode == EditorMode.SAVE_QUERY) {
            this.mode = EditorMode.WAITING;
            this.edited = false;

            String contents = String.join(System.lineSeparator(), this.map.string(0, 0, this.map.height(), -1, true));

            Files.write(this.file.toPath(), contents.getBytes());

            this.status = String.format("Saved file : %s", this.file.getCanonicalPath());
            this.mode = EditorMode.EDITING;

            this.draw();

            ThreadHelper.executeLater(() -> {
                try {
                    this.status = this.file.getCanonicalPath();

                    this.draw();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }, 2000);
        }
    }

    public void load(File file) throws IOException, InterruptedException {
        this.file = file;
        this.map = new CharMap();

        if ((this.file != null) && file.exists()) {
            this.status = String.format("Reading file : %s", this.file.getCanonicalPath());
            this.draw();

            if (this.file.exists()) {
                String contents = new String(Files.readAllBytes(this.file.toPath())).replace("\r", "").replace("\t", "    ");
                String[] lines = contents.split("\n");

                for (int y = 0; y != lines.length; y++) {
                    char[] line = lines[y].toCharArray();

                    this.map.ensureCapacity(line.length, lines.length);

                    this.map.setLine(y, line);
                }
            }

            this.status = this.file.getCanonicalPath();
        } else {
            this.status = "(New File)";
        }

        this.mode = EditorMode.EDITING;
        this.draw();
    }

    @Override
    public void draw() throws IOException, InterruptedException {
        Dimension size = ConsoleUtil.getSize();

        if (this.edited) {
            ConsoleUtil.setTitle("Atto *" + this.status);
        } else {
            ConsoleUtil.setTitle("Atto " + this.status);
        }

        // Reset color
        this.window.cursorTo(0, 0).setBackgroundColor(ConsoleColor.BLACK).setTextColor(ConsoleColor.WHITE);

        // Write title bar
        this.window.setBackgroundColor(ConsoleColor.WHITE).setTextColor(ConsoleColor.BLACK);
        this.window.replaceLine(makeTopBarText());

        // Write contents.
        this.window.setBackgroundColor(ConsoleColor.BLACK).setTextColor(ConsoleColor.WHITE).cursorTo(0, Atto.TOP_INDENT); // Reset.

        String[] lines = this.map.string(this.scroll.x, this.scroll.y, (size.height - Atto.TOP_INDENT) - Atto.BOTTOM_INDENT, size.width, false);
        int num = 0;

        for (String line : lines) {
            this.window.cursorTo(0, Atto.TOP_INDENT + num).write(line);
            num++;
        }

        // Write bottom bar
        this.window.cursorTo(0, size.height - Atto.BOTTOM_INDENT).setBackgroundColor(ConsoleColor.WHITE).setTextColor(ConsoleColor.BLACK);

        if (this.mode == EditorMode.EDITING) {
            this.window.replaceLine("^S Save File    ^L Clear Line    ^O Open File");
        } else if ((this.mode == EditorMode.SAVE_QUERY) || (this.mode == EditorMode.OPEN_QUERY)) {
            this.window.clearLine();
            this.window.write("File: ");
            this.window.setBackgroundColor(ConsoleColor.BLACK).setTextColor(ConsoleColor.WHITE);
            this.window.write(this.buffer);
        }

        // Move cursor
        if (this.mode == EditorMode.EDITING) {
            this.window.cursorTo((this.cursor.x - this.scroll.x), (this.cursor.y - this.scroll.y) + Atto.TOP_INDENT);
        } else if ((this.mode == EditorMode.SAVE_QUERY) || (this.mode == EditorMode.OPEN_QUERY)) {
            // Approximate cursor location.
            this.window.cursorTo("File: ".length() + this.buffer.length(), size.height - Atto.BOTTOM_INDENT);
        }

        // Update
        this.window.update();
    }

    private String makeTopBarText() {
        List<String> topBar = new ArrayList<>();

        if (this.edited) {
            topBar.add("UNSAVED");
        }

        if (this.inserting) {
            topBar.add("INS");
        }

        return String.format("Atto [%d,%d] [%d,%d] %s", this.cursor.x, this.cursor.y, this.scroll.x, this.scroll.y, String.join(", ", topBar));
    }

    @SneakyThrows
    @Override
    public void onKey(int key) {
        if (this.mode == EditorMode.EDITING) {
            switch (key) {

                case 15: // ^O
                    this.mode = EditorMode.OPEN_QUERY;
                    this.buffer = new StringBuilder();
                    this.draw();
                    return;

                case 19: // ^S
                    this.save();
                    return;

                case 12: // ^L
                    this.map.clearLine(this.cursor.y);
                    this.draw();
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
        } else if ((this.mode == EditorMode.SAVE_QUERY) || (this.mode == EditorMode.OPEN_QUERY)) {
            this.buffer.append((char) key);
            this.draw();
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

        this.draw();
    }

    @SneakyThrows
    @Override
    public void onKey(InputKey key) {
        switch (key) {

            case PAGE_UP: {
                if (this.mode == EditorMode.EDITING) {
                    if (this.scroll.y > 0) {
                        this.scroll.y--;
                        this.cursor.y--;
                        this.draw();
                    } else {
                        ConsoleUtil.bell();
                    }
                }
                return;
            }

            case PAGE_DOWN: {
                if (this.mode == EditorMode.EDITING) {
                    this.scroll.y++;
                    this.cursor.y++;
                    this.draw();
                }
                return;
            }

            // Cursoring
            case UP: {
                if (this.mode == EditorMode.EDITING) {
                    if (this.cursor.y > 0) {
                        this.move(0, -1);
                    } else {
                        ConsoleUtil.bell();
                    }
                }
                return;
            }

            case DOWN: {
                if (this.mode == EditorMode.EDITING) {
                    this.move(0, 1);
                }
                return;
            }

            case LEFT: {
                if (this.mode == EditorMode.EDITING) {
                    if (this.cursor.x > 0) {
                        this.move(-1, 0);
                    } else if (this.cursor.y > 0) {
                        this.cursor.x = this.map.getLength(this.cursor.y - 1);
                        this.move(0, -1);
                    } else {
                        ConsoleUtil.bell();
                    }
                }
                return;
            }

            case RIGHT: {
                this.move(1, 0);
                return;
            }

            // Editing
            case ENTER: {
                if (this.mode == EditorMode.EDITING) {
                    if (!this.inserting) {
                        this.cursor.x = this.map.getLength(this.cursor.y + 1);
                    }

                    this.move(0, 1);
                } else if (this.mode == EditorMode.SAVE_QUERY) {
                    this.file = new File(this.buffer.toString());

                    this.save();
                } else if (this.mode == EditorMode.OPEN_QUERY) {
                    this.load(new File(this.buffer.toString()));
                }
                return;
            }

            case BACK_SPACE: {
                if (this.mode == EditorMode.EDITING) {
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
                } else if ((this.mode == EditorMode.SAVE_QUERY) || (this.mode == EditorMode.OPEN_QUERY)) {
                    if (this.buffer.length() > 0) {
                        this.buffer.deleteCharAt(this.buffer.length() - 1);
                        this.draw();
                    }
                }
                return;
            }

            case INSERT: {
                if (this.mode == EditorMode.EDITING) {
                    this.inserting = !this.inserting;
                    this.draw();
                }
                return;
            }

            case ESCAPE: {
                if ((this.mode == EditorMode.SAVE_QUERY) || (this.mode == EditorMode.OPEN_QUERY)) {
                    this.mode = EditorMode.EDITING;
                    this.draw();
                }
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
                this.draw();
                return;
            }

            case ALT:
            case END:
            default:
                return;

        }
    }

}
