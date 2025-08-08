package thaumcraft;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Thaumcraft.MODID)
public class Thaumcraft {
    public static final String MODID = "thaumcraft";

    public Thaumcraft() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        // TODO: register blocks, items, etc.
    }
}
