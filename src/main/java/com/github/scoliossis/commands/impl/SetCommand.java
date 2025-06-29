package com.github.scoliossis.commands.impl;

import com.github.scoliossis.commands.Command;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.SubModule;
import com.github.scoliossis.utils.ChatUtil;
import com.github.scoliossis.utils.FuzzySearchUtil;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SetCommand extends Command {
    @Override
    public String name() {
        return "set";
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length < 3) return false;

        Module module = FuzzySearchUtil.findModule(args[0]);
        SubModule subModule = FuzzySearchUtil.findSubModule(args[1], module);
        Class<?> type = subModule.getField().getType();
        String settingValue = args[2];

        if (type.isEnum()) {
            List<String> enumValues = Arrays.stream(type.getEnumConstants()).map(e -> ((Enum<?>)e).name().toLowerCase()).collect(Collectors.toList());
            Optional<ExtractedResult> enumValue =  FuzzySearchUtil.fuzzySearch(settingValue, enumValues);
            if (!enumValue.isPresent()) return false;
            Object value = type.getEnumConstants()[enumValue.get().getIndex()];

            ChatUtil.prefixMessage("&c" + module.getAnnotation().name() + "'s " + subModule.getAnnotation().name() + " &fchanged from &c" + subModule.get() + " &fto &c" + ((Enum<?>) value).name());
            subModule.set(value);

            return true;
        }
        else if (type == double.class || type == float.class || type == int.class || type == long.class) {
            if (!settingValue.matches("-?\\d+(\\.\\d+)?")) return false;

            double value = Double.parseDouble(settingValue);
            // make sure you keep within bounds :)
            value = Math.max(Math.min(subModule.getAnnotation().max(), value), subModule.getAnnotation().min());

            ChatUtil.prefixMessage("&c" + module.getAnnotation().name() + "'s " + subModule.getAnnotation().name() + " &fchanged from &c" + subModule.get() + " &fto &c" + value);
            subModule.set(value);

            return true;
        }
        else if (type == boolean.class) {
            if (!settingValue.equals("true") && !settingValue.equals("false")) return false;

            subModule.set(settingValue.equals("true"));
            ChatUtil.prefixMessage(module.getAnnotation().name() + "'s " + subModule.getAnnotation().name() + " &fchanged to " + (settingValue.equals("true") ? "&aEnabled" : "&cDisabled"));
            return true;
        }

        return false;
    }

    @Override
    public String[] usage() {
        return new String[] { "<module> <setting> <value>" };
    }
}
