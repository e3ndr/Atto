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
import xyz.e3ndr.atto.util.ByteMap;
import xyz.e3ndr.atto.util.EnumUtil;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.consoleutil.ConsoleWindow;
import xyz.e3ndr.consoleutil.input.InputKey;
import xyz.e3ndr.consoleutil.input.KeyHook;
import xyz.e3ndr.consoleutil.input.KeyListener;

public class HexEditorScreen implements EditorScreen, KeyListener {
    private int cursor = 0;
    private int scroll = 0;

    private @NonNull @Getter @Setter LineEndings lineEndings;
    private @Nullable @Getter File file;
    private ByteMap map = new ByteMap(new byte[0]);

    private @Getter boolean overwriting = false;
    private @Getter boolean edited = false;

    private Atto atto;

    public HexEditorScreen(@NonNull Atto atto, @NonNull LineEndings lineEndings) {
        this.atto = atto;
        this.lineEndings = lineEndings;

        KeyHook.addListener(this);
    }

    @Override
    public void save(@NonNull File file) throws IOException, InterruptedException {
        if (this.atto.getScreenAction() == ScreenAction.SAVE_QUERY) {
            this.atto.setScreenAction(ScreenAction.WAITING);
            this.edited = false;
            this.file = file;

            Files.write(this.file.toPath(), this.map.getArray());

            this.atto.setStatus(String.format(LangProvider.get("status.savedfile"), this.file.getCanonicalPath()));
            this.atto.setScreenAction(ScreenAction.EDITING_HEX);
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

    @Override
    public void load(@Nullable File file) throws IOException, InterruptedException {
        this.file = file;
        this.map = new ByteMap(new byte[0]);

        if ((this.file != null) && file.exists() && file.isFile()) {
            this.atto.setStatus(String.format(LangProvider.get("status.readingfile"), this.file.getCanonicalPath()));
            this.atto.draw();

            if (this.file.isFile()) {
                this.map = new ByteMap(Files.readAllBytes(this.file.toPath()));
            }

            this.atto.setStatus(this.file.getCanonicalPath());
        } else

        {
            this.atto.setStatus("status.newfile");
        }

        this.atto.setScreenAction(ScreenAction.EDITING_HEX);
        this.atto.draw();
    }

    @Override
    public void draw(@NonNull ConsoleWindow window, @NonNull Dimension size) throws Exception {
        if (this.atto.getScreenAction() != ScreenAction.OPTIONS) {
            TextEditorTheme theme = this.atto.getConfig().getTextEditorTheme();

            window.setBackgroundColor(theme.getBackgroundColor()).setTextColor(theme.getTextColor());
            window.setAttributes(theme.getTextAttributes()).cursorTo(0, Atto.TOP_INDENT); // Reset.

            // Write contents.
//             String contents = this.map.string("\n");

            // window.cursorTo(0,
            // Atto.TOP_INDENT).write(MiscUtil.subStringWithColor(highlighted,
            // this.scroll.x, this.scroll.y, size.width - 2, size.height)); // Sub 2 because
            // of the top indent and bottom indent

            if (this.atto.getScreenAction() == ScreenAction.EDITING_HEX) {
                // window.cursorTo((this.cursor.x - this.scroll.x), (this.cursor.y -
                // this.scroll.y) + Atto.TOP_INDENT);
                window.saveCursorPosition();
            }
        }
    }

    @SneakyThrows
    @Override
    public void onKey(char key, boolean alt, boolean control) {
        if (this.atto.getScreenAction() == ScreenAction.EDITING_HEX) {
            if (!control) {
                // TODO
            }
        }
    }

    private void move(int dX) throws IOException, InterruptedException {
        Dimension size = ConsoleUtil.getSize();

        size.height -= Atto.BOTTOM_INDENT;
        size.height -= Atto.TOP_INDENT;

        // Convert to 0index
        size.height--;
        size.width--;

        if ((this.cursor == 0) && (dX < 0)) {
            dX = 0;
        }

        int newX = (this.cursor - this.scroll) + dX;

        if ((newX > size.width) || ((newX < 0) && (this.scroll > 0))) {
            this.scroll += dX;
            this.cursor += dX;
        } else {
            this.cursor += dX;
        }

        this.atto.draw();
    }

    @SneakyThrows
    @Override
    public void onKey(InputKey key) {
        if (this.atto.getScreenAction() == ScreenAction.EDITING_HEX) {
            switch (key) {

                case END: {
                    this.lineEndings = EnumUtil.getNext(this.lineEndings);
                    this.edited = true;
                    this.atto.draw();

                    return;
                }

                case PAGE_UP: {
                    if (this.atto.getScreenAction() == ScreenAction.EDITING_HEX) {
                        if (this.scroll > 0) {
                            this.scroll--;
                            this.cursor--;
                            this.atto.draw();
                        } else {
                            ConsoleUtil.bell();
                        }
                    }

                    return;
                }

                case PAGE_DOWN: {
                    if (this.atto.getScreenAction() == ScreenAction.EDITING_HEX) {
                        this.scroll++;
                        this.cursor++;
                        this.atto.draw();
                    }

                    return;
                }

                // Cursoring
                case LEFT:
                case UP: {
                    this.move(-1);

                    return;
                }

                case RIGHT:
                case DOWN: {
                    this.move(1);

                    return;
                }

                // Editing
                case ENTER: {
                    // TODO set hex input

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

                case HOME: {
                    this.cursor = 0;
                    this.scroll = 0;
                    this.atto.draw();

                    return;
                }

                case DELETE:
                case BACK_SPACE:
                default:
                    return;

            }
        }
    }

}
