package com.azanor.thaumcraft;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TCContent {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Thaumcraft.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Thaumcraft.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Thaumcraft.MOD_ID);

    public static final RegistryObject<Block> ARCANE_STONE = BLOCKS.register("arcane_stone",
            () -> new Block(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.STONE)));

    public static final RegistryObject<Item> ARCANE_STONE_ITEM = ITEMS.register("arcane_stone",
            () -> new BlockItem(ARCANE_STONE.get(), new Item.Properties()));

    public static final RegistryObject<Item> THAUMONOMICON = ITEMS.register("thaumonomicon",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("thaumcraft",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.thaumcraft"))
                    .icon(() -> new ItemStack(THAUMONOMICON.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(THAUMONOMICON.get());
                        output.accept(ARCANE_STONE_ITEM.get());
                    })
                    .build());
}
