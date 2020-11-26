package xyz.e3ndr.atto.lang;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Language {
    EN("English"),
    ES("Español"),
    FR("Français");

    private String str;

    @Override
    public String toString() {
        return this.str;
    }

}
