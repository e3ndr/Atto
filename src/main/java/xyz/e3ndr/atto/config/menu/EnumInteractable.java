package xyz.e3ndr.atto.config.menu;

import lombok.NonNull;
import xyz.e3ndr.atto.util.EnumUtil;

public class EnumInteractable extends Interactable<Enum<?>> implements InteractableList {

    public EnumInteractable(Object holder, String prefix, String var) {
        super(holder, prefix, var);
        // TODO Auto-generated constructor stub
    }

    @Override
    public @NonNull String getValue() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        return get().name();
    }

    @Override
    public void increment() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        this.set(EnumUtil.getNext(this.get()));
    }

    @Override
    public void decrement() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        this.set(EnumUtil.getPrevious(this.get()));

    }

}
