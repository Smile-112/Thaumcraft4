package thaumcraft;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import thaumcraft.compat.CuriosCompat;

@Mod(Thaumcraft.MODID)
public class Thaumcraft {
    public static final String MODID = "thaumcraft";

    public Thaumcraft() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        if (ModList.get().isLoaded("curios")) {
            CuriosCompat.registerSlots();
        }
        // TODO: register blocks, items, etc.
    }
}
