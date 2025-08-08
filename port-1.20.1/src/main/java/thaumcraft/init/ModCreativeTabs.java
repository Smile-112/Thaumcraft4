package thaumcraft.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import thaumcraft.Thaumcraft;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Thaumcraft.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + Thaumcraft.MODID + ".main"))
            .icon(() -> new ItemStack(ModItems.THAUMOMETER.get()))
            .displayItems((params, output) -> {
                output.accept(ModItems.THAUMOMETER.get());
                output.accept(ModItems.THAUMONOMICON.get());
            })
            .build()
    );
}
