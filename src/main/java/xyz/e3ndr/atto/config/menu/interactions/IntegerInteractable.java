package xyz.e3ndr.atto.config.menu.interactions;

import lombok.NonNull;
import xyz.e3ndr.atto.config.menu.Interaction;

public class IntegerInteractable extends Interactable<Integer> implements InteractableList {

    public IntegerInteractable(Interaction annotation, Object holder, String prefix, String var) {
        super(annotation, holder, prefix, var);
    }

    @Override
    public @NonNull String getValue() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        return String.valueOf(get());
    }

    @Override
    public void increment() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        int newValue = this.get() + 1;

        if (newValue <= this.annotation.upperBound()) {
            this.set(newValue);
        }
    }

    @Override
    public void decrement() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        int newValue = this.get() - 1;

        if (newValue >= this.annotation.lowerBound()) {
            this.set(newValue);
        }
    }

}
