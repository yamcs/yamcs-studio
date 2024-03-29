/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.editor.base;

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
        var userHome = Paths.get(System.getProperty("user.home"));
        // The location ~/.config conforms to XDG.
        // For windows it'd be more standard if we could write to '~\Local Settings\Application Data' but I'm
        // not aware of cross-platform API for this.
        return userHome.resolve(".config").resolve("yamcs-studio");
    }

    public static Path getHistoryFile() {
        return getDataDir().resolve("workspace_history");
    }

    public static List<String> readWorkspaceHistory() {
        var file = getHistoryFile();
        if (!Files.exists(file)) {
            return new ArrayList<>(0);
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8).stream().filter(line -> !line.trim().isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>(0);
        }
    }

    public static void updateWorkspaceHistory(String mostRecentWorkspace) {
        var normalized = Paths.get(mostRecentWorkspace).normalize().toAbsolutePath().toString();

        var history = readWorkspaceHistory();
        history.removeIf(w -> w.equals(normalized));
        history.add(0, normalized);

        // Limit list size
        while (history.size() > 10) {
            history.remove(history.size() - 1);
        }

        try (var writer = Files.newBufferedWriter(getHistoryFile(), StandardCharsets.UTF_8)) {
            for (var workspace : history) {
                writer.write(workspace);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
