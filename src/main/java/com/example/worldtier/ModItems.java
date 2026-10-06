package com.example.worldtier;

import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

public final class ModItems {
    public static final TagKey<Block> INCORRECT_FOR_MYTHRIL_TOOL =
            TagKey.create(Registries.BLOCK, WorldTierMod.id("incorrect_for_mythril_tool"));
    public static final TagKey<Item> MYTHRIL_REPAIR =
            TagKey.create(Registries.ITEM, WorldTierMod.id("mythril_repair"));
    public static final TagKey<Block> INCORRECT_FOR_ADAMANTITE_TOOL =
            TagKey.create(Registries.BLOCK, WorldTierMod.id("incorrect_for_adamantite_tool"));
    public static final TagKey<Item> ADAMANTITE_REPAIR =
            TagKey.create(Registries.ITEM, WorldTierMod.id("adamantite_repair"));

    //                                         tag salah,                 durability, speed, dmg bonus, enchant, repair
    public static final ToolMaterial MYTHRIL_MATERIAL = new ToolMaterial(
            INCORRECT_FOR_MYTHRIL_TOOL, 900, 7.0F, 2.5F, 18, MYTHRIL_REPAIR);
    public static final ToolMaterial ADAMANTITE_MATERIAL = new ToolMaterial(
            INCORRECT_FOR_ADAMANTITE_TOOL, 2400, 9.5F, 4.5F, 20, ADAMANTITE_REPAIR);

    public static final Item MYTHRIL_INGOT = register("mythril_ingot", Item::new, new Item.Properties());
    public static final Item ADAMANTITE_INGOT = register("adamantite_ingot", Item::new, new Item.Properties());

    public static final Item MYTHRIL_SWORD = register("mythril_sword", Item::new,
            new Item.Properties().sword(MYTHRIL_MATERIAL, 3.0F, -2.4F));
    public static final Item MYTHRIL_PICKAXE = register("mythril_pickaxe", Item::new,
            new Item.Properties().pickaxe(MYTHRIL_MATERIAL, 1.0F, -2.8F));
    public static final Item ADAMANTITE_SWORD = register("adamantite_sword", Item::new,
            new Item.Properties().sword(ADAMANTITE_MATERIAL, 3.0F, -2.4F));
    public static final Item ADAMANTITE_PICKAXE = register("adamantite_pickaxe", Item::new,
            new Item.Properties().pickaxe(ADAMANTITE_MATERIAL, 1.0F, -2.8F));

    private ModItems() {
    }

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, WorldTierMod.id(name));
        Item item = factory.apply(props.setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void initialize() {
    }
}
