package xyz.e3ndr.atto.config.highlight‌ing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;
import xyz.e3ndr.atto.Atto;
import xyz.e3ndr.atto.util.MiscUtil;
import xyz.e3ndr.consoleutil.ansi.ConsoleColor;

public class Highlighter {
    private static Map<String, HighlighterConfig> configs = new HashMap<>();

    static {
        try {
            InputStream in = Highlighter.class.getResourceAsStream("/syntax/js.json");

            StringBuilder sb = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                int c;

                while ((c = reader.read()) != -1) {
                    sb.append((char) c);
                }
            }

            HighlighterConfig config = Atto.GSON.fromJson(sb.toString(), HighlighterConfig.class);

            config.sort();

            for (String alias : config.getAliases()) {
                configs.put(alias, config);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addConfig(@NonNull File file) throws IOException {
        InputStream in = new FileInputStream(file);

        StringBuilder sb = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            int c;

            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }
        }

        HighlighterConfig config = Atto.GSON.fromJson(sb.toString(), HighlighterConfig.class);

        config.sort();

        for (String alias : config.getAliases()) {
            configs.put(alias, config);
        }
    }

    public static String formatLine(@NonNull String text, @Nullable File file, @NonNull ConsoleColor defaultColor) {
        if (file != null) {
            String[] name = file.getName().split("\\.");
            HighlighterConfig config = configs.get(name[name.length - 1]);

            if (config != null) {
                StringBuilder textBuilder = new StringBuilder(text);
                StringBuilder result = new StringBuilder(text);

                for (HighlighterRule rule : config.getRules()) {
                    int colorSize = defaultColor.getForeground().length() + rule.getColor().getForeground().length();
                    Matcher matcher = Pattern.compile(rule.getMatch()).matcher(textBuilder.toString());
                    ConsoleColor ruleColor = rule.getColor();
                    int inOffset = 0;

                    while (matcher.find()) {
                        int size = matcher.group().length();

                        textBuilder.replace(matcher.start() + inOffset, matcher.end() + inOffset, MiscUtil.explode(size + colorSize));
                        result.insert(matcher.end() + inOffset, defaultColor.getForeground());
                        result.insert(matcher.start() + inOffset, ruleColor.getForeground());

                        inOffset += colorSize;
                    }
                }

                return result.toString();
            } else {
                return text;
            }
        } else {
            return text;
        }
    }

}
