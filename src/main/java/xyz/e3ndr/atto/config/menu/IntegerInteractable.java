package xyz.e3ndr.atto.config.menu;

public class IntegerInteractable implements InteractableList<Integer> {

    @Override
    public String getValue(Integer obj) {
        return obj.toString();
    }

    @Override
    public Integer increment(Integer obj) {
        return obj++;
    }

    @Override
    public Integer decrement(Integer obj) {
        return obj--;
    }

}
