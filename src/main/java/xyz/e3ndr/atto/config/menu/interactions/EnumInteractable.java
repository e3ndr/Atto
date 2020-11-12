package xyz.e3ndr.atto.config.menu.interactions;

import lombok.NonNull;
import xyz.e3ndr.atto.config.menu.Interaction;
import xyz.e3ndr.atto.util.EnumUtil;
import xyz.e3ndr.consoleutil.ansi.ConsoleAttribute;
import xyz.e3ndr.consoleutil.ansi.ConsoleColor;

public class EnumInteractable extends Interactable<Enum<?>> implements InteractableList {

    public EnumInteractable(Interaction annotation, Object holder, String prefix, String var) {
        super(annotation, holder, prefix, var);
    }

    @Override
    public @NonNull String getValue() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Enum<?> e = this.get();

        // We don't have control over these, so we manually prettify them.
        if ((e instanceof ConsoleColor) || (e instanceof ConsoleAttribute)) {
            return EnumUtil.prettify(e);
        } else {
            return e.toString();
        }
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
