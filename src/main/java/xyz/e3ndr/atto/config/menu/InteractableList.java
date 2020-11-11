package xyz.e3ndr.atto.config.menu;

public interface InteractableList<T> extends Interactable<T> {

    public T increment(T obj);

    public T decrement(T obj);

}
