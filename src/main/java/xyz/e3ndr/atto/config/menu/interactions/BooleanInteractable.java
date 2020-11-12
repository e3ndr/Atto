package xyz.e3ndr.atto.config.menu.interactions;

import xyz.e3ndr.atto.config.menu.Interaction;

public class BooleanInteractable extends Interactable<Boolean> implements InteractableList {

    public BooleanInteractable(Interaction annotation, Object holder, String prefix, String var) {
        super(annotation, holder, prefix, var);
    }

    @Override
    public String getValue() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        return String.valueOf(get());
    }

    @Override
    public void increment() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        this.set(!this.get());
    }

    @Override
    public void decrement() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        this.set(!this.get());
    }

}
