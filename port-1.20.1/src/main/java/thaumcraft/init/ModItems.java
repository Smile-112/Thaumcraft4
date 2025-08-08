package thaumcraft.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import thaumcraft.Thaumcraft;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Thaumcraft.MODID);

    public static final RegistryObject<Item> THAUMONOMICON = ITEMS.register(
            "thaumonomicon",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> ARCANE_STONE = ITEMS.register(
            "arcane_stone",
            () -> new BlockItem(ModBlocks.ARCANE_STONE.get(), new Item.Properties())
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

