package xyz.e3ndr.atto.config.menu;

import lombok.NonNull;

public class IntegerInteractable extends Interactable<Integer> implements InteractableList {

    public IntegerInteractable(Object holder, String prefix, String var) {
        super(holder, prefix, var);
    }

    @Override
    public @NonNull String getValue() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        return String.valueOf(get());
    }

    @Override
    public void increment() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        this.set(this.get() + 1);
    }

    @Override
    public void decrement() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        this.set(this.get() - 1);
    }

}
