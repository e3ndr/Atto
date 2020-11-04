package xyz.e3ndr.atto;

import java.io.File;
import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import xyz.e3ndr.consoleutil.ConsoleUtil;

@Getter
@Command(name = "atto", mixinStandardHelpOptions = true, version = "Atto-" + Atto.VERSION, description = "Opens Atto")
public class Launcher implements Runnable {

    @Option(names = {
            "-f",
            "--file"
    }, description = "Opens the specified file")
    private @Nullable File file;

    public static void main(String[] args) throws IOException, InterruptedException {
        ConsoleUtil.summonConsoleWindow();
        new CommandLine(new Launcher()).execute(args);
    }

    @Override
    public void run() {
        try {
            new Atto(this);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    static {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(Integer.MAX_VALUE); // Keep a thread awake, since ConsoleUtil's key listener is a daemon thread.
                } catch (InterruptedException e) {}
            }
        }).start();
    }

}
