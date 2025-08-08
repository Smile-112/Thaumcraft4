package thaumcraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.eventbus.api.EventPriority;
import thaumcraft.init.ModItems;
import thaumcraft.init.ModCreativeTabs;
import thaumcraft.init.ModBlocks;
import thaumcraft.network.NetworkHandler;
import thaumcraft.research.ResearchDataManager;
import thaumcraft.scan.AspectDataManager;
import thaumcraft.world.aura.AuraProvider;

@Mod(Thaumcraft.MODID)
public class Thaumcraft {
    public static final String MODID = "thaumcraft";

    public Thaumcraft() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.REGISTER.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModBlocks.REGISTER.register(modBus);
        modBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.init();
    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new AspectDataManager());
        event.addListener(new ResearchDataManager());
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void attachCapabilities(AttachCapabilitiesEvent<LevelChunk> event) {
        if (event.getObject() != null) {
            event.addCapability(thaumcraft.world.aura.AuraProvider.KEY, new AuraProvider());
        }
    }
}