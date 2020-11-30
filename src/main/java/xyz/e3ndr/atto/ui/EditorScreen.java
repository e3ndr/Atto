package xyz.e3ndr.atto.ui;

import java.io.File;
import java.io.IOException;

public interface EditorScreen extends Screen {

    public boolean isEdited();

    public void save(File fileFromBuffer) throws IOException, InterruptedException;

    public void load(File fileFromBuffer) throws IOException, InterruptedException;

    public File getFile();

    public boolean isOverwriting();

}
