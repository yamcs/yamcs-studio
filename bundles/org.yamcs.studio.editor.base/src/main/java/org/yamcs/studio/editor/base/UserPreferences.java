package org.yamcs.studio.editor.base;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Access to user preferences (stored under ~/.config/yamcs-studio).
 */
public class UserPreferences {

    public static Path getDataDir() {
        Path userHome = Paths.get(System.getProperty("user.home"));
        // The location ~/.config conforms to XDG.
        // For windows it'd be more standard if we could write to '~\Local Settings\Application Data' but I'm
        // not aware of cross-platform API for this.
        return userHome.resolve(".config").resolve("yamcs-studio");
    }

    public static Path getHistoryFile() {
        return getDataDir().resolve("workspace_history");
    }

    public static List<String> readWorkspaceHistory() {
        Path file = getHistoryFile();
        if (!Files.exists(file)) {
            return new ArrayList<>(0);
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8)
                    .stream()
                    .filter(line -> !line.trim().isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>(0);
        }
    }

    public static void updateWorkspaceHistory(String mostRecentWorkspace) {
        String normalized = Paths.get(mostRecentWorkspace).normalize().toAbsolutePath().toString();

        List<String> history = readWorkspaceHistory();
        history.removeIf(w -> w.equals(normalized));
        history.add(0, normalized);

        // Limit list size
        while (history.size() > 10) {
            history.remove(history.size() - 1);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(getHistoryFile(), StandardCharsets.UTF_8)) {
            for (String workspace : history) {
                writer.write(workspace);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
