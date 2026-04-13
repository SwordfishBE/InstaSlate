package net.instaslate;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.instaslate.config.InstaSlateConfig;
import net.instaslate.config.InstaSlateConfigManager;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class InstaSlate implements ModInitializer {

    public static final String MOD_ID = "instaslate";
    public static final String MOD_NAME = FabricLoader.getInstance()
            .getModContainer(MOD_ID)
            .map(container -> container.getMetadata().getName())
            .orElse("InstaSlate");
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Set<Block> BOOSTED_BLOCKS = Set.of(
            Blocks.DEEPSLATE,
            Blocks.COBBLED_DEEPSLATE,
            Blocks.COBBLED_DEEPSLATE_STAIRS,
            Blocks.COBBLED_DEEPSLATE_SLAB,
            Blocks.COBBLED_DEEPSLATE_WALL,
            Blocks.POLISHED_DEEPSLATE,
            Blocks.POLISHED_DEEPSLATE_STAIRS,
            Blocks.POLISHED_DEEPSLATE_SLAB,
            Blocks.POLISHED_DEEPSLATE_WALL,
            Blocks.DEEPSLATE_TILES,
            Blocks.DEEPSLATE_TILE_STAIRS,
            Blocks.DEEPSLATE_TILE_SLAB,
            Blocks.DEEPSLATE_TILE_WALL,
            Blocks.DEEPSLATE_BRICKS,
            Blocks.DEEPSLATE_BRICK_STAIRS,
            Blocks.DEEPSLATE_BRICK_SLAB,
            Blocks.DEEPSLATE_BRICK_WALL,
            Blocks.CHISELED_DEEPSLATE,
            Blocks.CRACKED_DEEPSLATE_BRICKS,
            Blocks.CRACKED_DEEPSLATE_TILES,
            Blocks.INFESTED_DEEPSLATE,
            Blocks.REINFORCED_DEEPSLATE,
            Blocks.DEEPSLATE_COAL_ORE,
            Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.DEEPSLATE_IRON_ORE,
            Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.DEEPSLATE_REDSTONE_ORE
    );

    private static InstaSlateConfigManager configManager;
    private static InstaSlateConfig config;

    @Override
    public void onInitialize() {
        configManager = new InstaSlateConfigManager();
        config = configManager.load();

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

    public static boolean shouldBoostDeepslateMining(Player player, BlockState state) {
        if (!config.isEnabled()) {
            return false;
        }

        if (!BOOSTED_BLOCKS.contains(state.getBlock())) {
            return false;
        }

        ItemStack mainHandStack = player.getMainHandItem();
        if (!mainHandStack.is(Items.NETHERITE_PICKAXE)) {
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

    public static String getLogPrefix() {
        return "[" + MOD_NAME + "]";
    }

    public static String getModVersion() {
        return FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }
}
