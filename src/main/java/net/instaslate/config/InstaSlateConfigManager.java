package net.instaslate.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;
import net.instaslate.InstaSlate;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class InstaSlateConfigManager {

    private static final String CONFIG_FILE_NAME = "instaslate.json";
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final Path configPath;

    public InstaSlateConfigManager() {
        this.configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve(CONFIG_FILE_NAME);
    }

    public InstaSlateConfig load() {
        if (!Files.exists(configPath)) {
            InstaSlate.LOGGER.debug("{} Config not found, creating default file at {}", InstaSlate.getLogPrefix(), configPath);
            InstaSlateConfig defaults = new InstaSlateConfig();
            save(defaults);
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(true);

            InstaSlateConfig loadedConfig = GSON.fromJson(jsonReader, InstaSlateConfig.class);
            if (loadedConfig == null) {
                InstaSlate.LOGGER.warn("{} Config file was empty or invalid, using defaults.", InstaSlate.getLogPrefix());
                loadedConfig = new InstaSlateConfig();
            }

            loadedConfig.validate();
            save(loadedConfig);
            InstaSlate.LOGGER.debug("{} Config loaded: {}", InstaSlate.getLogPrefix(), loadedConfig);
            return loadedConfig;
        } catch (IOException exception) {
            InstaSlate.LOGGER.warn("{} Failed to read config file, using defaults.", InstaSlate.getLogPrefix(), exception);
            return new InstaSlateConfig();
        }
    }

    public void save(InstaSlateConfig config) {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                writer.write(buildConfigFileContents(config));
            }
            InstaSlate.LOGGER.debug("{} Config saved to {}", InstaSlate.getLogPrefix(), configPath);
        } catch (IOException exception) {
            InstaSlate.LOGGER.warn("{} Failed to save config file.", InstaSlate.getLogPrefix(), exception);
        }
    }

    private String buildConfigFileContents(InstaSlateConfig config) {
        return """
                {
                  "enabled": %s // Enables the deepslate mining speed boost for a Netherite pickaxe with Efficiency V and Haste II.
                }
                """.formatted(config.isEnabled());
    }
}
