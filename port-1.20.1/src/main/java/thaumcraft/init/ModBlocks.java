package thaumcraft.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import thaumcraft.Thaumcraft;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Thaumcraft.MODID);

    public static final RegistryObject<Block> ARCANE_STONE = BLOCKS.register(
            "arcane_stone",
            () -> new Block(BlockBehaviour.Properties.of(Material.STONE)
                    .strength(1.5F, 6.0F))
    );

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}

