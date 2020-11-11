package xyz.e3ndr.atto;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.jetbrains.annotations.Nullable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import xyz.e3ndr.atto.config.AttoConfig;
import xyz.e3ndr.consoleutil.ConsoleUtil;

@Command(name = "atto", mixinStandardHelpOptions = true, version = "Atto-" + Atto.VERSION, description = "Opens Atto")
public class Launcher implements Runnable {

    @Option(names = {
            "-f",
            "--file"
    }, description = "Opens the specified file")
    private @Nullable File file;

    @Option(names = {
            "-d",
            "--debug"
    }, description = "Enables debugging")
    private boolean debug;

    public static void main(String[] args) throws IOException, InterruptedException {
        ConsoleUtil.summonConsoleWindow();
        new CommandLine(new Launcher()).execute(args);
    }

    @Override
    public void run() {
        try {
            File configFile = new File("config.json");
            AttoConfig config;

            if (configFile.exists()) {
                byte[] bytes = Files.readAllBytes(configFile.toPath());
                String contents = new String(bytes, StandardCharsets.UTF_8);

                config = Atto.GSON.fromJson(contents, AttoConfig.class);
            } else {
                config = new AttoConfig();
            }

            // Replace empty keys, and reset formatting.
            config.save();

            new Atto(this.file, this.debug, config);
        } catch (Exception e) {
            ConsoleUtil.getJLine().enableInterruptCharacter();

            e.printStackTrace();

            try {
                Thread.sleep(Integer.MAX_VALUE);
            } catch (InterruptedException ignored) {}
        }
    }

}
