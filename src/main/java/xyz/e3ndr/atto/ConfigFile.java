package xyz.e3ndr.atto;

import lombok.Getter;

@Getter
public class ConfigFile {
    private String language = "en";

    private String defaultLineEndings = "system";

    private boolean forceSize = false;
    private int width = 120;
    private int height = 30;

}
