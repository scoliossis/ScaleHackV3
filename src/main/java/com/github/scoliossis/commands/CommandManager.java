package com.github.scoliossis.commands;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.PacketEvent;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.ChatUtil;
import lombok.Getter;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import net.minecraft.network.play.client.C01PacketChatMessage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// i dont like this command manager but im sadly too lazy to make the improvements it needs.
public class CommandManager {
    @Getter
    private static final ArrayList<Command> commands = new ArrayList<>();

    public static void init() {
        for (Class<?> clazz : C.reflections.getSubTypesOf(Command.class)) {
            try {
                System.out.println("Registering command: " + clazz.getSimpleName());
                commands.add((Command) clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException ignored) { }
        }
    }

    @SubscribeEvent
    public static void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof C01PacketChatMessage) {
            String message = ((C01PacketChatMessage) event.packet).getMessage();

            if (message.startsWith(".")) {
                event.setCancelled(true);
                handleCommand(message.substring(1));
            }
        }
    }

    private static void handleCommand(String commandText) {
        String commandName = commandText.split(" ")[0].toLowerCase();

        List<String> commandNames = commands.stream().map(command -> command.name().toLowerCase()).collect(Collectors.toList());

        // i love fuzzysearch
        Optional<ExtractedResult> best = FuzzySearch.extractAll(commandName, commandNames, 40).stream().max(Comparator.comparingInt(ExtractedResult::getScore));
        if (!best.isPresent()) {
            ChatUtil.prefixMessage("&cCouldn't find command: " + commandName);
            ChatUtil.prefixMessage("&cUse .help for a list of commands!");
            return;
        }

        Command command = commands.get(best.get().getIndex());
        String[] args = commandText.substring(commandName.length()).trim().split(" ");

        if (!command.execute(args)) {
            ChatUtil.prefixMessage("&cInvalid Usage");
            for (String usage : command.usage()) {
                ChatUtil.prefixMessage("&c." + command.name() + " &f" + usage);
            }
        }
    }

    public static String gatherArgs(String[] args, int split) {
        String remainingArgs = "";
        for (int i = split; i < args.length; i++) {
            remainingArgs += args[i] + " ";
        }

        return remainingArgs.trim();
    }
}
