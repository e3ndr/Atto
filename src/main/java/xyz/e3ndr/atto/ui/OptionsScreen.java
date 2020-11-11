package xyz.e3ndr.atto.ui;

import java.awt.Dimension;
import java.io.IOException;

import lombok.NonNull;
import xyz.e3ndr.atto.Atto;
import xyz.e3ndr.atto.config.ConfigFile.InterfaceTheme;
import xyz.e3ndr.atto.util.MiscUtil;
import xyz.e3ndr.consoleutil.ConsoleWindow;
import xyz.e3ndr.consoleutil.input.InputKey;
import xyz.e3ndr.consoleutil.input.KeyHook;
import xyz.e3ndr.consoleutil.input.KeyListener;

public class OptionsScreen implements Screen, KeyListener {
    private int optionIndex = 0;
    private Object[] options;

    private Atto atto;

    public OptionsScreen(@NonNull Atto atto) {
        this.atto = atto;

        KeyHook.addListener(this);
    }

    @Override
    public void draw(@NonNull ConsoleWindow window, @NonNull Dimension size) throws IOException, InterruptedException {
        if (this.atto.getMode() == EditorMode.OPTIONS) {
            // Write title bar
            String middleText = String.format("Atto %s", Atto.VERSION);
            InterfaceTheme theme = this.atto.getConfig().getInterfaceTheme();

            window.cursorTo(0, 0);
            window.setBackgroundColor(theme.getBackgroundColor());
            window.setTextColor(theme.getTextColor());
            window.setAttributes(theme.getTextAttributes());
            window.clearLine();

            window.writeAt(MiscUtil.getPaddingToCenter(middleText.length(), size.width), 0, middleText);
        }
    }

    @Override
    public void onKey(char key, boolean alt, boolean control) {
        if (this.atto.getMode() == EditorMode.OPTIONS) {
            if (control) {
                switch (key) {
                    case 'p': { // ^P
                        this.atto.setMode(EditorMode.OPTIONS);
                        this.atto.draw();
                        return;
                    }

                    default:
                        return;

                }
            }
        }
    }

    @Override
    public void onKey(InputKey key) {
        if (this.atto.getMode() == EditorMode.OPTIONS) {
            switch (key) {

                case ESCAPE: {

                }

                case UP: {

                }

                case TAB:
                case DOWN: {

                }

                case BACK_SPACE:
                case DELETE:
                case LEFT: {

                }

                case ENTER:
                case RIGHT: {

                }

                case PAGE_UP:
                case PAGE_DOWN:
                case INSERT:
                case HOME:
                case END:
                default:
                    break;

            }
        }
    }

}
