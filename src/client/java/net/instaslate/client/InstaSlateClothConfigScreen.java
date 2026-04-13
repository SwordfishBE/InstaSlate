package net.instaslate.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.instaslate.InstaSlate;
import net.instaslate.config.InstaSlateConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class InstaSlateClothConfigScreen {

    private InstaSlateClothConfigScreen() {
    }

    public static Screen create(Screen parent) {
        InstaSlateConfig editedConfig = InstaSlate.loadConfigForEditing();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("InstaSlate Config"));

        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

        general.addEntry(entries.startBooleanToggle(
                        Component.literal("Enable InstaSlate"),
                        editedConfig.isEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.literal("When enabled, deepslate mines at stone speed with a Netherite pickaxe, Efficiency V, and Haste II."))
                .setSaveConsumer(editedConfig::setEnabled)
                .build());

        builder.setSavingRunnable(() -> InstaSlate.applyEditedConfig(editedConfig));
        return builder.build();
    }
}
