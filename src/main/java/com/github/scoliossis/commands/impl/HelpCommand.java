package com.github.scoliossis.commands.impl;

import com.github.scoliossis.Main;
import com.github.scoliossis.commands.Command;
import com.github.scoliossis.commands.CommandManager;
import com.github.scoliossis.utils.ChatUtil;

import java.util.stream.Collectors;

public class HelpCommand extends Command {

    @Override
    public String name() {
        return "help";
    }

    @Override
    public boolean execute(String[] args) {
        switch (args[0]) {
            case "commands":
                ChatUtil.prefixMessage("&cCommands: ");
                ChatUtil.chat(
                        CommandManager.getCommands().stream().map(command -> {
                                    StringBuilder commandUsages = new StringBuilder();
                                    for (String usage : command.usage())
                                        commandUsages.append("\n&c.").append(command.name()).append(" &f").append(usage);
                                    return ((commandUsages.length() == 0) ? "\n&c." + command.name() : commandUsages.toString());
                                }
                        ).collect(Collectors.joining("\n"))
                );
                break;

            default:
                ChatUtil.prefixMessage("Mod Information: ");
                ChatUtil.chat(
                        "&7Modules are found using fuzzy search, this means you can type in any part of the module name to find it!" +
                        "\n&7Spaces are used to split each command argument, so you can't name configs with spaces!" +
                        "\n&7" + (Main.optifineInstalled
                                ? "Optifine is installed, this means you cannot drag and drop cookie alts"
                                : "Optifine is not installed, this means you can drag and drop cookie alts!") +
                        "\n&7Use \".help commands\" &7to see a list of commands.");
        }

        return true;
    }

    @Override
    public String[] usage() {
        return new String[] {
                "",
                "commands"
        };
    }
}
