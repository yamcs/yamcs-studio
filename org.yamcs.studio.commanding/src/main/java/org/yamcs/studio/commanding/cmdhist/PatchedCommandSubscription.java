package org.yamcs.studio.commanding.cmdhist;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.yamcs.api.MethodHandler;
import org.yamcs.client.Command;
import org.yamcs.client.CommandListener;
import org.yamcs.client.Helpers;
import org.yamcs.client.MessageListener;
import org.yamcs.client.base.AbstractSubscription;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.protobuf.SubscribeCommandsRequest;
import org.yamcs.studio.core.YamcsPlugin;

// Hack to work around aliases not added to initial websocket message upon
// a new command.
public class PatchedCommandSubscription extends AbstractSubscription<SubscribeCommandsRequest, CommandHistoryEntry> {

    // Concurrency only between consumers and update mechanism.
    // We are not expecting (nor supporting) parallel updates.
    private Map<String, Command> commands = new ConcurrentHashMap<>();
    private Set<CommandListener> commandListeners = new CopyOnWriteArraySet<>();

    public PatchedCommandSubscription(MethodHandler methodHandler) {
        super(methodHandler, "commands", CommandHistoryEntry.class);
        addMessageListener(new MessageListener<CommandHistoryEntry>() {

            @Override
            public void onMessage(CommandHistoryEntry entry) {
                var qname = entry.getCommandName();

                var info = YamcsPlugin.getMissionDatabase().getCommandInfo(qname);
                var aliases = new HashMap<String, String>();
                if (info != null) {
                    for (var alias : info.getAliasList()) {
                        aliases.put(alias.getNamespace(), alias.getName());
                    }
                }

                var generationTime = Helpers.toInstant(entry.getGenerationTime());
                var command = commands.computeIfAbsent(entry.getId(), id -> new Command(entry.getId(),
                        entry.getCommandName(), aliases, entry.getAssignmentsList(), entry.getOrigin(),
                        entry.getSequenceNumber(), generationTime));
                command.merge(entry);
                commandListeners.forEach(l -> {
                    l.onUpdate(command);
                    l.onUpdate(command, entry);
                });
            }

            @Override
            public void onError(Throwable t) {
                commandListeners.forEach(l -> l.onError(t));
            }
        });
    }

    public void addListener(CommandListener listener) {
        commandListeners.add(listener);
    }

    public void removeListener(CommandListener listener) {
        commandListeners.remove(listener);
    }

    public void clear() {
        commands.clear();
    }

    public Command getCommand(String id) {
        return commands.get(id);
    }
}
