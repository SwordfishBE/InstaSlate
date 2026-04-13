package net.instaslate;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.instaslate.config.InstaSlateConfig;
import net.instaslate.config.InstaSlateConfigManager;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class InstaSlate implements ModInitializer {

    public static final String MOD_ID = "instaslate";
    public static final String MOD_NAME = FabricLoader.getInstance()
            .getModContainer(MOD_ID)
            .map(container -> container.getMetadata().getName())
            .orElse("InstaSlate");
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final float BOOSTED_DEEPSLATE_TOOL_SPEED = 38.3F;
    private static Tool defaultNetheritePickaxeTool;
    private static Tool boostedNetheritePickaxeTool;

    private static InstaSlateConfigManager configManager;
    private static InstaSlateConfig config;

    @Override
    public void onInitialize() {
        configManager = new InstaSlateConfigManager();
        config = configManager.load();
        ServerTickEvents.END_SERVER_TICK.register(server ->
                server.getPlayerList().getPlayers().forEach(InstaSlate::updateBoostedMainHandTool)
        );

        LOGGER.info("{} Mod initialized. Version: {}", getLogPrefix(), getModVersion());
    }

    public static InstaSlateConfig getConfig() {
        return config;
    }

    public static InstaSlateConfig loadConfigForEditing() {
        InstaSlateConfig editableConfig = new InstaSlateConfig();
        editableConfig.setEnabled(config.isEnabled());
        return editableConfig;
    }

    public static void applyEditedConfig(InstaSlateConfig editedConfig) {
        editedConfig.validate();
        configManager.save(editedConfig);
        config = editedConfig;
        LOGGER.debug("{} Config updated: enabled={}", getLogPrefix(), config.isEnabled());
    }

    public static String getLogPrefix() {
        return "[" + MOD_NAME + "]";
    }

    public static String getModVersion() {
        return FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    private static void updateBoostedMainHandTool(ServerPlayer player) {
        ensureToolProfilesInitialized();

        if (defaultNetheritePickaxeTool == null || boostedNetheritePickaxeTool == null) {
            return;
        }

        boolean changed = false;
        Inventory inventory = player.getInventory();
        ItemStack mainHandStack = player.getMainHandItem();
        boolean shouldBoostMainHand = shouldBoostMainHandTool(player, mainHandStack);

        if (shouldBoostMainHand && !isBoostedNetheritePickaxe(mainHandStack)) {
            mainHandStack.set(DataComponents.TOOL, boostedNetheritePickaxeTool);
            changed = true;
        } else if (!shouldBoostMainHand && isBoostedNetheritePickaxe(mainHandStack)) {
            mainHandStack.set(DataComponents.TOOL, defaultNetheritePickaxeTool);
            changed = true;
        }

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack == mainHandStack || !isBoostedNetheritePickaxe(stack)) {
                continue;
            }

            stack.set(DataComponents.TOOL, defaultNetheritePickaxeTool);
            changed = true;
        }

        if (!changed) {
            return;
        }

        inventory.setChanged();
        player.containerMenu.broadcastChanges();
    }

    private static boolean shouldBoostMainHandTool(Player player, ItemStack mainHandStack) {
        if (!config.isEnabled() || !mainHandStack.is(Items.NETHERITE_PICKAXE)) {
            return false;
        }

        HolderGetter<Enchantment> enchantments = player.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT);

        int efficiencyLevel = EnchantmentHelper.getItemEnchantmentLevel(
                enchantments.getOrThrow(Enchantments.EFFICIENCY),
                mainHandStack
        );

        if (efficiencyLevel < 5) {
            return false;
        }

        MobEffectInstance haste = player.getEffect(MobEffects.HASTE);
        return haste != null && haste.getAmplifier() >= 1;
    }

    private static boolean isBoostedNetheritePickaxe(ItemStack stack) {
        return stack.is(Items.NETHERITE_PICKAXE) && boostedNetheritePickaxeTool != null
                && boostedNetheritePickaxeTool.equals(stack.get(DataComponents.TOOL));
    }

    private static Tool createBoostedNetheritePickaxeTool() {
        List<Tool.Rule> boostedRules = new ArrayList<>();
        boolean deepslateRuleInserted = false;

        for (Tool.Rule rule : defaultNetheritePickaxeTool.rules()) {
            boostedRules.add(rule);

            if (!deepslateRuleInserted && rule.correctForDrops().filter(correctForDrops -> !correctForDrops).isPresent()) {
                boostedRules.add(Tool.Rule.minesAndDrops(
                        HolderSet.direct(Blocks.DEEPSLATE.builtInRegistryHolder()),
                        BOOSTED_DEEPSLATE_TOOL_SPEED
                ));
                deepslateRuleInserted = true;
            }
        }

        if (!deepslateRuleInserted) {
            boostedRules.add(0, Tool.Rule.minesAndDrops(
                    HolderSet.direct(Blocks.DEEPSLATE.builtInRegistryHolder()),
                    BOOSTED_DEEPSLATE_TOOL_SPEED
            ));
        }

        return new Tool(
                boostedRules,
                defaultNetheritePickaxeTool.defaultMiningSpeed(),
                defaultNetheritePickaxeTool.damagePerBlock(),
                defaultNetheritePickaxeTool.canDestroyBlocksInCreative()
        );
    }

    private static void ensureToolProfilesInitialized() {
        if (defaultNetheritePickaxeTool != null && boostedNetheritePickaxeTool != null) {
            return;
        }

        Tool tool = Items.NETHERITE_PICKAXE.getDefaultInstance().get(DataComponents.TOOL);
        if (tool == null) {
            LOGGER.warn("{} Netherite pickaxe tool component is not available yet.", getLogPrefix());
            return;
        }

        defaultNetheritePickaxeTool = tool;
        boostedNetheritePickaxeTool = createBoostedNetheritePickaxeTool();
    }
}
