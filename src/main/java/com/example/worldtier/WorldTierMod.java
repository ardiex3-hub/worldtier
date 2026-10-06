package com.example.worldtier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class WorldTierMod implements ModInitializer {
    public static final String MOD_ID = "worldtier";

    private static final Identifier HARDMODE_HEALTH = id("hardmode_health");
    private static final Identifier HARDMODE_DAMAGE = id("hardmode_damage");

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        ModItems.initialize();

        registerCreativeTabs();
        registerOreGeneration();
        registerBossTracking();
        registerOreGate();
        registerHardmodeScaling();
        registerCommand();
    }

    // ---------- Creative tab ----------
    private void registerCreativeTabs() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(tab -> {
            tab.accept(ModItems.MYTHRIL_INGOT);
            tab.accept(ModItems.ADAMANTITE_INGOT);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(tab -> {
            tab.accept(ModBlocks.MYTHRIL_ORE.asItem());
            tab.accept(ModBlocks.ADAMANTITE_ORE.asItem());
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(tab -> {
            tab.accept(ModItems.MYTHRIL_SWORD);
            tab.accept(ModItems.ADAMANTITE_SWORD);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(tab -> {
            tab.accept(ModItems.MYTHRIL_PICKAXE);
            tab.accept(ModItems.ADAMANTITE_PICKAXE);
        });
    }

    // ---------- Worldgen (konfigurasi ada di data/worldtier/worldgen) ----------
    private void registerOreGeneration() {
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                ResourceKey.<PlacedFeature>create(Registries.PLACED_FEATURE, id("mythril_ore")));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                ResourceKey.<PlacedFeature>create(Registries.PLACED_FEATURE, id("adamantite_ore")));
    }

    // ---------- Boss dikalahkan -> tier naik ----------
    private static int bossFlag(EntityType<?> type) {
        if (type == EntityType.ELDER_GUARDIAN) return WorldTierData.GUARDIAN;
        if (type == EntityType.WITHER) return WorldTierData.WITHER;
        if (type == EntityType.WARDEN) return WorldTierData.WARDEN;
        if (type == EntityType.ENDER_DRAGON) return WorldTierData.DRAGON;
        return 0;
    }

    private static String unlockMessage(int flag) {
        return switch (flag) {
            case WorldTierData.GUARDIAN -> "Elder Guardian kalah! Bijih Mythril kini bisa ditambang.";
            case WorldTierData.WITHER -> "Wither kalah! Dunia memasuki HARDMODE. Bijih Adamantite terbuka, monster makin kuat.";
            case WorldTierData.WARDEN -> "Warden kalah! Kegelapan makin dalam... monster makin kuat.";
            case WorldTierData.DRAGON -> "Ender Dragon kalah! Dunia mencapai tier tertinggi.";
            default -> "";
        };
    }

    private void registerBossTracking() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            int flag = bossFlag(entity.getType());
            if (flag == 0) {
                return;
            }
            MinecraftServer server = entity.level().getServer();
            if (server == null) {
                return;
            }
            WorldTierData data = WorldTierData.get(server);
            if (data.unlock(flag)) {
                server.getPlayerList().broadcastSystemMessage(
                        Component.literal("[World Tier] " + unlockMessage(flag)
                                + " (Tier " + data.getTier() + ")"), false);
            }
        });
    }

    // ---------- Bijih terkunci sampai boss kalah ----------
    private static int requiredFlag(BlockState state) {
        if (state.is(ModBlocks.MYTHRIL_ORE)) return WorldTierData.GUARDIAN;
        if (state.is(ModBlocks.ADAMANTITE_ORE)) return WorldTierData.WITHER;
        return 0;
    }

    private void registerOreGate() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            int need = requiredFlag(state);
            if (need == 0 || player.isCreative()) {
                return true;
            }
            MinecraftServer server = level.getServer();
            if (server == null) {
                return true;
            }
            if (WorldTierData.get(server).has(need)) {
                return true;
            }
            String boss = need == WorldTierData.GUARDIAN ? "Elder Guardian" : "Wither";
            player.sendSystemMessage(Component.literal(
                    "Bijih ini terlalu keras. Kalahkan " + boss + " dulu!"));
            return false;
        });
    }

    // ---------- Hardmode: monster lebih kuat mulai tier 2 ----------
    private void registerHardmodeScaling() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!(entity instanceof LivingEntity living)) {
                return;
            }
            if (living.getType().getCategory() != MobCategory.MONSTER) {
                return;
            }
            EntityType<?> type = living.getType();
            if (type == EntityType.WITHER || type == EntityType.WARDEN) {
                return;
            }
            int tier = WorldTierData.get(level.getServer()).getTier();
            if (tier < 2) {
                return;
            }
            double bonus = 0.3 * (tier - 1); // tier2 +30%, tier3 +60%, tier4 +90%

            AttributeInstance health = living.getAttribute(Attributes.MAX_HEALTH);
            if (health != null && !health.hasModifier(HARDMODE_HEALTH)) {
                health.addPermanentModifier(new AttributeModifier(
                        HARDMODE_HEALTH, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                living.setHealth(living.getMaxHealth());
            }
            AttributeInstance damage = living.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damage != null && !damage.hasModifier(HARDMODE_DAMAGE)) {
                damage.addPermanentModifier(new AttributeModifier(
                        HARDMODE_DAMAGE, bonus * 0.7, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        });
    }

    // ---------- /worldtier ----------
    private void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
                dispatcher.register(Commands.literal("worldtier").executes(ctx -> {
                    WorldTierData data = WorldTierData.get(ctx.getSource().getServer());
                    String text = "Tier dunia: " + data.getTier() + "/4 | "
                            + (data.has(WorldTierData.GUARDIAN) ? "[x]" : "[ ]") + " Elder Guardian  "
                            + (data.has(WorldTierData.WITHER) ? "[x]" : "[ ]") + " Wither  "
                            + (data.has(WorldTierData.WARDEN) ? "[x]" : "[ ]") + " Warden  "
                            + (data.has(WorldTierData.DRAGON) ? "[x]" : "[ ]") + " Ender Dragon";
                    ctx.getSource().sendSuccess(() -> Component.literal(text), false);
                    return 1;
                })));
    }
}
