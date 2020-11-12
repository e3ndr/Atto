package xyz.e3ndr.atto.ui;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;

import lombok.NonNull;
import xyz.e3ndr.atto.Atto;
import xyz.e3ndr.atto.config.AttoConfig.InterfaceTheme;
import xyz.e3ndr.atto.config.menu.interactions.Interactable;
import xyz.e3ndr.atto.config.menu.interactions.InteractableList;
import xyz.e3ndr.atto.lang.LangProvider;
import xyz.e3ndr.atto.util.MiscUtil;
import xyz.e3ndr.consoleutil.ConsoleWindow;
import xyz.e3ndr.consoleutil.input.InputKey;
import xyz.e3ndr.consoleutil.input.KeyHook;
import xyz.e3ndr.consoleutil.input.KeyListener;

public class OptionsScreen implements Screen, KeyListener {
    private List<Interactable<?>> options;
    private int optionIndex = 0;

    private Atto atto;

    public OptionsScreen(@NonNull Atto atto) {
        this.atto = atto;

        KeyHook.addListener(this);

        this.options = this.atto.getConfig().getInteractions();
    }

    @Override
    public void draw(@NonNull ConsoleWindow window, @NonNull Dimension size) throws Exception {
        if (this.atto.getScreenAction() == ScreenAction.OPTIONS) {
            // Write title bar
            String middleText = String.format("Atto %s", Atto.VERSION);
            InterfaceTheme theme = this.atto.getConfig().getInterfaceTheme();

            window.cursorTo(0, 0);
            window.setBackgroundColor(theme.getBackgroundColor());
            window.setTextColor(theme.getTextColor());
            window.setAttributes(theme.getTextAttributes());
            window.clearScreen();

            window.writeAt(MiscUtil.getPaddingToCenter(middleText.length(), size.width), 0, middleText);

            int cursorIndex = 0;
            for (Interactable<?> interactable : this.options) {
                String name = interactable.getName();
                String value = interactable.getValue();

                window.cursorTo(3, cursorIndex + 2).write(name);
                window.write(" ");

                if (this.optionIndex == cursorIndex) {
                    // Invert colors
                    window.setBackgroundColor(theme.getTextColor());
                    window.setTextColor(theme.getBackgroundColor());
                    window.cursorTo(35, cursorIndex + 2).saveCursorPosition();
                }

                // TODO calculate the widest option name
                window.cursorTo(35, cursorIndex + 2).write(value);

                // Reset color
                if (this.optionIndex == cursorIndex) {
                    window.setBackgroundColor(theme.getBackgroundColor());
                    window.setTextColor(theme.getTextColor());
                }

                cursorIndex++;
            }
        }
    }

    @Override
    public void onKey(char key, boolean alt, boolean control) {
        try {
            if (control) {
                switch (key) {

                    case 'p': { // ^P
                        if (this.atto.getScreenAction() == ScreenAction.EDITING_TEXT) {
                            this.atto.setScreenAction(ScreenAction.OPTIONS);
                        } else if (this.atto.getScreenAction() == ScreenAction.OPTIONS) {
                            LangProvider.setLanguage(this.atto.getConfig().getLanguage());
                            this.atto.getConfig().save();
                            this.atto.setScreenAction(ScreenAction.EDITING_TEXT);
                        }

                        this.atto.draw();

                        return;
                    }

                }
            }
        } catch (IOException e) {
            this.atto.exception(e);
        }
    }

    @Override
    public void onKey(InputKey key) {
        try {
            if (this.atto.getScreenAction() == ScreenAction.OPTIONS) {
                switch (key) {

                    case ESCAPE: {
                        LangProvider.setLanguage(this.atto.getConfig().getLanguage());
                        this.atto.getConfig().save();
                        this.atto.setScreenAction(ScreenAction.EDITING_TEXT);
                        this.atto.draw();

                        return;
                    }

                    case UP: {
                        this.optionIndex--;

                        if (this.optionIndex < 0) {
                            this.optionIndex = this.options.size() - 1;
                        }

                        this.atto.draw();

                        return;
                    }

                    case TAB:
                    case DOWN: {
                        this.optionIndex++;

                        if (this.optionIndex >= this.options.size()) {
                            this.optionIndex = 0;
                        }

                        this.atto.draw();

                        return;
                    }

                    case BACK_SPACE:
                    case DELETE:
                    case LEFT: {
                        Interactable<?> current = this.options.get(this.optionIndex);

                        if (current instanceof InteractableList) {
                            InteractableList list = (InteractableList) current;

                            list.decrement();
                        }

                        this.atto.draw();

                        return;
                    }

                    case ENTER:
                    case RIGHT: {
                        Interactable<?> current = this.options.get(this.optionIndex);

                        if (current instanceof InteractableList) {
                            InteractableList list = (InteractableList) current;

                            list.increment();
                        }

                        this.atto.draw();

                        return;
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
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | IOException e) {
            this.atto.exception(e);
        }
    }
}
