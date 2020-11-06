package xyz.e3ndr.atto.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;

public class LangProvider {
    private static Map<String, String> lang = new HashMap<>();

    public static void setLanguage(@NonNull String language) {
        try {
            lang = new HashMap<>();

            InputStream in = LangProvider.class.getResourceAsStream(String.format("/%s.lang", language));

            StringBuilder sb = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                int c = 0;
                while ((c = reader.read()) != -1) {
                    sb.append((char) c);
                }
            }

            String[] lines = sb.toString().split("\n");

            for (String line : lines) {
                if (line.contains("=")) {
                    String[] split = line.split("=", 2);

                    lang.put(split[0].toLowerCase(), split[1]);
                }
            }
        } catch (IOException ignored) {}
    }

    public static String get(@NonNull String key) {
        return lang.getOrDefault(key.toLowerCase(), key.toLowerCase());
    }

}
