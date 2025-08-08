package thaumcraft.init;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import thaumcraft.Thaumcraft;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(ForgeRegistries.CREATIVE_MODE_TABS, Thaumcraft.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.thaumcraft.main"))
            .icon(() -> new ItemStack(ModItems.THAUMOMETER.get()))
            .displayItems((params, output) -> {
                output.accept(ModItems.THAUMOMETER.get());
                output.accept(ModItems.THAUMONOMICON.get());
                // Place more items/blocks here as they get ported:
                output.accept(new ItemStack(net.minecraft.world.level.block.Blocks.BOOKSHELF));
            })
            .build()
    );
}