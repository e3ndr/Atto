package xyz.e3ndr.atto.config.menu;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import xyz.e3ndr.reflectionlib.ReflectionLib;

@NonNull
@AllArgsConstructor
public abstract class Interactable<T> {
    protected Object holder;
    private String prefix;
    protected String var;

    public abstract @NonNull String getValue() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException;

    protected T get() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        return ReflectionLib.getValue(this.holder, this.var);
    }

    protected void set(T value) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        ReflectionLib.setValue(this.holder, this.var, value);
    }

    public String getName() {
        String[] split = this.var.split("(?=\\p{Lu})");
        String spaced = String.join(" ", split);

        return this.prefix + spaced.substring(0, 1).toUpperCase() + spaced.substring(1);
    }

}
