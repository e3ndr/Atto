package xyz.e3ndr.atto.ui;

import java.awt.Dimension;
import java.io.IOException;

import lombok.NonNull;
import xyz.e3ndr.consoleutil.ConsoleWindow;

public interface Screen {

    public void draw(@NonNull ConsoleWindow window, @NonNull Dimension size) throws IOException, InterruptedException;

}
