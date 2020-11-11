package xyz.e3ndr.atto.config.menu;

public class BooleanInteractable extends Interactable<Boolean> implements InteractableList {

    public BooleanInteractable(Object holder, String prefix, String var) {
        super(holder, prefix, var);
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
