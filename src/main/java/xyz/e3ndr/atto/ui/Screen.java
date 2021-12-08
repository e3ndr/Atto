package xyz.e3ndr.atto.ui;

import java.awt.Dimension;

import lombok.NonNull;
import xyz.e3ndr.consoleutil.consolewindow.ConsoleWindow;

public interface Screen {

    public void draw(@NonNull ConsoleWindow window, @NonNull Dimension size) throws Exception;

}
