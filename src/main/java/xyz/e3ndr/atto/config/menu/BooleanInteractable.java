package xyz.e3ndr.atto.config.menu;

public class BooleanInteractable implements InteractableList<Boolean> {

    @Override
    public String getValue(Boolean obj) {
        return obj.toString();
    }

    @Override
    public Boolean increment(Boolean obj) {
        return !obj;
    }

    @Override
    public Boolean decrement(Boolean obj) {
        return !obj;
    }

}
