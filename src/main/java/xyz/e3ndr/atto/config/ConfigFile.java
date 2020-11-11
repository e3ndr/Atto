package xyz.e3ndr.atto.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import xyz.e3ndr.atto.Atto;
import xyz.e3ndr.atto.config.menu.BooleanInteractable;
import xyz.e3ndr.atto.config.menu.EnumInteractable;
import xyz.e3ndr.atto.config.menu.IntegerInteractable;
import xyz.e3ndr.atto.config.menu.Interactable;
import xyz.e3ndr.atto.ui.LineEndings;
import xyz.e3ndr.consoleutil.ansi.ConsoleAttribute;
import xyz.e3ndr.consoleutil.ansi.ConsoleColor;

@Getter
public class ConfigFile {
    private static @Getter final Map<Class<?>, Interactable<?>> interactions = new HashMap<>();

    static {
        interactions.put(int.class, new IntegerInteractable());
        interactions.put(Integer.class, new IntegerInteractable());

        interactions.put(boolean.class, new BooleanInteractable());
        interactions.put(Boolean.class, new BooleanInteractable());

        interactions.put(Enum.class, new EnumInteractable());
    }

    private String language = "en";

    private LineEndings defaultLineEndings = LineEndings.detect("system");

    private boolean forceSize = false;
    private int width = 120;
    private int height = 30;

    private TextEditorTheme textEditorTheme = new TextEditorTheme();
    private InterfaceTheme interfaceTheme = new InterfaceTheme();

    @Getter
    @Setter
    public static class InterfaceTheme implements Theme {
        private ConsoleAttribute[] textAttributes = new ConsoleAttribute[0];
        private ConsoleColor backgroundColor = ConsoleColor.GRAY;
        private ConsoleColor textColor = ConsoleColor.BLACK;

    }

    @Getter
    @Setter
    public static class TextEditorTheme implements Theme {
        private ConsoleAttribute[] textAttributes = new ConsoleAttribute[0];
        private ConsoleColor backgroundColor = ConsoleColor.BLACK;
        private ConsoleColor textColor = ConsoleColor.GRAY;

    }

    public static interface Theme {

        public ConsoleAttribute[] getTextAttributes();

        public void setTextAttributes(ConsoleAttribute[] attributes);

        public ConsoleColor getBackgroundColor();

        public void setBackgroundColor(ConsoleColor color);

        public ConsoleColor getTextColor();

        public void setTextColor(ConsoleColor color);
    }

    public void save() throws IOException {
        String contents = Atto.GSON.toJson(this);
        byte[] bytes = contents.getBytes(StandardCharsets.UTF_8);

        Files.write(new File("config.json").toPath(), bytes);
    }

}
