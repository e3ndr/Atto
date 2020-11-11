package xyz.e3ndr.atto.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import xyz.e3ndr.atto.Atto;
import xyz.e3ndr.atto.config.menu.BooleanInteractable;
import xyz.e3ndr.atto.config.menu.EnumInteractable;
import xyz.e3ndr.atto.config.menu.IntegerInteractable;
import xyz.e3ndr.atto.config.menu.Interactable;
import xyz.e3ndr.atto.config.menu.Interaction;
import xyz.e3ndr.atto.config.menu.SubInteraction;
import xyz.e3ndr.atto.ui.LineEndings;
import xyz.e3ndr.consoleutil.ansi.ConsoleAttribute;
import xyz.e3ndr.consoleutil.ansi.ConsoleColor;
import xyz.e3ndr.reflectionlib.ReflectionLib;

@Getter
public class AttoConfig {
    private static final Map<Class<?>, Class<? extends Interactable<?>>> interactions = new HashMap<>();

    static {
        interactions.put(int.class, IntegerInteractable.class);
        interactions.put(Integer.class, IntegerInteractable.class);

        interactions.put(boolean.class, BooleanInteractable.class);
        interactions.put(Boolean.class, BooleanInteractable.class);

        interactions.put(Enum.class, EnumInteractable.class);
    }

    // System
    private String language = "en";

    private @Interaction LineEndings defaultLineEndings = LineEndings.fromString("system");

    // Sizes
    private @Interaction boolean forceSize = false;
    private @Interaction int width = 120;
    private @Interaction int height = 30;

    // Theming
    private @SubInteraction("Text Editor") TextEditorTheme textEditorTheme = new TextEditorTheme();
    private @SubInteraction("Interface") InterfaceTheme interfaceTheme = new InterfaceTheme();

    @Getter
    @Setter
    public static class InterfaceTheme implements Theme {
        private ConsoleAttribute[] textAttributes = new ConsoleAttribute[0];
        private @Interaction ConsoleColor backgroundColor = ConsoleColor.GRAY;
        private @Interaction ConsoleColor textColor = ConsoleColor.BLACK;

    }

    @Getter
    @Setter
    public static class TextEditorTheme implements Theme {
        private ConsoleAttribute[] textAttributes = new ConsoleAttribute[0];
        private @Interaction ConsoleColor backgroundColor = ConsoleColor.BLACK;
        private @Interaction ConsoleColor textColor = ConsoleColor.GRAY;

    }

    @SneakyThrows
    public List<Interactable<?>> getInteractions() {
        List<Interactable<?>> interactables = new ArrayList<>();

        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Interaction.class)) {
                interactables.add(getForType(this, "", field));
            } else if (field.isAnnotationPresent(SubInteraction.class)) {
                Object holder = ReflectionLib.getValue(this, field.getName());
                SubInteraction sub = field.getAnnotation(SubInteraction.class);

                for (Field subField : holder.getClass().getDeclaredFields()) {
                    if (subField.isAnnotationPresent(Interaction.class)) {
                        interactables.add(getForType(holder, sub.value() + ' ', subField));
                    }
                }
            }
        }

        return interactables;
    }

    public void save() throws IOException {
        String contents = Atto.GSON.toJson(this);
        byte[] bytes = contents.getBytes(StandardCharsets.UTF_8);

        Files.write(new File("config.json").toPath(), bytes);
    }

    public static Interactable<?> getForType(Object holder, String prefix, Field field) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<?> fieldType = field.getType();
        Class<? extends Interactable<?>> interactableClazz;

        if (Enum.class.isAssignableFrom(fieldType)) {
            interactableClazz = interactions.get(Enum.class);
        } else {
            interactableClazz = interactions.get(fieldType);
        }

        Constructor<? extends Interactable<?>> constructor = interactableClazz.getConstructor(Object.class, String.class, String.class);

        return constructor.newInstance(holder, prefix, field.getName());
    }

    public static interface Theme {

        public ConsoleAttribute[] getTextAttributes();

        public void setTextAttributes(ConsoleAttribute[] attributes);

        public ConsoleColor getBackgroundColor();

        public void setBackgroundColor(ConsoleColor color);

        public ConsoleColor getTextColor();

        public void setTextColor(ConsoleColor color);
    }

}
