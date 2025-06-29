package com.github.scoliossis.modules;

import com.github.scoliossis.Main;
import com.github.scoliossis.modules.SubModules.*;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.ChatUtil;
import com.github.scoliossis.utils.KeybindHandler;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    private static final HashMap<Class<? extends Module>, Module> modules = new HashMap<>();

    public static void init() {
        for (Class<?> clazz : C.reflections.getTypesAnnotatedWith(RegisterModule.class)) {
            try {
                System.out.println("Registering module: " + clazz.getSimpleName());

                RegisterModule annotation = clazz.getAnnotation(RegisterModule.class);

                Module m = (Module) clazz.newInstance();

                for (Field field : clazz.getDeclaredFields()) {
                    RegisterSubModule registerSubModule = field.getAnnotation(RegisterSubModule.class);

                    if (registerSubModule != null) {
                        SubModule subModule = getSubModule(field, m, registerSubModule);

                        if (!registerSubModule.parent().isEmpty()) {
                            // todo: children have to be below parents in the file, fix later if needed <3
                            for (SubModule sm : m.getChildren()) {
                                if (registerSubModule.parent().equals(sm.getAnnotation().name())) {
                                    sm.getChildren().add(subModule);
                                    subModule.setParent(sm);
                                    break;
                                }
                            }
                        }
                        m.getChildren().add(subModule);
                    }
                }

                Class<? extends Module> moduleClazz = (Class<? extends Module>) clazz;

                m.setAnnotation(annotation);
                modules.put(moduleClazz, m);
                m.setEnabled(annotation.enabledByDefault());
            } catch (Exception e) {
                System.err.println("Failed to register module: " + clazz.getSimpleName());
                e.printStackTrace();
            }

        }
    }

    private static SubModule getSubModule(Field field, Module m, RegisterSubModule registerSubModule) throws IllegalAccessException {
        SubModule subModule;
        if (field.getType() == SubCategory.class) subModule = new SubCategory(m, field, new ArrayList<>(), null, registerSubModule);
        else if (field.getType() == ColourSubModule.class) subModule = new ColourSubModule(m, field, new ArrayList<>(), null, registerSubModule);
        else if (field.getType() == boolean.class) subModule = new BooleanSubModule(m, field, new ArrayList<>(), null, registerSubModule);
        else if (field.getType().isEnum()) subModule = new EnumSubModule(m, field, new ArrayList<>(), null, registerSubModule);
        else subModule = new SliderSubModule(m, field, new ArrayList<>(), null, registerSubModule);
        return subModule;
    }

    public static boolean isEnabled(Class<? extends Module> clazz) {
        Module module = ModuleManager.getModule(clazz);
        return module != null && module.isEnabled();
    }
    public static void setEnabled(Class<? extends Module> clazz, boolean enabled) {
        ModuleManager.getModule(clazz).setEnabled(enabled);
    }

    public static Module getModule(Class<? extends Module> clazz) {
        return modules.get(clazz);
    }

    public static List<String> getModuleNames() {
        return getModules().stream().map(module -> module.getAnnotation().name()).collect(Collectors.toList());
    }

    public static List<Module> getModules() {
        return new ArrayList<>(modules.values());
    }

    public static List<Module> getModulesByCategory(Category category) {
        return getModules().stream().filter(e -> e.getAnnotation().category().equals(category)).collect(Collectors.toList());
    }

    public static List<Module> getModulesByCategory(Category category, List<Module> modules) {
        return modules.stream().filter(e -> e.getAnnotation().category().equals(category)).collect(Collectors.toList());
    }

    public static void openConfigFolder() {
        try {
            Files.createDirectories(Paths.get(Main.configPath));
            Desktop.getDesktop().open(new File(Main.configPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // god bless https://github.com/google/gson/blob/main/UserGuide.md
    public static void saveConfig(String configName) {
        if (!C.isInGame()) return;

        HashMap<String, LinkedTreeMap<String, Object>> moduleJSON = new HashMap<>();

        List<Module> modules = getModules();

        for (Module m : modules) {
            saveModule(moduleJSON, m);
        }

        saveConfig(Main.configPath, configName, C.gson.toJson(moduleJSON));
    }

    private static void saveModule(HashMap<String, LinkedTreeMap<String, Object>> moduleJSON, Module module) {
        LinkedTreeMap<String, Object> subModules = new LinkedTreeMap<>();

        subModules.put("enabled", module.isEnabled());
        subModules.put("keybind", module.getKeybind());

        for (SubModule sm : module.getChildren()) {
            if (sm.getField().getType() != SubCategory.class)
                subModules.put(sm.getAnnotation().name(), sm.get());
        }

        moduleJSON.put(module.getAnnotation().name(), subModules);
    }

    public static String saveModule(Module module) {
        HashMap<String, LinkedTreeMap<String, Object>> moduleJSON = new HashMap<>();

        saveModule(moduleJSON, module);

        return C.gson.toJson(moduleJSON);
    }

    public static void saveModule(Module module, String configName) {
        saveConfig(Main.configPath + module.getAnnotation().name() + "/", configName, saveModule(module));
    }

    public static void saveConfig(String filePath, String fileName, String JSON) {
        try {
            Files.createDirectories(Paths.get(filePath));
            Files.write(Paths.get(filePath + fileName + Main.configExtension), JSON.getBytes());
        } catch (IOException e) {
            ChatUtil.prefixMessage("Failed to save config: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static boolean loadKeybinds(String configName) {
        String configFileText = readConfig(configName);

        if (configFileText == null) return false;

        KeybindHandler.keybindsMap.clear();

        HashMap<String, LinkedTreeMap<String, Object>> modulesJSON = C.gson.fromJson(configFileText, HashMap.class);

        for (Module m : modules.values()) {
            if (modulesJSON.containsKey(m.getAnnotation().name())) {
                int keybind = getKeybindValue(modulesJSON.get(m.getAnnotation().name()));
                if (keybind != -1)
                    KeybindHandler.addKeybind(m, keybind);
            }
        }

        return true;
    }
    public static boolean loadConfig(String configName) {
        String configFileText = readConfig(configName);

        if (configFileText == null) return false;

        for (Module m : modules.values()) loadModule(m, configFileText);

        return true;
    }

    public static void loadModule(Module module, String configFileText) {
        HashMap<String, LinkedTreeMap<String, Object>> modulesJSON = C.gson.fromJson(configFileText, HashMap.class);

        if (!modulesJSON.containsKey(module.getAnnotation().name())) {
            ChatUtil.prefixMessage("&cWarning: &f" + module.getAnnotation().name() + " module not found, defaulting to &c" + module.getAnnotation().enabledByDefault());
        }
        else {
            LinkedTreeMap<String, Object> subModules = modulesJSON.get(module.getAnnotation().name());
            module.setEnabled(subModules.get("enabled").equals(true));
            int keybind = getKeybindValue(subModules);
            if (keybind != -1)
                KeybindHandler.addKeybind(module, keybind);

            for (SubModule subModule : module.getChildren()) {
                if (subModule.getField().getType() == SubCategory.class) continue;

                if (!subModules.containsKey(subModule.getAnnotation().name())) {
                    System.out.println("Module has no config: " + subModule.getAnnotation().name() + " submodule of " + module.getAnnotation().name() + " not found, defaulting to " + subModule.get());
                    continue;
                }

                Object value = subModules.get(subModule.getAnnotation().name());

                if (subModule.getField().getType().isEnum()) {
                    Enum<?>[] enumConstants = (Enum<?>[]) subModule.getField().getType().getEnumConstants();
                    for (Enum<?> enumConstant : enumConstants)
                        if (enumConstant.name().equals(value)) subModule.set(enumConstant);
                }
                // todo: this probably should be cleaned up... bad impl.
                else if (subModule.getField().getType() == ColourSubModule.class) {
                    LinkedTreeMap<String, Object> colourSubModule = (LinkedTreeMap<String, Object>) value;

                    // i could use math to work these out im sure, but i dont want to :yawn:
                    ArrayList<Double> m = (ArrayList<Double>) colourSubModule.get("lastColourPickerMousePos");
                    int[] lastColourPickerMousePos = new int[] {m.get(0).intValue(), m.get(1).intValue()};
                    int lastHslMouseY = ((Double) colourSubModule.get("lastHslMouseY")).intValue();
                    int lastOpacityMouseY = ((Double) colourSubModule.get("lastOpacityMouseY")).intValue();

                    LinkedTreeMap<String, Object> colourMap = (LinkedTreeMap<String, Object>) colourSubModule.get("colour");
                    Color colour = new Color(((Double) colourMap.get("value")).intValue(), true);

                    subModule.set(new ColourSubModule(lastColourPickerMousePos, lastHslMouseY, lastOpacityMouseY, false, colour));
                }
                else {
                    subModule.set(value);
                }
            }
        }
    }

    private static int getKeybindValue(LinkedTreeMap<String, Object> subModules) {
        // it thinks -1 is a double :(
        return (int) Double.parseDouble(subModules.get("keybind").toString());
    }

    public static String readConfig(String configName) {
        try {
            File configFile = new File(Main.configPath + configName + Main.configExtension);
            if (!Files.exists(configFile.toPath())) {
                ChatUtil.prefixMessage("&cConfig file does not exist: &f" + configName);
                return null;
            }
            return FileUtils.readFileToString(configFile);
        } catch (IOException e) {
            ChatUtil.prefixMessage("&cFailed to read config: " + e.getMessage());
            return null;
        }
    }

    public static List<File> getConfigFiles(String folderPath) {
        return Arrays.asList(Objects.requireNonNull(Paths.get(Main.configPath + folderPath).toFile().listFiles()));
    }
}
