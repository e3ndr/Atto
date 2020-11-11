package xyz.e3ndr.atto;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.jetbrains.annotations.Nullable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import xyz.e3ndr.atto.config.ConfigFile;
import xyz.e3ndr.consoleutil.ConsoleUtil;

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
            File configFile = new File("config.json");
            ConfigFile config;

            if (configFile.exists()) {
                byte[] bytes = Files.readAllBytes(configFile.toPath());
                String contents = new String(bytes, StandardCharsets.UTF_8);

                config = Atto.GSON.fromJson(contents, ConfigFile.class);
            } else {
                config = new ConfigFile();
            }

            // Replace empty keys, and reset formatting.
            config.save();

            new Atto(this.file, config);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
