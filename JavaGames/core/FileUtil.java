package core;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;

public final class FileUtil {
    private FileUtil() {} // prevent instantiation

    public static void appendLog(String fileName, String text) {
        try {
            Files.createDirectories(Paths.get("logs"));
            try (BufferedWriter bw = Files.newBufferedWriter(
                    Paths.get("logs", fileName),
                    java.nio.charset.StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                bw.write(LocalDateTime.now() + " | " + text);
                bw.newLine();
            }
        } catch (IOException e) {
            // silently fail to keep UI stable
        }
    }
}
