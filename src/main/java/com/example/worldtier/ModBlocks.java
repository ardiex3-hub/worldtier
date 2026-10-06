package com.example.worldtier;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
    public static final Block MYTHRIL_ORE = register("mythril_ore", 3.0f);
    public static final Block ADAMANTITE_ORE = register("adamantite_ore", 4.5f);

    private ModBlocks() {
    }

    private static Block register(String name, float hardness) {
        Identifier id = WorldTierMod.id(name);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

        BlockBehaviour.Properties props = BlockBehaviour.Properties.of()
                .strength(hardness, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)
                .setId(blockKey);
        Block block = Registry.register(BuiltInRegistries.BLOCK, blockKey, new Block(props));

        BlockItem blockItem = new BlockItem(block,
                new Item.Properties().useBlockDescriptionPrefix().setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
        return block;
    }

    public static void initialize() {
    }
}
