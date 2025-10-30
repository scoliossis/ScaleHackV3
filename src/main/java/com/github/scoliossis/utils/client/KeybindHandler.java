package com.github.scoliossis.utils.client;

import com.github.scoliossis.events.SubscribeEvent;
import com.github.scoliossis.events.impl.KeyPressedEvent;
import com.github.scoliossis.modules.Module;
import com.github.scoliossis.modules.impl.client.Notifications;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeybindHandler {
    public static HashMap<Integer, List<Module>> keybindsMap = new HashMap<>();

    public static Module listeningModule = null;

    @SubscribeEvent
    public static void onKeyPressed(KeyPressedEvent event) {
        List<Module> modules = keybindsMap.get(event.keyCode);

        if (event.pressed) {
            if (listeningModule != null) {
                registerKeybind(listeningModule, event.keyCode);

                Notifications.addNotification("Keybind", "&cSuccessfully bound &f" + listeningModule.getAnnotation().name() + " &cto &f" + getCharacter(event.keyCode));

                listeningModule = null;
                return;
            }
        }

        if (modules != null) {
            for (Module module : modules) {
                if (module.holdKeybind) module.setEnabled(event.pressed);
                else if (event.pressed) module.toggle();
            }
        }

    }

    public static void registerKeybind(Module module, int keyCode) {
        removeKeybind(module);

        if (!keybindsMap.containsKey(keyCode))
            keybindsMap.put(keyCode, Stream.of(module).collect(Collectors.toList()));
        else
            keybindsMap.get(keyCode).add(module);

        module.setKeybind(keyCode);
    }

    public static void removeKeybind(Module module) {
        for (Map.Entry<Integer, List<Module>> entry : keybindsMap.entrySet()) {
            if (entry.getValue().contains(module)) {
                entry.getValue().remove(module);
                if (entry.getValue().isEmpty()) keybindsMap.remove(entry.getKey());

                break;
            }
        }

        module.setKeybind(-1);
    }

    public static String getCharacter(int keyCode) {
        return Keyboard.getKeyName(keyCode);
    }
}