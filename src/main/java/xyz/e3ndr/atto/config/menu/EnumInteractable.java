package xyz.e3ndr.atto.config.menu;

import lombok.AllArgsConstructor;
import xyz.e3ndr.atto.util.EnumUtil;

@AllArgsConstructor
public class EnumInteractable implements InteractableList<Enum<?>> {

    @Override
    public Enum<?> increment(Enum<?> obj) {
        return EnumUtil.getNext(obj);
    }

    @Override
    public Enum<?> decrement(Enum<?> obj) {
        return EnumUtil.getPrevious(obj);
    }

    @Override
    public String getValue(Enum<?> obj) {
        return obj.name();
    }

}
